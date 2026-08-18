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
- **Shared named-query catalog** — our factory-attached `EObjectRegistry` is the interim;
  emf.persistence-jpa**#163** asks for the stack-wide contract. *When it lands:* swap the
  lookup to the shared contract.

## Decided upstream, zero change here

- **#161 (query-capability narrowing): refused with reasons** (2026-08-18) — query
  capabilities stay backend-wide by definition; the mapping-dependent truth is
  `validate()`'s job, a mapping-dependent refusal is a Diagnostic with the way out. That
  is exactly what this backend already does; the #114-by-analogy stance is now the stated
  upstream rule. *Revisit trigger (upstream's):* a real pre-validation router would make
  the `EStructuralFeature` overload a purely additive extension.

- **Fuzzy string matching** — requested upstream as emf.persistence-jpa**#167** (new
  `StringMatchKind.FUZZY` with `maxEdits`/`prefixLength`, one new `QueryFeature`).
  *When it lands:* translate to `FuzzyQuery` on keyword projections, refuse analyzed
  sources like the anchored kinds, declare the feature.

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
