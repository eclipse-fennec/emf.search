# Architecture

How `emf.search` is put together, and why. This page is the stable, user-facing view; the
[blueprint](./search-access.md)
carries the full design, the complete capability profile and the task plan.

::: warning Status
Early development — this describes the target architecture. Bundles appear as the wave-1
issues land.
:::

## The layers

```
consumer
   │  canonical query IR (org.eclipse.fennec.query.model)
   ▼
QueryProcessor (backend=lucene) ──► capability refusal, when Lucene cannot answer honestly
   │  Lucene Query
   ▼
index unit ── IndexWriter / SearcherManager (NRT)
   ▲
   │  Document
mapping pipeline ◄── esearch.ecore mapping model (per EPackage)
   ▲
   │  EObject
PersistenceResource / Resource.Factory
```

Nothing in that column is OSGi-specific. The OSGi layer sits *beside* it: Declarative
Services components that build an index unit from a Configuration Admin factory
configuration and publish it — plus the `Resource.Factory` registration that lets a Fennec
`ResourceSet` discover the backend.

## Bundles

| Bundle | Content |
|---|---|
| `org.eclipse.fennec.search.model` | `esearch.ecore` — the mapping metamodel and its generated EMF code. Plain EMF. |
| `org.eclipse.fennec.search` | The backend as a plain-Java library: index lifecycle, mapping pipeline, query translation, highlighting, similarity. |
| `org.eclipse.fennec.search.osgi` | The thin OSGi layer: DS components, configuration per index unit, whiteboard publication, `Resource.Factory`. |
| `org.eclipse.fennec.search.suggest` (+ `.osgi`) | Suggest/completion — its own small API, sharing the mapping model and the index lifecycle. |
| `org.eclipse.fennec.search.index` | Secondary-index maintenance fed by the change stream, plus query routing (later stage). |
| `org.eclipse.fennec.search.tck` | The binding against the published Fennec persistence TCK. |
| `org.eclipse.fennec.search.workspace.library` | bnd workspace library for downstream consumption. |

The `.osgi` bundles inline their core packages, so a consumer needs one bundle at runtime
rather than two.

## The mapping model — "eorm for the index"

Indexing is declared, not coded. Per `EClass`: index name, default analyzer, refresh and
commit policy. Per `EAttribute`: indexed / stored / tokenized, analyzer, doc values for
sorting and faceting, facet dimension, boost. Convention defaults cover small models
without any declaration at all — an id becomes a stored keyword field, strings become
analyzed text, numerics become point fields with doc values.

One attribute often needs more than one index field: analyzed text answers "find documents
about this", an unanalyzed keyword answers "exactly this value" and sorts and facets, and a
second analyzer may serve another language. A field mapping therefore carries *sub-fields* —
the outer mapping is the primary projection, sub-fields inherit its attribute and are named
relative to it (`title.keyword`). Where two projections could answer the same predicate, the
mapping declares which one is for what (`MATCH`, `EXACT`, `RANGE`, `SORT`, `FACET`,
`HIGHLIGHT`, `SIMILARITY`); an ambiguous declaration is rejected rather than guessed.

A field does not have to read a single attribute. It can follow a path of features across
the model, or compute its value with an OCL expression evaluated against the instance — and
it can exist with no attribute of its own at all, as a field that lives only in the index
(a concatenated search-over-everything field, a normalized sort key). Two things follow, and
both are deliberate: a field with no attribute cannot be named in a canonical query, so
values that must be *queryable* belong in the model as a derived feature rather than in the
index as a virtual field; and a value read across a reference makes the document depend on
another object, which is only refreshed when the owner is saved.

References are the interesting part, because a Lucene document is flat and an EMF model is
not. Three strategies:

- **`EMBED`** — denormalize the target's fields under a prefix. Simple, and enough for
  single-valued targets.
- **`NESTED`** — index the target as a child document inside the parent's block, queried
  through a block join. This is the one that preserves *correlation*: with `EMBED`, a query
  for `child.a = 1 AND child.b = 2` also matches an object where two *different* children
  satisfy the two conditions. `NESTED` does not. The price is that a block is only
  replaceable as a whole, so changing one child reindexes its parent.
- **`ID_ONLY`** — store the target's id and nothing else. Queries across such a reference
  are refused, exactly as cross-document paths are in the MongoDB backend.

### Where mappings come from

