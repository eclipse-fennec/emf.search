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

1. **An unmapped type name is an error diagnostic, and the read stays empty** — the trigger
   this backend gives `provokeLoadDiagnostic()` (emf.persistence-jpa#197). The alternative
   trigger, the partial-reconstruction warning that §4.3 already emits, would have needed
   no product change and was rejected as ducking the point (Mark's call, 2026-08-23).
   Not thrown: a resource that addresses an unknown type is still a legitimate, empty view,
   and the TCK case is only meaningful when the resource comes back.
2. **The URI type segment widens to subtypes** (`typeFilter`, abstract classes included).
   Previously a raw discriminator term — every polymorphic case had gone through a gated
   type predicate, so the URI path had never been asked.
3. **Delete refuses when an `ID_ONLY` reference still points at the object** (#195), by
   probing before deleting. The known limit is the id model, not the probe: `_root` is the
   bare id value, so two types sharing an id share a block identity — the same collision
   the delete itself has. *Revisit if* a unit-wide unique document key ever replaces the
   bare id.
4. **The named-operation catalog is swapped, and the old registry reference survives as a
   fallback** (#203/#163, done 2026-08-23 — the entry that used to sit under "open" is
   settled). `SearchResource` and its factory take a `NamedOperations`; the OSGi component
   prefers a bound service and otherwise wraps the registry it already referenced in
   `RegistryNamedOperations`, so a deployment configured before the contract existed keeps
   working without a second lookup road of our own. Documented in §5.6.
5. **`PROJECTION_EXPRESSION` (#189) stays undeclared** — the projection counterpart of the
   SORT_EXPRESSION refusal. The shared validator refuses it by name; the test pins that
   nobody declares it by accident.

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

## Tracked gaps (issues exist, nothing to decide)

- The read side still ignores `FieldUse`/`subFields` — #39.
