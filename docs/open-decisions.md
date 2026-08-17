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

## Known gaps left deliberately (tracked)

- The read side still ignores `FieldUse`/`subFields` — #39.
- `EMBED` reconstruction is refused by design (§4.3), not a gap.
- STORED_OBJECT (`ObjectSerializer`, EMF Binary) and SOURCE_URI tiers: next steps of #18,
  after the `stored`-default generation round.
