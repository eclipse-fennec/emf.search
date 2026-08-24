# Open decisions — for review

Internal working log, unpublished. Two kinds of entries: **open** (blocked on an upstream
decision, each with its revisit trigger) and **taken but not yet reviewed** (decided
autonomously, pinned by tests, awaiting Mark's eyes). Settled entries are deleted; their
outcomes live in `search-access.md`, the issues, and the code's own documentation.
Cleaned 2026-08-18 after the A/B/C review — everything reviewed there is gone from here.

## Open — blocked on upstream, with revisit triggers

- **Lazy results / `SERVER_CURSORS`** — the `StoreFeature` literal exists since the
  2026-08-18 publish (#162); implementing means holding the searcher lease until
  `QueryResult.close()` (an `IndexUnit` acquire/release surface) and declaring the
  feature. Next sizeable unit-lifecycle task; until built, results stay materialized —
  behaviour must not change undeclared.
- **Interval predicates (S15, #17)** — raised 2026-08-23 as emf.persistence-jpa**#215**
  (INTERSECTS / WITHIN / CONTAINS, where the interval comes from, boundary and unbounded
  semantics, one capability). Nothing is built here on purpose: the metamodel's
  `RangeFieldMapping` stays refused by the writer, because a range field no query can reach
  is dead weight in the index and would have to be rebuilt anyway once the shape is known.
  The refusal names the fallback (two comparisons over the bound attributes), which is what
  a modeller needs today. *When it lands:* write `LongRange`/`DoubleRange` and map the three
  relations — Lucene's side is one-to-one, so this is a small task the day the vocabulary
  exists. *Also revisit if* the upstream round decides the query names the bound pair
  (the `GeoSubject` precedent of #113) rather than the mapping declaring it: then
  `RangeFieldMapping` becomes an index-shape declaration only, not the subject.

## Decided upstream, zero change here

- **#161 (query-capability narrowing): refused with reasons** (2026-08-18) — query
  capabilities stay backend-wide by definition; the mapping-dependent truth is
  `validate()`'s job, a mapping-dependent refusal is a Diagnostic with the way out. That
  is exactly what this backend already does; the #114-by-analogy stance is now the stated
  upstream rule. *Revisit trigger (upstream's):* a real pre-validation router would make
  the `EStructuralFeature` overload a purely additive extension.

## Autonomous calls re-syncing with the persistence update (2026-08-23)

The upstream snapshot of 2026-08-22 added an abstract TCK method and eleven ungated §8
core cases. Three of them failed here; all three were product defects, not binding gaps.
Pinned by `SearchResourceTest` and the TCK binding.

1. **The URI type segment widens to subtypes** (`typeFilter`, abstract classes included).
   Previously a raw discriminator term — every polymorphic case had gone through a gated
   type predicate, so the URI path had never been asked.
2. **Delete refuses when an `ID_ONLY` reference still points at the object** (#195), by
   probing before deleting. The known limit is the id model, not the probe: `_root` is the
   bare id value, so two types sharing an id share a block identity — the same collision
   the delete itself has. *Revisit if* a unit-wide unique document key ever replaces the
   bare id.
3. **The named-operation catalog is swapped, and the old registry reference survives as a
   fallback** (#203/#163, done 2026-08-23 — the entry that used to sit under "open" is
   settled). `SearchResource` and its factory take a `NamedOperations`; the OSGi component
   prefers a bound service and otherwise wraps the registry it already referenced in
   `RegistryNamedOperations`, so a deployment configured before the contract existed keeps
   working without a second lookup road of our own. Documented in §5.6.
4. **`PROJECTION_EXPRESSION` (#189) stays undeclared** — the projection counterpart of the
   SORT_EXPRESSION refusal. The shared validator refuses it by name; the test pins that
   nobody declares it by accident.

## Housekeeping (2026-08-23)

Not decisions so much as answers that had been left implicit, all pinned now.

1. **`testOSGi` may not pass by having run nothing (#35).** Two guards: a run must leave a
   report containing at least one test (previous reports are deleted first, or the guard
   accepts yesterday's), and `resolve.test` no longer claims to be up to date, because Gradle
   cannot see that a changed *bundle* invalidates a resolution. The committed `-runbundles`
   is checked against what the resolver produces — the note asking for a hand-refresh had
   already gone stale once, so it is a gate now. Verified by reproducing the original symptom.
2. **References across resources (#33): the index stores ids, not URIs.** Two behaviours were
   wrong and are refusals now — a proxy whose fragment addresses a *position* (no EMF id on
   the target's class) used to be written into the index as if it were an id, and an
   unresolved child of a NESTED block used to be written as an empty document. And indexing
   no longer resolves proxies at all: a write must not load the model it points at.
3. **The read side sees sub-fields (#39).** `FieldUse` decides which projection answers —
   declared where a modeller must say it, derived from the kind otherwise. Two projections
   claiming one use are refused at mapping time. A mapping with one projection per attribute
   behaves exactly as before, which is what made this safe to change late.
4. **#34 and #36 are upstream reports**, raised as emf.persistence-jpa#216 (the TCK is not in
   the `fennecPersistence` library — a `fennecPersistenceTest` library is the shape that
   matches the existing `fennec`/`fennecTest` split) and #217 (two libraries sharing one bnd
   plugin key, so the older index silently wins). Both stay worked around here.

## Computed fields (S20/#28, 2026-08-23)

**Mark's ecore round**, chosen from three shapes: `sources : ValueSource[0..*]` *beside*
`feature`, with `FeatureSource` / `PathSource` / `OclSource` and an optional `separator`.
Additive — no existing mapping changes, and the conventions stay feature-based.

**Mark's second call, made while building it: the OCL rung stays out.** Wiring the m2x engine
had pulled it into `IndexSchema`'s load path, and the suggest bundle fell over with a
`NoClassDefFoundError` — which surfaced the real question rather than a missing buildpath
entry: must every deployment carry an expression engine so that some mapping *could* compute
something? Three roads were on the table (isolate behind an interface with an optional
import; make m2x a hard dependency; drop the rung) and the answer is the third. `OclSource`
stays declared and is refused by name, pointing at a `PathSource` or at a derived
`EStructuralFeature` with the m2x derivation annotation — which is computed by EMF, arrives
here as an ordinary feature, and is *queryable*, unlike anything computed inside a mapping.

The calls I made around it, pinned by `ComputedFieldTest`:

1. **A field says where its value comes from once.** Declaring both `feature` and `sources`
   is refused rather than resolved by precedence.
2. **A computed field must carry a name** — there is no attribute to take one from — and a
   **sub-field may not be computed**: a sub-field is another projection of its parent's
   attribute, which is exactly what a different value under that name would contradict.
3. **Several sources are several values, unless `separator` joins them.** The two cases are
   genuinely different (match one of these, versus search this one string), and guessing from
   the field kind would be the kind of cleverness that surprises.
4. **Only text, keyword and numeric fields take sources.** Geo, rank signals and the reserved
   kinds declare their own inputs; a computed one of those would be the same thing said twice.
5. **A computed numeric derives its encoding from the value** when the mapping says `AUTO`,
   and a non-numeric value is refused by name rather than coerced.
6. **`dependencies(EClass)` reports the navigated paths**, and losing the expression rung is
   what makes that exact instead of best-effort. **`fingerprint()`** answers the third part of
   the issue — a stable hash over everything that decides what lands in the index, for a unit
   to record beside its data (S18) and notice that a rebuild is due.

## Autonomous calls in write commands (S21/#29, S22/#30, 2026-08-23)

The blueprint prescribed the shape (§5.4) — these are the calls it did not make. Pinned by
`WriteCommandTest` and by the TCK's command family.

1. **The transaction bracket is refused, as recommended.** `IndexWriter.rollback()` discards
   every uncommitted change in the unit rather than the caller's share, so a bracket would be
   sound only while a single writer owns the unit — a condition this backend cannot enforce.
   Option 2 of #30 (serialize the bracket) stays the documented upgrade path: it turns a write
   bracket into a global write lock on the unit, which is a throughput decision a consumer has
   to ask for, not a default.
2. **A refusal carries its Diagnostic inside the exception.** `IOException` → cause
   `QueryException` → `getDiagnostic()`, beside the `PersistenceDiagnostic` on the resource.
   The upstream cross-product (#175) reads exactly that to separate a refusal from a failure,
   and the query path was changed with the command path so both speak one contract.
3. **Delete resolves matches to root ids and deletes blocks**, rather than handing the
   selector to `deleteDocuments(Query)` — which would take the matched roots and leave the
   `NESTED` children behind. The §4.4 referential-integrity refusal applies to the whole
   matched set, with references *inside* the set exempt: they go away together.
4. **Update patches everything before writing anything.** All matches are read, patched and
   mapped first; the writer sees them only if every one of them worked. Half-applied is the
   one outcome a store must not produce quietly.
5. **`ID_ONLY` writes accept an unresolved proxy** and take the id from its URI fragment.
   Without it read-patch-rewrite cannot work at all — a reconstructed object holds proxies for
   its references — and it fixes the same latent hole on the ordinary save path.
6. **A `STORED_OBJECT` document is completed with its `ID_ONLY` fields on read.** The
   serialized tree cannot carry a cross-document reference (at write time the target lives in
   another document, so the serializer has no href), the fields beside it can, and for those
   references the fields are the truth — the reference is unset before it is re-read, or a
   many-valued one comes back doubled. Found by the TCK the moment the binding declared
   materialization.
7. **The TCK binding declares `STORED_OBJECT` for its model.** Not cosmetics: it is what makes
   `UPDATE_BY_SELECTOR` honest for the TCK's classes, so the update family runs for real
   instead of being narrowed away. The refusal without it is covered by plain JUnit.

## Grouping with representatives (S19/#21, 2026-08-23)

**Mark's call**, after the clarification the issue asked for: the pipeline cannot express
"top-N documents per group" (a `GroupByStage` yields key plus aggregates, never documents),
and the slot upstream reserved for it — `BottomTop` — is unbuilt. Of the three roads (own
API plus an upstream issue / only the issue and park S19 / own API and never an issue) he
chose the first: ship it beside the contract now, and keep the IR road open.
`emf.persistence-jpa#214` carries the shape question, including the two things the backend
knows a caller needs (documents per group, and the group's *total* so a truncated group says
so) and the result-shape question the IR round actually turns on.

The calls made building it, pinned by `GroupSearchTest`:

1. **A document without the key is in no group.** Lucene collects those into a null group
   and — worse — counts it, because the all-groups collector reads values rather than the
   selector's verdict. So they are filtered out of the match set with a `FieldExistsQuery`
   instead of explained away afterwards; the count is then honest by construction.
2. **The key must be a keyword projection with doc values, and single-valued.** Analyzed
   text has no single value per document, a numeric key would need ranges nobody declared,
   and a many-valued attribute would put one object in several groups. All three are refused
   by name; the facet API is the way out for the last one.
3. **The doc-values shape is wrapped, not the mapping bent.** `TermGroupSelector` reads
   `SORTED` and every keyword field here is `SORTED_SET` (what a many-valued attribute needs,
   and what the keyword sort already reads). `KeywordGroupSelector` wraps the set through
   `SortedSetSelector`; unambiguous because a many-valued key is refused anyway.
4. **Groups ordered by their best hit, representatives by relevance** — "the best three of
   every manufacturer" in the order the phrase means. Ordering by key or by group size is
   what the upstream shape should decide, not a second knob invented here.
5. **`GroupResults` is not a `QueryResult`.** A grouped answer is neither the objects nor the
   rows the contract knows; dressing it as either would misreport it. That is also the
   cleanest thing to retire the day the IR grows the stage.

## Autonomous calls in rank signals (S14/#16, 2026-08-23)

The metamodel already carried `RankSignalFieldMapping` (function, pivot, exponent) from S2,
so what needed deciding was everything around it. Pinned by `RankSignalTest`.

1. **Selection rides on `QueryContext.options()`** (`SearchOptions.RANK_SIGNALS`), not on an
   own API and not on new IR vocabulary. An own API of the suggest kind would duplicate the
   query and materialization path to change one thing about the score; IR vocabulary would
   break the §3 ground rule for something that is per-engine by nature. The options map is
   the contract's own extension point and it is documented as such upstream.
2. **One reserved field for every signal** (`_features`), feature-named per declaration —
   Lucene's own shape for this, and it keeps a feature field (which no other type may sit
   next to under one name) out of the mapped-name universe.
3. **A signal-only attribute is not a comparable field**, refused with the way out — the geo
   rule again. The way out is a rank signal declared as a *sub-field* of an ordinary
   projection, which the sub-field machinery already supported; the compound name
   (`views.signal`) is what a query then selects.
4. **A non-positive or non-finite value is no signal**, not an error: the document carries
   none and scores on text alone, the same answer as for an attribute that was never set.
   Lucene refuses such a weight, and a data-driven signal that made a write fail would be a
   trap — a new item with zero views is ordinary data, not a mapping mistake.
5. **Refused rather than ignored**: a selected signal on a COUNT/AGGREGATION shape (no score
   to shape — the `withScores` precedent), a SIGMOID without a pivot (no point to turn at;
   SATURATION derives one from index statistics), a many-valued attribute (one weight per
   feature name per document), and two declarations sharing a name but disagreeing.
   *Not* refused, only documented: a query with a field sort reads no score, so a signal
   selected there does nothing.
6. **The declared `boost` is the signal's weight.** `FieldMapping.boost` already said
   "a curated relevance decision belongs in the mapping, not in the query", so a per-query
   weight would have contradicted the field it sits next to.

## Autonomous calls in geo (S9/#13, 2026-08-19)

Both §5.5 open points are settled in `search-access.md`; these are the calls the blueprint
did not prescribe. All are pinned by `GeoSearchTest` and by the five TCK geo cases, which
run now that `GEO_WITHIN`/`GEO_DISTANCE` are declared.

1. **`<` on a distance is served as `<=`**, while `=`/`!=` stay refused. The strict and
   non-strict forms differ only for a point exactly on the radius — below the G5 band the
   vocabulary itself declares, and below Lucene's own encoding resolution. Refusing `<`
   while serving `<=` would be pedantry; refusing `=` is not, because measure-zero has no
   tolerance argument to stand on. *Revisit if* someone shows a case where the boundary is
   observable.
2. **Farthest-first is refused, not emulated.** `newDistanceSort` has no reversed form, and
   the alternative — sorting a computed column — is the per-document arithmetic this
   backend refuses everywhere else. The refusal names the way out (`geoDistance(...) >= r`).
3. **An impossible position fails the write; a missing one does not.** Out-of-range degrees
   are bad data and are refused loudly (Lucene draws the same line); a packed point whose
   `coordinates` does not hold exactly two numbers is UNKNOWN, which is what §5.5 rule 2
   says about a missing coordinate.
4. **A coordinates attribute is not also indexed as a value.** Discovered the hard way: two
   field types under one name is a write-time failure in Lucene. The split pair keeps its
   ordinary numeric fields — those are meaningful scalars on their own.
5. **Rider fix, not geo-specific**: the existence probe behind every negation used
   `FieldExistsQuery` for numeric fields, which reads doc values, norms or vectors — never
   a BKD tree. A *declared* numeric field with `docValues="false"` therefore made any
   negated comparison throw at search time (convention numerics always carry doc values,
   which is why nothing had hit it). The probe is now an unbounded point range for those,
   and the world box for a geo field without doc values. Pinned by a case in
   `ThreeValuedNegationTest`.
6. **EMaps in the TCK binding** (upstream #185, ungated core cases that arrived with the
   2026-08-19 TCK snapshot): an EMap is a containment reference to entry objects, so it maps
   as a NESTED block like any other, and the block reconstruction rebuilds it. The only
   backend-specific part is naming: two entry classes keying on a string and on an int would
   otherwise collide on the field names `key`/`value`, so the binding names them per entry
   class. No product-code change was needed.

## Autonomous calls in fuzzy matching (emf.persistence-jpa#167, 2026-08-19)

The upstream request landed: `StringMatchKind.FUZZY` with `maxEdits`/`prefixLength` and
`QueryFeature.STRING_MATCH_FUZZY`. It arrived as a *compile* error here — both switches over
`StringMatchKind` were exhaustive over four constants — which is the enum-exhaustiveness
guard doing its job. Implemented as planned (`FuzzyQuery` on keyword projections, analyzed
sources refused like the anchored kinds, feature declared); the TCK's
`queryFuzzyMatchingAgreesWithTheMemoryOracle` now runs and passes. Three calls beyond the
plan:

1. **Case-insensitive fuzziness is refused**, not approximated. The other kinds fold case
   with the regexp automaton's `CASE_INSENSITIVE` flag; a fuzzy automaton has no such flag,
   and folding the pattern alone would not fold the indexed terms. The refusal names the
   way out (a lowercasing keyword field). *Alternative if wanted:* fold at index time and
   let the mapping declare it — additive, no IR change.
2. **Constant-score rewrite instead of Lucene's default top-terms one.** The default keeps
   the 50 closest terms and silently drops the rest, which for a predicate means answering
   less than was asked; every other multi-term form here rewrites to constant score too.
   The price is that fuzzy hits do not score by edit distance — acceptable while relevance
   comes from declared rank signals rather than from the predicate.
3. **The budget stays the IR's 1..2**, though Lucene accepts 0. `maxEdits = 0` is an exact
   match spelled the long way, and the upstream validator refuses it before translation; a
   deserialized query that carries it anyway is refused here rather than quietly widened.

## Taken, not yet reviewed

1. **ID_ONLY over an abstract target class is an omission, not an error** — a proxy needs
   an instance; named by `omissions()`, carried as a load warning.
2. **Positional child ids (`root#ref.0`) are not written back** into reconstructed NESTED
   children — they never existed on the original object.
3. **Partiality warnings are per EClass, statically derived**, not per object — per-object
   warnings would flood `getWarnings()` on a type-level load and say nothing new.
4. **`getEObject(fragment)` falls back to the mapping's id attribute** after the intrinsic
   EMF ID lookup — a declared `idFeature` need not be an `iD` attribute.
5. **Non-containment targets inside a STORED_OBJECT blob keep the original's URIs**
   (standard EMF binary rules); a target living in no resource serializes as a dangling
   ref — EMF's own semantics, not re-validated here.
6. **`ObjectSerializers` is an immutable, constructor-filled registry** (the
   `AnalyzerRegistry` pattern); the OSGi whiteboard landed with #32 — `ObjectSerializer`
   services join the binary default in the resource-factory component.
7. **Deserialization uses the caller's package registry** (ResourceSet's when present,
   always plus the unit's own EPackage), never the global registry implicitly — the
   codec's `PackageResolver` stance.
8. **No separate user-docs page for the query path yet** — §5 of the blueprint carries the
   design; a user page comes when the own search API (#41) gives users a surface.
9. **DESC = best-first for a score sort** — Lucene's natural relevance order is what
   descending means for a score-valued key; ASC inverts. Pinned ordinally only.

## Autonomous calls in suggest (S8/#12, 2026-08-18)

10. **Snapshot suggesters, caller-owned rebuild cadence.** *Question:* how fresh is a
    suggester against NRT? *Options:* rebuild per write (the old stack's commit-per-entry
    amplification), rebuild on unit refresh/commit hooks (needs #20's callback surface),
    or explicit `rebuild()` with lazy first build. *Decision:* explicit + lazy — the FSTs
    are snapshots by nature, and a swap is atomic so in-flight lookups never see a
    half-built one. *Revisit when* #20 lands: a commit callback is the natural automatic
    cadence.
11. **COMPLETION and contexts refuse by name** — index-time suggest fields change the
    document shape (mapper work, own follow-up), and the metamodel ties filter contexts
    to exactly that kind. ANALYZING/FUZZY/FREE_TEXT build from documents as they are.
12. **Weights come from data** (the declared attribute's doc values per document) — the
    old stack hardcoded weight 4 into a parameter named numberResults; nothing of that
    API is carried over.

## Autonomous calls in highlighting (S12/#14, 2026-08-18)

1. **Result carrier = §6.1 option (a), as recommended** — `HighlightedHit(object, score,
   highlights)` from `HighlightSearch` in the core bundle. Options (b) projected columns
   and (c) side-channel map stay open as upstream follow-ups if the shared `Hit` envelope
   (persistence #165) ever grows an extension point; nothing here precludes that.
2. **No match, no snippet** — the `UnifiedHighlighter` default returns the field's leading
   text as a summary fallback; that is a summary, not a highlight, so it is switched off
   (`withMaxNoHighlightPassages(0)`) and an unmatched field yields `Optional.empty()`.
3. **Fields are requested as `EAttribute`s** (resolved through the schema like builder
   paths), not by string name — the same type-safety line the rest of the stack draws.
   Rationale: a string name would invent a second naming scheme next to the mapping.
4. **Highlightable = analyzed text + stored original**; keywords and `stored=false` text
   are refused by name with the way out. Term vectors stay an accelerator the highlighter
   picks up on its own — requiring them would make the default mapping unhighlightable.
5. **Default markers `<b>…</b>` are the v1 contract** — formatter/markers become request
   options only when someone needs them; the snippet is one string per field, best
   passages joined in document order (`maxPassages`).
6. **Every mapped unit gets the DS service** (unlike suggest, which needs declared
   sources) — highlightability is per-field and answered at request time, so absence of a
   service would signal nothing.

## Autonomous calls in similarity (S13/#15, 2026-08-18)

1. **The anchor must be indexed** — `MoreLikeThis.like(docNum)` over the corpus's own
   statistics; an unindexed object has none, so it is refused with the way out ("index it
   first") rather than silently answered from re-analyzed live values. A like-from-text
   mode for unindexed objects stays a possible additive follow-up if a use case appears.
2. **Neighbours are same-type only** (anchor's EClass, concrete subtypes included) — the
   type filter every query applies. Cross-type similarity would compare statistics of
   fields that happen to share a name; if wanted later it becomes an explicit request
   option, never the default.
3. **Frequency thresholds default to 1/1**, not Lucene's 2/5 — the API answers on any
   corpus size out of the box; the knobs (`minTermFreq`/`minDocFreq`) are on the request
   for large-corpus precision. Logged because it deviates from the engine default.
4. **Terms must be recoverable**: term vectors or the stored original. `stored=false`
   without vectors is refused naming both ways out; with declared vectors an unstored
   field works (pinned by test) — vectors are the declared fast path of §6.2.
5. **Refactor rider**: the root filter moved to `SearchFields.rootFilter()`, the type
   filter to `IndexSchema.typeFilter(EClass)`, block reconstruction to
   `DocumentReader.blockChildren(...)`, and the mapper grew public `documentId(EObject)` —
   shared by resource, highlight and similarity instead of three private copies;
   `QueryTranslator` stays package-private.
6. **DS races fixed while wiring**: the three per-unit components (suggest, highlight,
   similarity) rebuild their service when a unit is *replaced* (bind-before-unbind order)
   and tear down only when the published unit departs; the wiring test delivers the
   mapping registry before the unit config, because the unit component's static-greedy
   registry reference restarts the unit by design (#19: index order fixed at writer
   creation) — the deployment advice is in the mapping-delivery guide's rebuild note.

## Autonomous calls in the TCK binding (#8, 2026-08-18)

13. **No id generation — the effective id is the id.** An index has no honest counter, and
    post-#37 an unset numeric id *is* its default: the TCK's generation case is overridden
    (the sanctioned Mongo route) to pin what actually happens — save succeeds under id 0,
    nothing is written back, and two id-less objects collide into one document. Callers
    own their ids; the divergence is documented in the override, not hidden.
14. **The TCK unit commits per document with refresh-on-commit** — deliberate suite
    tuning for the read-your-writes the TCK assumes, explicitly marked as what no
    production configuration should copy.
15. **One index, one field type per name** — Person.name keyword forces Company.name
    keyword; Lucene refuses a field analyzed in one document and keyword in the next.
    Worth remembering when mappings grow: the field-name universe is per unit.
