# Search access — blueprint for the `emf.search` repository

**Status:** working blueprint (2026-08-05, feature scope extended the same day — §7
feature radar, waves). The repository decision, roles and the persistence-side
prerequisites are settled (discussion 2026-08-05 in `emf.persistence-jpa`); the task
breakdown in §8 is cut as GitHub issues (#1 foundation, #2 wave-1 features, #3 gated/wave 2 —
see the issue number on each task). Companions (all in
`eclipse-fennec/emf.persistence-jpa`, `docs/unified-persistence/`):
`query-ir-redesign.md` (Expression IR, capability discipline),
`query-processor-spi.md` (per-backend translation), `timeseries-access.md` (stream
store — the v2 index feed), `geo-vocabulary.md` (emf.persistence-jpa#101, concept round
pending). Prerequisites landed there: emf.persistence-jpa#99 (TCK as consumable API),
emf.persistence-jpa#100 (SCORE vocabulary).

## 1. Mission

Lucene as a **capability-honest search backend** for the Fennec persistence stack —
replacing the retired `org.gecko.search` architecture, whose pain points define the
anti-goals:

| gecko.search (old) | emf.search (new) |
|---|---|
| consumer hand-builds `IndexContextObject`s per ADD/MODIFY/REMOVE | index maintenance behind the `Resource`/store contract, v2 fed by the change stream |
| EObject→Document mapping hand-coded per use case (`ContextObjectFactory`) | declarative mapping model (`esearch.ecore`), processor pipeline like eorm |
| queries are raw Lucene `Query` objects, results are raw `Document`s | canonical query IR in, EObjects/rows out — `QueryProcessor` SPI, capability-refused where Lucene cannot |
| per-index manual DS wiring (service + analyzer + descriptor) | one whiteboard configuration per index unit (the `JPAUnit`/`mongo.database.alias` pattern) |
| suggest as a parallel second stack | suggest as one module with its own API, sharing model + lifecycle |

Two usage roles, both first-class:

- **Standalone index** ("only a Lucene index, nothing else"): the backend is a
  `QueryableResource`/`PersistenceResource` — documents are saved into and queried from
  the index directly. Honest contract limits: NRT visibility instead of
  read-your-writes transactions, reference contracts largely refused.
- **Secondary index** next to JPA/Mongo (the dominant case): v2, fed by the CHANGELOG
  stream of the timeseries/stream stack (`timeseries-access.md` cut 1) — `append` →
  incremental index update, `replay` → rebuild from scratch; query routing sends
  full-text predicates to Lucene and materializes hits via keyed finds in the primary
  store.

Consumer motivation: OData `$search` (unserved today), model-atlas search, plus every
consumer that needs ranked full-text over EMF models.

## 2. Repository layout

Bnd workspace, same conventions as `emf.persistence-jpa` (`cnf/`, bnd libraries,
reusable CI workflows from `eclipse-fennec/.github`).

**Lucene comes from the OSGi-repackaged `org.geckoprojects.libraries:org.apache.lucene.*`
bundles** built in the `org.gecko.libraries` workspace, pinned in `cnf/ext/central.mvn` at
**10.5.0-SNAPSHOT** (Central snapshots, published 2026-08-05). They supersede the older
`org.geckoprojects.search` set up to 9.11.1 that `org.gecko.search` used.

Bundles available in that workspace: `core` (core + analysis-common),
`analysis.icu/morfologik/opennlp/phonetic`, `backward.codecs`, `codecs`, `classification`,
`expressions`, `facet`, `grouping`, `highlighter`, `join`, `memory`, `misc`, `monitor`,
`queries`, `queryparser`, `spatial` (spatial3d + spatial-extras), `suggest`, `benchmark`.
Wave 1 (§7) needs `core`, `queries`, `queryparser`, `facet`, `suggest`, `highlighter`,
`join`, `grouping` and `memory`; wave 2 adds `analysis.*`, `monitor` and `classification`.
**`spatial` is probably not needed for geo** — `LatLonPoint`/`LatLonDocValuesField`/
`LatLonShape` and polygon queries live in `core`; the `spatial` bundle is only required for
Spatial4j/JTS shapes (WKT parsing) and geo3d, so S9 confirms this before pulling it in.
KNN vector search (wave 2) also needs no extra bundle — it is in `core`.

**Lucene 10 check (done, S3):** the class and method names cited here were written against
the 9.x API and have been verified against 10.5 — `IndexWriter.setLiveCommitData`,
`SearcherManager`, `ControlledRealTimeReopenThread`, `updateDocuments` for blocks,
`FieldExistsQuery`, `LatLonPoint`, `FeatureField`, `LongRange` and `KnnFloatVectorQuery` all
exist. One deprecation to carry: `new MatchAllDocsQuery()` gives way to
`MatchAllDocsQuery.INSTANCE`. Lucene 10's Java 21 baseline matches the workspace.

