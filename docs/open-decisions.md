# Open decisions and autonomous calls — for review

Internal working log, unpublished. Each entry is either a decision I took autonomously
(with the rationale and the precedent it leans on) or a genuine open point. Review top to
bottom, delete entries once settled; settled outcomes belong in `search-access.md`.

## Direction notes from Mark, 2026-08-17 (not yet folded into the blueprint)

1. **KNN for ML must be supportable.** Today KNN sits in wave 2 (epic #3), gated on new
   query-IR vocabulary upstream. Mark's separate-APIs directive (below) opens a second
   route: KNN as its **own API next to the persistence contract** — like suggest (§6) —
   which needs no IR change at all and could land earlier. The metamodel slot
   (`VectorFieldMapping`) already exists. *Open:* whether KNN-as-own-API is wave 1.5 or
   stays wave 2; the `EmbeddingProvider` SPI question is unchanged either way.
2. **Everything that does not fit the persistence contract gets its own API** — suggester,
   facets, and by extension similarity/highlighting/KNN. Suggest already follows this
   (§6, own bundle). New consequence for **facets (S7, #11)**: a dedicated facet API is
   the primary surface; the GROUP_BY/AGG_COUNT pipeline subset in the query IR remains
   only where it maps honestly. I will design S7 that way unless objected.

## Autonomous calls in #18 (2026-08-17), with rationale

3. **Eager load, not Mongo's deferred population.** Mongo defers `doLoad` to first
   `getContents()` because its load must diagnose an unreachable database without I/O.
   An embedded index has no such connection problem, and the deferral machinery
   (re-entrancy guards, WrappedException) buys nothing here. Kept: Mongo's "keep already
   attached objects, skip incoming duplicates by id" rule.
4. **Proxy URIs name the declared reference type** (`lucene://unit/<declared type>/<id>`).
   ID_ONLY stores only the id, so a target indexed as a *subclass* will not resolve under
   the declared type's discriminator. Alternatives (store the target's type alongside the
   id) change the index layout; deferred until it hurts. Documented in
   `DocumentReader.proxyFor`.
5. **ID_ONLY over an abstract target class is an omission, not an error** — a proxy needs
   an instance. Named by `DocumentReader.omissions()`, carried as a load warning.
6. **Positional child ids (`root#ref.0`) are not written back** into reconstructed NESTED
   children — they never existed on the original object; the child's real id attribute
   round-trips through its own stored field.
7. **`FieldMapping.stored` default flips to `true` in the metamodel** — §4.3 says opt-out,
   and a non-unsettable boolean cannot express "unset", so the default literal is the only
   place the opt-out semantic can live. Needs one Eclipse generation round.
8. **Warning granularity: one per EClass, statically derived** (`DocumentReader.omissions`),
   not per object. Per-object warnings would flood `getWarnings()` on a type-level load
   and say nothing new.
9. **`getEObject(fragment)` falls back to the mapping's id attribute** after the intrinsic
   EMF ID lookup, because a declared `idFeature` need not be an `iD` attribute — otherwise
   proxies would only resolve for models with intrinsic ids.

## Autonomous calls in the STORED_OBJECT / SOURCE_URI tiers (2026-08-17)

10. **A document written before a materialization declaration is refused** (`MappingException`
    naming the rebuild), never silently served partial. "Declaration = behaviour" is the
    upstream conformance doctrine (§2B of their conformance doc); a declared STORED_OBJECT
    that quietly downgrades would overstate itself exactly the way the TCK's
    `commandCapabilitiesMatchDeclaredBehaviour` exists to catch. Same stance for unreadable
    bytes after a `format` change.
11. **Serialization copies** (`EcoreUtil.copy`) rather than detaching/reattaching the live
    object — a failed serialize must not be able to corrupt the caller's containment tree.
    Costs one copy per materialized write; measurable in the perf suites if it ever matters.
12. **Non-containment targets inside a STORED_OBJECT blob keep the original's URIs**
    (standard EMF external-reference rules of the binary format). A target living in no
    resource serializes as a dangling ref — EMF's own semantics; not re-validated here.
13. **`ObjectSerializers` is an immutable, constructor-filled registry** (same pattern as
    the unit's `AnalyzerRegistry`): plain-Java `withDefaults()`/`of(...)`, the OSGi layer
    later builds one from whiteboard services. No dynamic lookup in the core (§2.2). The
    whiteboard half waits for the OSGi `Resource.Factory` (#32), where mapper construction
    happens.
14. **SearchResource deserializes against the caller's package registry** (ResourceSet
    registry when present, always including the unit's own EPackage), never the global
    `EPackage.Registry.INSTANCE` implicitly — the codec's `PackageResolver` takes the same
    stance upstream.

## Known gaps left deliberately (tracked)

- The read side still ignores `FieldUse`/`subFields` — #39.
- `EMBED` reconstruction is refused by design (§4.3), not a gap.
- The `stored` default flip (true) in the metamodel awaits one Eclipse generation round;
  until then a declared field without an explicit `stored="true"` is not stored.
- `QueryShape.OBJECTS` results through the query path still need the execution half of #8;
  the reader is ready for it.
