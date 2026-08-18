# Open decisions and autonomous calls — for review

Internal working log, unpublished. Each entry is either a decision I took autonomously
(with the rationale and the precedent it leans on) or a genuine open point. Review top to
bottom, delete entries once settled; settled outcomes belong in `search-access.md`.

## Direction notes from Mark — settled 2026-08-18, folded into the blueprint

1. ~~KNN~~ → **decided**: own API, no IR change, sequenced after the wave-1 core — issue
   #40, blueprint §7/§8 updated.
2. ~~Separate APIs for what does not fit the persistence contract~~ → **decided**: facets
   (S7, #11) get an own API as the primary surface, the honest IR subset stays — blueprint
   §8 item 9 updated. The TCK-gating blocker is filed upstream as emf.persistence-jpa#160.

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

## Autonomous calls in the block join (S11/#9, 2026-08-18)

15. **Quantifier semantics are the Mongo backend's, verbatim** — the four faces reduce to
    two shapes with the inner predicate translated under the same negation flag: EXISTS /
    ¬FOR_ALL = one `ToParentBlockJoinQuery` over matching children; FOR_ALL / ¬EXISTS =
    root filter minus a block join over the *escaping* children. UNKNOWN children escape a
    FOR_ALL and block a ¬EXISTS, childless parents pass both — pinned by tests mirroring
    the TCK's `queryForAllIsVacuouslyTrueOnEmpty`.
16. **One shared static `QueryBitSetProducer` over the root marker** — it caches per leaf
    reader (weakly), and the root-marker query is identical across units, so per-translator
    instances would only fragment the cache.
17. **Refused, each by name:** quantifier source navigating >1 step (cross-document),
    quantifier over an attribute (COLLECTION_COUNT territory, undeclared), quantifier
    inside a quantifier (a block is one level deep — the mapper indexes a child's
    attributes, not its references), correlated paths back to the root from child scope,
    and paths bound to a foreign iterator variable. `EXISTS`/`FOR_ALL` are *declared*
    backend-wide although they only work over NESTED — same stance as `UPDATE_BY_SELECTOR`
    per-EClass narrowing upstream (§5.4): a narrowed feature still counts as supported, and
    the narrower case refuses by name in validate/translate.
18. **Docs**: §5.2 of the blueprint already carries the design and reindex semantics; no
    separate user page until the query path itself is user-facing (QueryableResource).

## Autonomous calls in the query execution half (#8, 2026-08-18)

19. **Results are materialized inside the searcher lease** — a `QueryResult`'s streams
    outlive the searcher, so the window is collected first. A lazily paging cursor would
    be the `SERVER_CURSORS` store feature, which this backend deliberately does not
    declare (upstream names it as an expected `StoreFeature` neighbour).
20. **The persisted-query catalog is a document convention** (`_qname` term + `_qxmi`
    stored XMI, replaced by name), the Lucene analogue of Mongo's `fennec.queries`
    collection. Catalog documents carry no root marker, so every plan's root filter keeps
    them invisible. `saveNamedQuery` refreshes the unit once — a persisted query promises
    read-your-writes by name, which the unit's refresh policy alone would not.
21. **No `ConverterService` is passed into the query context** (null) — nothing in this
    repo provides one yet; parameters convert through `ExpressionValues`' defaults. Wire
    it up when the OSGi layer (#32) has a service to inject.
22. **`storedField` projection gate updated to the §4.3 default**: only an explicit
    `stored=false` makes a field unprojectable — the pre-flip rule ("only ids are stored
    by convention") lived in the processor and had to move with the convention.

## Known gaps left deliberately (tracked)

- The read side still ignores `FieldUse`/`subFields` — #39.
- `EMBED` reconstruction is refused by design (§4.3), not a gap.
- The `stored` default flip (true) in the metamodel awaits one Eclipse generation round;
  until then a declared field without an explicit `stored="true"` is not stored.
- `QueryShape.OBJECTS` results through the query path still need the execution half of #8;
  the reader is ready for it.