| Bundle | Content |
|---|---|
| `org.eclipse.fennec.search.model` | `esearch.ecore` — the mapping metamodel (§4), generated EMF code. Plain EMF, no OSGi. |
| `org.eclipse.fennec.search` | the backend **as a plain-Java library**: index lifecycle, mapping processors, `QueryProcessor` (`backend=lucene`), highlighting (§6.1) and similarity (§6.2) — the latter two need the live searcher plus the executed query, so they stay here instead of becoming siblings of `search.suggest`. Constructible from ordinary code, no framework required. |
| `org.eclipse.fennec.search.osgi` | the thin OSGi layer: `Resource.Factory` component, ConfigAdmin factory config per index unit, whiteboard publication (the `mongo.database.alias` pattern), analyzer/provider services |
| `org.eclipse.fennec.search.osgi.tests` | OSGi integration tests (`testOSGi`) — wiring only, see §2.2 |
| `org.eclipse.fennec.search.suggest` (+ `.osgi`, `.osgi.tests`) | suggest/completion as its own API + impl (§6) — deliberately NOT query-IR vocabulary; same plain-core/thin-OSGi split |
| `org.eclipse.fennec.search.index` | v2: stream-fed secondary-index maintenance + query routing (§1) — depends on `persistence.stream`, starts only after its P1 |
| `org.eclipse.fennec.search.tck` | TCK binding: consumes the published `org.eclipse.fennec.persistence.tck` (emf.persistence-jpa#99) + search-specific cases. The TCK is plain JUnit 5, so the binding runs in the normal `test` task. |
| `org.eclipse.fennec.search.workspace.library` | bnd workspace library (`-library: fennecSearch`) publishing these bundles plus their Maven closure, mirroring `fennecUtil`/`fennecPersistence` |

**Why the bundles are not called `…search.lucene`.** There is no index abstraction layer and
none is planned: Lucene is the engine, not one of several. A `.lucene` segment would promise a
choice that does not exist, and a second engine would in any case be a different backend with
its own concept (§9). The core bundle therefore carries the plain name, matching
`org.eclipse.fennec.persistence` in its own group. The one place the engine name stays is the
`QueryProcessor` service property `backend=lucene` — there it is not a bundle name but the
value that distinguishes this backend from `mongo` and `memory` in a namespace shared across
the persistence stack, so it carries real information.

### 2.1 Related repositories

| Repository | Role for `emf.search` |
|---|---|
| `eclipse-fennec/emf.persistence-jpa` | the contracts consumed here (§3) and the home of the design companions listed at the top. Also the reference implementation to imitate: `persistence.orm`'s processor/mapping-context pipeline (§4), the Mongo backend's capability refusals and 3VL negation push-down (§5.1). |
| `eclipse-fennec/emf.osgi` | the EMF-in-OSGi runtime the backend plugs into — `ResourceSet`/`ResourceSetFactory` and `EPackage`/`Resource.Factory` as OSGi services instead of the static EMF registries, plus the EMF codegen used for `esearch.ecore` (S2). Also supplies both mechanisms that carry the mapping instances (§4.1): `emf.osgi.eobject.registry` (named, provider-fed EObject registries with a non-OSGi bootstrap) and `emf.osgi.metadata` (`MetadataService`, `AspectEntry`). |
| `eclipse-fennec/emf.codec` | EMF (de)serialization framework. Two precedents: its `_type` discriminator (`TypeConfig`/`ConfigProperty.TYPE_KEY`, default `_type`) for the type field in §5, and its config-resolution/annotation-scope model for how §4 mapping declarations are attached and overridden. |
| `geckoprojects-org/org.gecko.search` | the retired predecessor whose pain points are the §1 anti-goals. Worth mining, not copying: the Lucene lifecycle mechanics (`LuceneIndexImpl`/`DefaultLuceneIndex` writer + `SearcherManager` NRT handling, `CommitCallback`, the prototype-scoped searcher service factory, the suggest module's `SuggestionService`). What is replaced wholesale: `IndexContextObject`/`ContextObjectFactory` as the consumer-facing API, hand-coded `EObject`→`Document` mapping, raw `Query` in / raw `Document` out. |
| `geckoprojects-org/org.gecko.libraries` | the OSGi-repackaged Lucene bundles (§2). |

### 2.2 Engineering conventions

**OSGi-ready, OSGi-optional.** The stack targets OSGi, but every piece must work without
it. Concretely:

- No `org.osgi.*` import in a core bundle. Index units, analyzer registries, embedding
  providers and mapping registries are configured through plain constructors/builders and
  config records; DS `@Designate`/`@ObjectClassDefinition` types map onto those records in
  the `.osgi` bundle, they do not replace them.
- Every lookup that OSGi answers with a whiteboard needs a non-OSGi answer too — a
  programmatic registry argument, or `ServiceLoader` where a global default is genuinely
  wanted. The precedent is `MetadataServiceFactory.create()` in `emf.osgi`: an explicit
  bootstrap for plain Java next to the service-driven one for OSGi.
- Conversely, plain-Java design must not break dynamics: no static singletons or
  `EPackage.Registry.INSTANCE` reliance that a second index unit or a bundle restart would
  corrupt. State belongs to the unit instance.
- `.osgi` bundles inline their core packages (the emf.util pattern) so a consumer needs
  one bundle at runtime.

**Test strategy: plain JUnit as far as it goes.** Mapping, translation, 3VL negation,
block-join semantics, facets, suggest, highlighting, scoring and the whole TCK binding are
plain JUnit 5 (+ AssertJ, Mockito) against an in-memory `ByteBuffersDirectory` or
`MemoryIndex` — they need no framework and stay fast. `testOSGi` is reserved for what only
a running framework can prove: component activation and configuration, `Resource.Factory`
discovery through a Fennec `ResourceSet`, whiteboard binding of multiple units, service
dynamics on unit removal, and resolvability of the bundle set. A rule of thumb for review:
if an OSGi test asserts *search behaviour* rather than *wiring*, the assertion belongs in
a plain-JUnit test. Note the known `.osgi.tests` gotcha from emf.util — the per-project
`build.gradle` must point `testOSGi` at the resolved bndrun.

**Documentation is part of the task, not a follow-up.** Every issue below delivers
hand-written markdown in `docs/` (the source of truth) and, when the page is user-facing,
an entry in the `docs-site` allowlist so it appears in the published VitePress site. The
emf.util wiring is the template: `docs/*.md` → `docs-site/guides.mjs` (`GUIDES`/
`EXAMPLES`) → `sync-guides.mjs` copies the allowlisted pages before each build and
rewrites links to unpublished internal docs (like this blueprint) to GitHub blob URLs.
Internal design docs stay in `docs/` unpublished — that is a feature, not an omission.

**CI is the reusable Fennec pipeline.** The workflows in `.github/workflows` delegate to
the pinned reusables in `eclipse-fennec/.github` (verify / release / docs / scorecard /
dependency-review); the docs job publishes the VitePress site. Repo-local CI logic is
avoided — if something is missing, it is fixed in the reusable, not forked here.

## 3. Dependency contract with emf.persistence-jpa

One direction only: `emf.search` consumes **published** artifacts (bnd repos / DIM
snapshots; the `fennecPersistence` workspace-library pattern, see the
emf.persistence-jpa README §consuming). Everything below is API there as of 2026-08-05:

- Expression IR + `query.model`, the `QueryProcessor` SPI, `QueryCapabilities`/
  `QueryFeature`, validation (`QueryValidator`, `ExpressionAnalyzer`).
- `PersistenceResource`/`QueryableResource`/`CommandResource` contracts (`persistence`,
  `persistence.query`) and the write vocabulary of `command.model` — `InsertCommand`,
  `DeleteCommand` (selector), `UpdateCommand` (selector + ChangeSet template).
- The TCK as subclass API with bundled model fixtures (emf.persistence-jpa#99): extend
  `AbstractPersistenceTCK`, implement `setUpBackend`/`createBackendResourceSet`/`uriFor`,
  declare variance via the `supports*()` hooks.
- `QueryFeature.SCORE` + the `Score` expression (emf.persistence-jpa#100) — the first
  vocabulary item that exists *for* this repo.
- Geo vocabulary lands there after the emf.persistence-jpa#101 concept round;
  `emf.search` implements it in G-P3 of `geo-vocabulary.md`.

**Ground rule:** missing query vocabulary is never invented in `emf.search` — it goes
to `emf.persistence-jpa` as an IR issue (the SCORE/geo route). Protocol- or
engine-specific machinery (analyzers, suggest, highlighting) stays here.

## 4. The mapping model (`esearch.ecore`) — "eorm for the index"

Declarative EClass→index mapping, processed by a pipeline in the style of
`persistence.orm`'s `Processor`/`MappingContext`. The metamodel exists as of S2 (#5) —
`org.eclipse.fennec.search.model/model/esearch.ecore`, `SearchMappingRegistry` as root,
mirroring the `TrackingRegistry` pattern (reusable definitions in the root, referenced by
non-containment).

**Correction to the first cut:** that draft put refresh policy and commit policy at the
"index level (per EClass)". They cannot live there. One index unit is one Lucene directory
with one `IndexWriter`, so refresh, commit and index sort are physical properties of the
*unit* and cannot be varied per EClass — a per-class declaration would be unenforceable.
The metamodel therefore splits them:

- **`IndexUnitMapping`** (per EPackage universe, = one unit): unit name (the alias joining
  mapping and runtime config), type-discriminator field name (default `_type`), default
  analyzer, refresh policy, commit policy, index sort, `autoMap`.
- **`DocumentMapping`** (per EClass): type name, id feature, analyzer override,
  materialization, fields, references, suggest sources, `autoMap`.

Runtime configuration of the unit — directory location, service wiring — is deliberately
*not* in this model: it is the unit's config object, mapped from a ConfigAdmin factory
config in OSGi and constructed directly in plain Java (§2.2). The model says what documents
look like, not where they live.

- **Field level** (`FieldMapping` per EAttribute): indexed/stored/DocValues/boost/facet on
  the abstract base; tokenization is not a flag but the choice of subclass —
  `TextFieldMapping` (analyzer, term vectors), `KeywordFieldMapping` (normalizer — what
  makes CASE_INSENSITIVE answerable without a wildcard scan), `NumericFieldMapping`,
  `GeoPointFieldMapping`, `RangeFieldMapping`, `RankSignalFieldMapping`,
  `VectorFieldMapping`. Absent a declaration, a convention default applies (id → stored
  keyword, strings → text, numerics → point + DocValues) so small models need no mapping at
  all: a declaration is an override, never a prerequisite.
- **Multiple projections per attribute** (`FieldMapping.subFields`): one attribute usually
  wants more than one index field — analyzed text for relevance *and* an unanalyzed keyword
  for equality, sorting and faceting, or a second analyzer for another language. The
  enclosing mapping is the primary projection and sub-fields hang off it, so
  primary-vs-secondary is structural rather than a flag; a sub-field inherits the attribute
  and its name is relative (`title` + `keyword` → `title.keyword`). Because this makes
  translation ambiguous — which projection answers a given predicate? — `FieldMapping.use`
  (`MATCH`, `EXACT`, `RANGE`, `SORT`, `FACET`, `HIGHLIGHT`, `SIMILARITY`) declares it where
  the field kind alone does not: two projections claiming the same use for one attribute are
  refused at mapping time instead of being resolved by declaration order. Sub-fields
  multiply index size, so they are always declared, never derived.
- **References**: `EMBED` (denormalize the target's mapped fields under a prefix —
  containment-shaped, the Mongo-embedding analogue), `NESTED` (index the target as a
  child document in the parent's block, queried via block join — §5.2, the option that
  keeps per-child predicate correlation that `EMBED` loses) or `ID_ONLY` (store the
  target id; no joins — queries over `ID_ONLY` references are capability-refused exactly
  like Mongo's cross-document paths, diagnostic code analog `CODE_NON_EMBEDDED_PATH`).
- **Rank signals** (`RankSignalFieldMapping`): `FeatureField` with a saturating function
  (saturation/log/sigmoid + pivot) for static popularity/recency signals that shape the
  score without leaking arithmetic into the query IR (§5.3).
- **Interval attributes** (`RangeFieldMapping`): a pair of numeric/temporal features
  declared as one `LongRange`/`DoubleRange` field, so validity intervals get real
  `INTERSECTS`/`WITHIN`/`CONTAINS` semantics instead of two hand-written comparisons.
- **Materialization** (per document): whether the full EObject is stored in the index —
  serialized through `emf.codec`, field name and format declared — so role 1 can return
  complete objects without a primary store. Term vectors are per text field, since that is
  where the cost is.
- **Index ordering** (`IndexSort` on the unit): sort entries over DocValues features,
  enabling early termination for the dominant sort — the time-ordered case of the v2 feed.
  Fixed at index creation, which is why it is declared rather than configured.
- **Suggest sources** (`SuggestSource` per document): name, text feature, weight, contexts,
  suggester kind (§6) — the mapping-model half of S8.
### 4.1 Where mappings live — two existing mechanisms, no third one

The first cut said "registered on the metadata/aspect plane per EPackage" and left the
open point from `timeseries-access.md` O7 (aspect entry vs. standalone XMI artifacts)
unresolved. `emf.osgi` already ships both halves, so `emf.search` builds neither:

- **`org.eclipse.fennec.emf.osgi.eobject.registry`** — named registries of EObjects keyed
  by string, fed by `EObjectProvider`s (`FileEObjectProvider` for file-authored content),
  with change listeners and entries that carry their `source`. An authored `*.esearch`
  document becomes registry content: the provider loads it, the backend looks its
  `IndexUnitMapping` up by unit name, and the listener makes a mapping change observable
  instead of requiring a restart. Crucially for §2.2, it has an explicit non-OSGi
  bootstrap — `EObjectRegistries.createRegistry(name, provider)` — so the plain-Java path
  is the same mechanism, not a parallel one.
- **`AspectEntry` on the metadata plane** (`MetadataService.getPackageAspect(ePackage,
  typeId)` and the class/feature/operation variants) — a `typeId` plus the provider's own
  EObject as contained content, explicitly designed so a provider attaches its model
  without the metadata model knowing the type (the same slot `codec`, `orm` and `history`
  use). A model bundle can therefore ship its own index mapping under
  `typeId = "esearch"`, and the mapping survives writing the metadata tree to an index and
  reading it back.

So: **XMI is the authoring format, the EObject registry is the deployment path, and the
aspect plane is how a model bundle carries its own mapping** — which is exactly the
resolution O7 proposed, now with concrete machinery behind it.

The metamodel stays a self-contained tree (registry → unit → document → fields) rather
than being decomposed into per-element aspect fragments. Two reasons: composite mappings
(`RangeFieldMapping`, `GeoPointFieldMapping`, `VectorFieldMapping`, `SuggestSource`)
reference *several* features and have no single owning element to hang off; and a
self-contained tree can be validated in isolation, which the registry path needs since
nothing there implies an owner. Where a mapping does ride the aspect plane, the explicit
`ePackage`/`eClass`/`feature` references must agree with the element it hangs on — a
validation rule, not a second modelling shape.

Deferred to wave 2 but reserved in the metamodel so it does not become a breaking
change: **`VectorFieldMapping`** (source features, embedding-provider id, dimensions,
similarity function, embedding-model version) — §7. Declaring one is refused with a
diagnostic until the wave-2 work lands; the class exists so that adding the capability
later is additive.

### 4.2 Where a field value comes from (S20, #28)

A field mapping reads one `EAttribute` today. That is the first rung of a ladder the
Fennec stack already uses for ingest mappings (`timeseries-access.md` §6.1), and
`emf.search` adopts the same one, with the same bias — **the weakest sufficient rung**,
because the lower ones are verifiable without running anything:

| Source | Extraction | Verifiable |
|---|---|---|
| feature | `eGet` on one attribute | statically |
| feature path | `eGet` along a chain (`address.city`), including a hop across a reference | statically |
| m2x OCL | `OclEngine.evaluate(expr, OclContext.of(eObject))` | parse + type check at mapping load |

Several sources may feed one field (concatenated for text, multi-valued for keyword), and
a field may have sources but no owning attribute — a **virtual field**, existing only in
the index. OCL is stored as text and parsed with m2x (`OclEngine`, `OclExpressionCache`),
mirroring how `DerivedReferenceCompiler` treats the `derivation` annotation; the workspace
already has m2x via `-library: fennecM2X`, and the engine is a DS service in OSGi and a
constructor call in plain Java, so §2.2 holds both ways.

Three consequences, all owned by #28:

- **Virtual fields are not IR-addressable.** The canonical query IR names features; a field
  with none cannot appear in a canonical query. Virtual fields are for facets, suggest,
  highlighting and as targets of full-text matching. When a computed value must be
  *queryable*, the better carrier is a derived `EStructuralFeature` with the m2x derivation
  annotation — `DerivedReferenceCompiler` compiles it into the IR, `eGet` computes it, and
  the mapper needs no special case. Guidance, not a prohibition.
- **Reading across a reference makes documents stale.** The same exposure `EMBED`/`NESTED`
  carry (§5.2), but hidden in an expression rather than declared. The mitigation is static:
  `OclToExpr` compiles the expression into the Expression IR and `ExpressionAnalyzer` walks
  it for navigated paths, turning an opaque dependency into a declared one — which is what
  S10 needs to keep a stream-fed index from drifting silently (recorded on #22).
- **Changing an expression changes the index**, so a mapping is interpretation-relevant
  metadata: a changed source means a rebuild, and the mapping needs a version the index can
  record.

## 5. Query translation — capability profile

`QueryProcessor` with `backend=lucene`, IR → Lucene `Query`:

| Declared | Translation |
|---|---|
| WHERE_EQ / IN | `TermQuery` / `TermInSetQuery` (keyword fields), point queries (numerics) |
| WHERE_COMPARISON / WHERE_RANGE | point range queries; DocValues where unindexed |
| WHERE_STRING_MATCH (+CASE_INSENSITIVE) | contains/startsWith/endsWith → wildcard/prefix/regexp on keyword fields; analyzed match on text fields; LIKE → `RegexpQuery` via the shared like→regex translation |
| IS_NULL | `FieldExistsQuery` (negated for isNull) |
| LOGICAL_AND/OR/NOT | `BooleanQuery`; **NOT via negation push-down, not bare MUST_NOT** (§5.1) |
| SORT / LIMIT / SKIP | `Sort` over DocValues; `searchAfter`/`TopDocs` paging |
| SCORE (emf.persistence-jpa#100) | relevance sort (`Sort.RELEVANCE`) and projected score column |
| COUNT | `IndexSearcher.count` |
| GROUP_BY subset + AGG_COUNT | facets (taxonomy or SSDV) — declared only for the shapes facets actually answer (single group key, count aggregate); everything else refused |
| GROUP_BY with representative rows | `lucene-grouping` (`GroupingSearch`) — the shape facets cannot answer: top-N documents *per* group rather than counts |
| EXISTS / FOR_ALL over `NESTED` references | block join (§5.2) — `ToParentBlockJoinQuery` for EXISTS, negation-of-EXISTS for FOR_ALL; still refused over `EMBED` (no per-child correlation) and `ID_ONLY` |
| Geo predicates (emf.persistence-jpa#101) | `LatLonPoint`/`LatLonShape` queries from `core` — distance, bbox, polygon; sort by distance via `LatLonDocValuesField#newDistanceSort` |
| Interval predicates over declared range fields | `LongRange`/`DoubleRange` queries (`INTERSECTS`/`WITHIN`/`CONTAINS`) |
| TYPE_CHECK / TYPE_FILTER | type discriminator field (the codec `_type` analogue, written by the mapper) |

Refused (capability, not error): EXISTS/FOR_ALL over `EMBED`/`ID_ONLY` references,
FIELD_TO_FIELD, ARITHMETIC/functions pushdown, PIPELINE beyond the facet/grouping subset,
EXPAND, and joins other than the index-time block join of §5.2 (no term joins across
index units, no equi-joins). The refusals are the honesty of the backend — consumers
route those to the primary store (role 2) or restructure.

### 5.1 The 3VL lesson carries over

Lucene's `MUST_NOT` is two-valued and matches documents where the field is missing —
exactly Mongo's `$nor`/`$ne` situation (emf.persistence-jpa#97). The same recipe
applies verbatim: negation push-down (De Morgan, operator inversion),
`FieldExistsQuery` as the non-null guard on negated comparisons/IN/matches,
null-poisoned comparisons never match, negated or not. Quantifier duality was n/a in the
first cut and becomes relevant with §5.2: over `NESTED` references, ¬∃ ↔ ∀¬ has to be
handled at the block-join boundary — a parent with *no* matching child and a parent with
no children at all are different answers, and `ToParentBlockJoinQuery` inside a
`MUST_NOT` conflates them unless the parent filter guards it. The TCK cases pinning the
scalar part (`queryNotOverNullableComparisonExcludesNullRows`,
`queryNegationDistributesThreeValuedOverJunctions`) run against the binding via the
published TCK — they are the acceptance test for this section; the nested-quantifier
cases are S11's own.

### 5.2 Block joins — the one join that is honest

`lucene-join`'s block join is not a query-time join: parent and children are written as
one contiguous document block, so the "join" is an index-time fact. That is exactly the
shape of EMF containment, which is why `NESTED` (§4) is worth having next to `EMBED`:
`EMBED` flattens a multi-valued target into parallel field values and therefore loses
correlation (a query for `child.a = 1 AND child.b = 2` matches an object whose *different*
children satisfy the two predicates); `NESTED` keeps it. Consequences to accept
deliberately:

- **Atomicity**: a block is only replaceable as a whole. Any change to one child
  reindexes the parent and all its children (`IndexWriter#updateDocuments`). For
  containment that is defensible — the parent owns the children anyway — but it makes
  partial updates impossible and interacts with the v2 stream feed (a CHANGELOG entry for
  one child becomes a parent-scoped reindex).
- **Ordering**: children must precede the parent in the block, and the parent filter must
  be a reliable `BitSetProducer`; the mapper owns both invariants.
- **Scope**: only containment. Non-containment references stay `ID_ONLY`/refused —
  cross-block joins would reintroduce exactly the query-time join this section is not
  offering.

Because it changes the document *shape*, the `NESTED` decision belongs in S4 even though
the query side lands in S11 — retrofitting blocks onto a flat index is a reindex of
everything.

### 5.3 Score shaping without arithmetic pushdown

Static rank signals (popularity, recency, a curated boost) are declared in the mapping
model as `FeatureField`s and applied via `FeatureField#newSaturationQuery`/
`newLogQuery`. This deliberately keeps the ARITHMETIC refusal intact: the consumer never
expresses a scoring formula in the IR, it selects declared signals. `lucene-expressions`
(JavaScript compiled over DocValues) would be the general escape hatch and is **not**
part of the plan — it is a code-execution surface driven by query input, and it would
re-open the door the refusal closes.

### 5.4 Write commands

Commands, expressions and the query API are one intermediate layer: the same write
vocabulary has to work over JPA, Mongo and Lucene, and a backend that cannot honour part of
it says so through capabilities rather than through a surprise at runtime. `execute(Command)`
is therefore not an afterthought next to `Resource.save()` — it is how the layer expresses
writes that are addressed by a *selector* rather than by an object.

| Command | Over Lucene |
|---|---|
| `InsertCommand` | map each payload object, `addDocument`/`updateDocument` on its id term |
| `DeleteCommand` | translate the selector with the `QueryProcessor`, then `deleteDocuments(Query)` — Lucene deletes by query natively, so this maps cleanly |
| `UpdateCommand` | **conditional**: possible only where the document mapping declares materialization (§4, S16) |

The update case is the interesting one and it is not a matter of effort. Lucene has no
partial update: changing one field means rewriting the document, so the backend must be able
to reconstruct it first. With the stored EObject present it can — read, apply the ChangeSet,
re-map, replace. Without it, the index holds only the mapped fields, and rebuilding from
those would silently drop everything unmapped. A lossy write is worse than a refusal, so the
answer without materialization is a refusal (S21, #29).

A write bracket (`CommandResource.begin()`) has no clean Lucene equivalent either.
`IndexWriter.rollback()` discards *all* uncommitted work in the unit, not the calling
thread's share of it, so a bracket is only sound while a single writer owns the unit —
a condition the backend cannot enforce by itself. The v1 recommendation is to refuse and say
why, with a serialized bracket as the documented upgrade path (S22, #30).

**Both of those refusals currently have nowhere to be declared.** `QueryFeature` is
query-side only; there is no command vocabulary in the capability model, so an unsupported
command can only throw. That is a gap in the intermediate layer, not in this backend, and it
is filed as #31 here and raised upstream as **emf.persistence-jpa#114** — including the
sharper question the materialization case forces: whether a capability answer may be *per
EClass* rather than per backend.

## 6. Suggest — own API, shared machinery

Suggest/completion (Lucene `suggest` module: analyzing/fuzzy suggesters, weighted
completion fields) is **not** query-IR vocabulary — it is its own small service API in
`search.suggest`: suggestion sources declared in the mapping model (field + weight +
context), built from the same index lifecycle, exposed as a DS service per index unit.
The old stack's separate-suggest-stack mistake is avoided by sharing the mapping model
and lifecycle, not by forcing suggest through the query IR.

### 6.1 Highlighting

Same reasoning, different coupling: the `UnifiedHighlighter` needs the executed query and
the live searcher, so highlighting lives inside the search core rather than in a sibling
bundle — but it gets its own small API instead of being pushed into the query IR.

The open contract question is where highlights *go*. The result contract is "EObjects or
rows out", and an EObject has no slot for per-hit passages. Three options, to be decided
in S12: (a) a search-local result type that carries `EObject` + score + highlights,
returned by a search-core-specific entry point; (b) highlights as projected columns in
the row shape, which needs per-hit metadata in the IR result model — an
`emf.persistence-jpa` issue; (c) a side-channel map keyed by object id. Option (a) is the
default recommendation: it keeps the ground rule of §3 intact (no search-only vocabulary
in the shared IR) and mirrors what suggest already does.

### 6.2 Similarity (`MoreLikeThis`)

"Objects similar to this one" without embeddings — `lucene-queries`' `MoreLikeThis` over
term statistics of the already-indexed corpus. Exposed as a search-local API for the same
reason as 6.1, and worth having *before* vectors: it is the honest baseline that makes the
wave-2 KNN work measurable, and it costs almost nothing beyond declaring which fields
carry term vectors (§4).

## 7. Feature radar

The Lucene surface beyond §5, split into the waves agreed on 2026-08-05. "Needs IR" marks
what cannot be built here alone because it requires query vocabulary in
`emf.persistence-jpa` (§3 ground rule).

**Wave 1** — part of the v1 line, tasks in §8:

| Feature | Bundle | Needs IR? | Note |
|---|---|---|---|
| Block join over containment | `join` | no — uses existing EXISTS/FOR_ALL | §5.2; document-shape decision belongs in S4 |
| Geo predicates + distance sort | `core` | **yes** — emf.persistence-jpa#101, concept round still pending | now a wave-1 blocker, not a late add-on (§8) |
| Facets | `facet` | no — GROUP_BY/AGG_COUNT subset | taxonomy vs. SSDV decision in S7 |
| Suggest | `suggest` | no — own API (§6) | |
| Highlighting | `highlighter` | depends on the result-carrier decision (§6.1) | |
| Grouping with representatives | `grouping` | likely — "top-N per group" is a result shape, not a predicate | clarify against the pipeline vocabulary before implementing |
| `MoreLikeThis` | `queries` | no — own API (§6.2) | |
| `FeatureField` rank signals | `queries` | no — declared in the mapping model | §5.3 |
| Interval/range fields | `core` | **yes** — interval semantics are new vocabulary | fallback without IR: two scalar comparisons, correct but slower and less expressive |
| Stored EObject via `emf.codec` | `core` | no | makes role 1 self-sufficient |
| Index sorting + early termination | `core` | no | pairs with the existing `searchAfter` paging |
| Live commit data (checkpointing) | `core` | no | the stream offset lives in the Lucene commit — the mechanism S10 needs to resume honestly |
| `MemoryIndex` test harness | `memory` | no | single-document index; folded into S4's definition of done rather than its own issue |

**Wave 2** — reserved, not scheduled:

| Feature | Why it waits |
|---|---|
| KNN / vector search + hybrid retrieval | Biggest single change: needs an `EmbeddingProvider` SPI (indexing becomes slow and fallible), new IR vocabulary for similarity search with a k and a prefilter, and a decision whether the IR carries a vector or a text to embed. KNN is a top-k retriever, not a predicate — it does not compose with `BooleanQuery` the way `WHERE_EQ` does, and the capability must say so. Practical constraints: the codec's default dimension limit (verify against Lucene 10 — a custom `KnnVectorsFormat`/codec is needed for embedding models above it), and an embedding-model change is a full reindex, so the model version is index metadata. The metamodel slot is reserved in §4 so this stays additive. RAG shape, if it comes: chunk-as-child-document + block join (§5.2) for parent rollup, plus hand-rolled fusion of BM25 and KNN unless Lucene 10 ships something for it. |
| Standing queries / reverse search (`monitor`) | Structurally the best fit in the whole list — a registered query is an Expression IR EObject, so it is persistable, versionable and transportable over the typed-event/pushstream stack — but it only pays off together with the v2 change feed (S10), and it is a second execution model (documents pass queries) that deserves its own concept round. |
| Analysis-chain enrichment (`analysis.icu`, `.phonetic`, `.opennlp`, synonym graphs) | Index-time NLP (NER into facet fields, lemmatization) and phonetic matching are per-domain decisions, not backend decisions. Attractive Fennec angle for later: maintain the thesaurus/synonym set *as an EMF model* on the same registry plane as the mapping model. |
| `classification` | Auto-tagging from an existing index; nearly free once the index exists, but no consumer asks for it yet. |

Explicitly out for now: **`lucene-replicator`** (multi-node read replicas) and
`lucene-expressions` (§5.3).

## 8. Task breakdown (proposed issue set)

Issue-sized in the spirit of the emf.persistence-jpa #76–#84 wave. S1–S10 keep the IDs
from the first cut; S11–S19 are the wave-1 additions from §7.

**Definition of done, for every task** (§2.2): plain-JUnit coverage of the behaviour;
`testOSGi` only where the assertion is about wiring; a markdown page in `docs/` plus its
`docs-site` allowlist entry when the page is user-facing; green reusable CI.

**Foundation (strictly sequential — each needs the previous):**

1. **S1 (#4) — workspace bootstrap**: bnd workspace (cnf, libraries, `fennecPersistence`
   consumption, Lucene OSGi bundles), CI via the reusable workflows, license/dash setup,
   and the `docs-site` scaffolding (VitePress + `guides.mjs` allowlist + `sync-guides.mjs`,
   the emf.util wiring) so every later task has somewhere to publish. Also replaces the
   `emf.util` leftovers in `docs/`.
2. **S2 (#5) — `esearch.ecore` + codegen** (`search.model`): the §4 metamodel — including
   `NESTED`, rank signals, interval attributes, materialization and index-sort
   declarations, and the *reserved* (unimplemented) vector-field slot — genmodel,
   conventions documented.
3. **S3 (#6) — index lifecycle** (`org.eclipse.fennec.search`): unit configuration (directory path,
   analyzer registry) as DS factory config, `IndexWriter`/`SearcherManager` NRT
   lifecycle, whiteboard publication per unit (the `mongo.database.alias` pattern).
4. **S4 (#7) — mapping processors + `Resource.Factory`**: EObject→Document via the §4 model
   (processor pipeline), save/delete/load-by-id through the `PersistenceResource`
   contract, honest contract notes (NRT visibility, id required). **Carries the block
   decision of §5.2** — the document shape must accommodate parent/child blocks even
   though the query side is S11. `MemoryIndex`-based mapping tests are part of the
   definition of done.
5. **S5 (#8) — `QueryProcessor` + TCK binding**: §5 translation including the 3VL negation
   push-down, capability declaration, `search.tck` binding extending the published
   `AbstractPersistenceTCK` with the `supports*()` variance + search-specific cases.
6. **S11 (#9) — block join over containment** (§5.2): `NESTED` mapping, block writes
   (`updateDocuments`), `ToParentBlockJoinQuery` for EXISTS/FOR_ALL, capability upgrade
   from refused to supported-for-containment, reindex semantics documented. Prioritised
   directly after S5 because it changes the index, not just the translation.

**Wave 1, parallelizable after S5 (S11 first where it touches the document shape):**

7. **S6 (#10) — SCORE**: relevance sort + projected score (emf.persistence-jpa#100
   vocabulary), ordinal conformance cases (higher score sorts first on constructed
   corpora).
8. **S7 (#11) — facets**: the GROUP_BY/AGG_COUNT subset of §5, taxonomy vs. SSDV decision.
9. **S8 (#12) — suggest** (`search.suggest`): §6 API + mapping-model extension + impl.
10. **S9 (#13) — geo**: `GeoWithin`/`GeoDistance` over `LatLonPoint`/`LatLonShape` from `core`
    plus distance sort — G-P3 of `geo-vocabulary.md`. **Now a wave-1 item, so
    emf.persistence-jpa#101 becomes a wave-1 blocker**: the concept round there has to be
    scheduled, not awaited. S9 also confirms whether the `spatial` bundle is needed at all
    (§2).
11. **S12 (#14) — highlighting**: `UnifiedHighlighter` + the result-carrier decision of §6.1.
12. **S13 (#15) — similarity**: `MoreLikeThis` API (§6.2), term-vector declaration in the
    mapping model.
13. **S14 (#16) — rank signals**: `FeatureField` declaration and saturation/log queries (§5.3).
14. **S15 (#17) — interval fields**: `LongRange`/`DoubleRange` mapping and
    `INTERSECTS`/`WITHIN`/`CONTAINS` translation. **Blocked on a new IR issue** for
    interval vocabulary in `emf.persistence-jpa` — to be raised now; the two-scalar
    fallback ships meanwhile.
15. **S16 (#18) — self-sufficient hits**: full EObject stored in the index via `emf.codec`, so
    role 1 materializes without a primary store.
16. **S17 (#19) — sorted index**: `setIndexSort` for the dominant sort order, early
    termination, `searchAfter` paging hardened against it.
17. **S18 (#20) — checkpointing**: `IndexWriter#setLiveCommitData` carrying the applied change
    offset; recovery/resume semantics tested. Prerequisite for an honest S10.
18. **S19 (#21) — grouping with representatives**: `GroupingSearch` for top-N per group.
    Needs the result-shape question of §7 answered against the pipeline vocabulary first.
19. **S21 (#29) — write commands**: `CommandResource` — insert, delete-by-selector, and
    update where materialization allows it (§5.4).
20. **S22 (#30) — write bracket**: what `CommandTransaction` means over Lucene; refusal
    recommended for v1 (§5.4).
21. **#31 — command capabilities** (upstream): the capability model has no write
    vocabulary; to be raised in `emf.persistence-jpa`, with the per-EClass question.
22. **S20 (#28) — computed field values** (§4.2): feature paths and m2x OCL sources as
    field values, virtual fields, static dependency extraction. Starts after S5, and its
    dependency output is what S10 needs (#22).

**Gated (starts when its prerequisite lands):**

19. **S10 (#22) — v2 secondary index** (`search.index`, after `timeseries-access.md` P1):
    stream-fed maintenance (append→update, replay→rebuild), query routing full-text →
    Lucene → keyed finds, consistency notes (index lag is visible and documented). Builds
    on S18 for resume and interacts with S11's parent-scoped reindex.

**Wave 2** (§7, reserved — no issues cut yet): KNN/vector search + hybrid retrieval,
standing queries via `monitor`, analysis-chain enrichment, `classification`.

Issues to raise in `emf.persistence-jpa` for wave 1: interval vocabulary (S15), the
result shape for grouping representatives (S19), possibly per-hit metadata if §6.1
lands on option (b) — plus the escalation of #101 for S9.

## 9. Non-goals

- No Elasticsearch/OpenSearch backend — this is embedded Lucene; a remote search
  engine would be a different backend with its own concept.
- No query-IR forks or search-only vocabulary in this repository (§3 ground rule).
- No transactional guarantees beyond Lucene's commit semantics — the standalone role
  documents NRT visibility instead of pretending otherwise.
- v1 indexes a single EPackage universe per unit; cross-unit federation is out of scope.
- **No replication.** Multi-node read replicas (`lucene-replicator`) stay out for now —
  a single index unit is owned by a single writer. This also keeps the bundle out of the
  `org.gecko.libraries` set, where it does not currently exist.
- No query-time joins beyond the index-time block join of §5.2, and no scoring formulas
  from query input (`lucene-expressions`, §5.3).
- No embedding computation in this repository even in wave 2 — vectors come from an
  `EmbeddingProvider` service, they are not produced here.
