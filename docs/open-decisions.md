# Open decisions — for review

Internal working log, unpublished. Two kinds of entries: **open** (blocked on an upstream
decision, each with its revisit trigger) and **taken but not yet reviewed** (decided
autonomously, pinned by tests, awaiting Mark's eyes). Settled entries are deleted; their
outcomes live in `search-access.md`, the issues, and the code's own documentation.
Cleaned 2026-08-18 after the A/B/C review and again 2026-08-24 after the full wave-1
review — everything reviewed is gone from here. Review outcomes that changed something on
2026-08-24: the unmapped-type load refuses loudly now (was: empty view), suggest gets an
opt-in commit-driven rebuild (#48), comprehensive docs incl. the README are #47, opt-in
id generation is #49.

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
- **Respect the emf.osgi model fingerprints on deserialization** (deferred by Mark,
  2026-08-24, review of the packages() call — not upstream-blocked). emf.osgi computes a
  model-version fingerprint per EPackage (`FingerprintHelper`, service property
  `emf.fingerprint`); recording it beside a `STORED_OBJECT` write and comparing on read
  would tell "the blob was serialized against a different model version" apart from a
  plain deserialization failure. Complements `IndexSchema.fingerprint()` (S18): that one
  detects a changed *mapping*, this would detect a changed *model*. Left as-is for now;
  *revisit* with the S18 rebuild-detection round, where the unit starts recording
  fingerprints beside its data anyway.
- **bnd plugin-key collision (#36 / emf.persistence-jpa#217)** — `fennecJPA` and
  `fennecPersistence` declare their repository under one plugin key, so enabling both
  silently drops one index. Worked around by not enabling `fennecJPA` here. *Revisit when*
  upstream gives each library its own key — then the workaround note in
  `cnf/ext/fennec.bnd` goes. (#34, the sibling report, is settled: `fennecPersistenceTest`
  exists and this workspace consumes it since 2026-08-24.)

## Decided upstream, zero change here

- **#161 (query-capability narrowing): refused with reasons** (2026-08-18) — query
  capabilities stay backend-wide by definition; the mapping-dependent truth is
  `validate()`'s job, a mapping-dependent refusal is a Diagnostic with the way out. That
  is exactly what this backend already does; the #114-by-analogy stance is now the stated
  upstream rule. *Revisit trigger (upstream's):* a real pre-validation router would make
  the `EStructuralFeature` overload a purely additive extension.