A mapping is an EMF model, so it travels like one. `emf.search` invents no registration
mechanism of its own — it uses the two that
[`emf.osgi`](https://github.com/eclipse-fennec/emf.osgi) already provides:

- **Authored as an `*.esearch` XMI document** and loaded into a named *EObject registry*
  (`emf.osgi.eobject.registry`). The backend looks its unit up by name; registry listeners
  make a changed mapping observable without a restart. The registry has an explicit
  non-OSGi bootstrap, so a plain-Java application uses the same mechanism rather than a
  parallel one.
- **Shipped with the model bundle**, attached to the package metadata as an aspect
  (`MetadataService`, `AspectEntry` with `typeId = "esearch"`) — the same slot the codec
  and ORM aspects use. A model can then carry its own index mapping, and it survives being
  written to and read back from a metadata index.

## Query translation and its limits

The `QueryProcessor` for `backend=lucene` translates the canonical IR: equality and set
membership, comparisons and ranges, string matching (contains / startsWith / endsWith /
LIKE / fuzzy, case-insensitive variants), null checks, boolean junctions, sorting, paging,
counting, type filters, relevance scoring, facet counts and geo predicates.

Three things are worth knowing as a consumer:

**Negation is three-valued.** Lucene's `MUST_NOT` matches documents where the field is
simply absent, which is not what "not equal to 5" means over nullable data. The translation
pushes negation down (De Morgan plus operator inversion) and guards negated predicates with
a field-existence check, so a null never satisfies a comparison — negated or not. The same
rule the SQL and MongoDB backends follow; the shared TCK pins it.

**Fuzzy matching wants a keyword field.** `StringMatchKind.FUZZY` is edit distance over the
whole value, so it is answered by a `FuzzyQuery` on a keyword projection, where the indexed
term *is* the value — with the same Damerau-Levenshtein budget (`maxEdits` 1 or 2, adjacent
transpositions counted as one edit) and exact-prefix rule the in-memory reference uses, so
the two agree hit for hit. Over analyzed text the kind is refused with the way out, because
term-level fuzziness over tokens answers a different question than the IR asked; a
`caseInsensitive` fuzzy match is refused for the same reason a folded automaton cannot fold
the terms it walks. Neighbours are never truncated to the closest few: the rewrite is
constant-score, not Lucene's default top-terms one.

**Refusals are part of the contract.** Query-time joins, field-to-field comparisons,
arithmetic pushdown and reference traversal over non-embedded references are declared
unsupported. The backend tells you, with a diagnostic code; it does not silently return a
subset. The single exception to "no joins" is the block join above, and it is honest
precisely because it is an index-time fact rather than a query-time operation — which is
also why it is scoped to containment only.

Scoring follows the same discipline: static rank signals (popularity, recency, curated
boost) are *declared in the mapping model* and selected by name, rather than letting a
consumer send a scoring formula. That keeps the arithmetic refusal meaningful.

## Beyond the query IR

Some things do not belong in a cross-backend query language, because no other backend can
answer them. Those get their own small APIs in this repository instead of polluting the
shared vocabulary:

- **Suggest / completion** — its own service per index unit, sharing the mapping model and
  index lifecycle. (The predecessor built a second, parallel stack for this. Sharing the
  model and lifecycle, but not the query API, is the correction.)
- **Highlighting** — needs the executed query and the live searcher, so it lives inside the
  Lucene bundle, with its own entry point.
- **Similarity** ("objects like this one") — term-statistics based, no embeddings involved.

## Plain Java and OSGi

The rule is symmetric, and both directions matter:

- **No OSGi in the cores.** Index units, analyzer registries and mapping registries are
  built from plain constructors and config objects. Declarative Services types map *onto*
  those objects; they never replace them. Anything OSGi resolves through a whiteboard has a
  programmatic counterpart for plain-Java use.
- **No plain-Java shortcuts that break OSGi.** No static singletons, no reliance on the
  global EMF registries — state belongs to the index-unit instance, so a second unit or a
  bundle restart cannot corrupt it.

The practical payoff is the test suite: mapping, translation, negation semantics, facets,
suggest, highlighting, scoring and the entire TCK binding run as plain JUnit against an
in-memory directory. Framework tests are reserved for what only a framework can prove —
component activation, configuration, service discovery through a `ResourceSet`, and
behaviour when configuration disappears at runtime.

## Relationship to the persistence stack

The query IR, the backend SPI, the capability vocabulary and the TCK live in
[`emf.persistence-jpa`](https://github.com/eclipse-fennec/emf.persistence-jpa) and are
consumed here as published artifacts — one direction only. When a query concept is missing,
it is raised there as an IR issue rather than invented here; engine-specific machinery
(analyzers, suggest, highlighting) stays here. That boundary is what keeps "the same query
against a different backend" true.
