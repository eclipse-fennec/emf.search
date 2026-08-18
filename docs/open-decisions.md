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
- **Query-capability narrowing** — emf.persistence-jpa**#161** (does the query side need
  `supports(feature, …)` like the command side?). *When it lands:* express the
  EXISTS/FOR_ALL-over-NESTED and equality-on-keyword narrowings through it.
- **Score reclassification and score delivery** — emf.persistence-jpa**#165**, moving:
  part 1 (bare score key flags only `SCORE`) is implemented upstream, pending push —
  *when published:* remove the narrowed `SORT_EXPRESSION` declaration. Part 2 decided as
  a `Query.withScores` envelope flag delivered on the `QueryResult`; we answered the open
  shape question with **hit-wrapper-first** (highlighting #14, similarity #15 and the #41
  hit carrier are the same per-hit form) and asked for the rank-order contract sentence.
  *When the flag lands:* implement `withScores` in translate/execute. The projected
  score *column* is deferred upstream until a row-shape consumer pulls it.

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

## Tracked gaps (issues exist, nothing to decide)

- The read side still ignores `FieldUse`/`subFields` — #39.
