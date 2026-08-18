# Open decisions — for review

Internal working log, unpublished. Two kinds of entries: **open** (blocked on an upstream
decision, each with its revisit trigger) and **taken but not yet reviewed** (decided
autonomously, pinned by tests, awaiting Mark's eyes). Settled entries are deleted; their
outcomes live in `search-access.md`, the issues, and the code's own documentation.
Cleaned 2026-08-18 after the A/B/C review — everything reviewed there is gone from here.

## Open — blocked on upstream, with revisit triggers

- **TCK binding** (rest of #8) — waits for declarative capability gating,
  emf.persistence-jpa**#160**. *When it lands:* create `search.tck`, bind
  `AbstractPersistenceTCK`.
- **Lazy results / `SERVER_CURSORS`** — we want to hold the searcher until
  `QueryResult.close()` and declare the feature; waits for the `StoreFeature` literal,
  emf.persistence-jpa**#162**. *Until then* results stay materialized, because behaviour
  must not change undeclared.
- **Shared named-query catalog** — our factory-attached `EObjectRegistry` is the interim;
  emf.persistence-jpa**#163** asks for the stack-wide contract. *When it lands:* swap the
  lookup to the shared contract.
- **Converter contract + unexported package** — emf.persistence-jpa**#164** (nullable
  lookup vs. throw; `persistence.converter` not exported). *Until then* the converter is
  an injectable collaborator, null = identity. *When it lands:* consider a constructed
  default again.
- **Score delivery** — emf.persistence-jpa**#165** is decided end to end (2026-08-18):
  bare score key flags only `SCORE`; `Query.withScores` envelope flag; `QueryResult.hits()`
  as the primary per-hit carrier (`Hit` = the extensible envelope our highlighting and
  rank-signal payloads grow into), `scores()` as the derived metadata view, rank order as
  contract; the backend hands `QueryResults.hits(Stream<Hit>, Map<String,Double>)` itself.
  All on one upstream branch, **snapshot publish follows the push**. *When published:*
  (1) implement `withScores` in plan+execution, (2) remove the narrowed `SORT_EXPRESSION`
  declaration, (3) start the TCK binding — one publish unblocks all three.

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

## Tracked gaps (issues exist, nothing to decide)

- The read side still ignores `FieldUse`/`subFields` — #39.
