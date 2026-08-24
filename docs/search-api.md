# The direct search API

`IndexSearch` answers a canonical query with **hits as plain objects** — no `Resource`, no
`ResourceSet`, no ownership. It exists because the resource road, correct as it is, carries
EMF mechanics a search consumer often does not want: adding a hit to a resource *moves* it
(containment), a load populates `getContents()`, and everything is addressed through URIs.
A search box needs none of that. It needs: query in, scored objects out.

```java
IndexSearch search = IndexSearch.of(unit, IndexSchema.of(mapping));

List<Hit> hits = search.search(QueryBuilder.from(product)
        .where(path(name()).contains("espresso"))
        .build());

hits.get(0).object();   // a plain EObject, owned by nobody
hits.get(0).score();    // how good the match is — always filled
```

`Hit` is the persistence contract's own carrier (object + score), not a type of this
backend — what a hit carries is decided once, stack-wide.

## When to use which road

| You want | Use |
|---|---|
| Objects for a search box, scored, no resource semantics | **`IndexSearch`** — this page |
| Counts, projections, aggregations | `QueryableResource.query(…)` — its `QueryResult` carries those shapes |
| EMF editing semantics: load into a `ResourceSet`, modify, save back | `PersistenceResource` over the index |
| Facets, suggestions, highlights, similar objects, groups | The sibling APIs of the same family — each has its own page |

`IndexSearch` deliberately answers **only the OBJECTS shape**. A `countOnly` or projected
query is refused by name, pointing at the resource road — one API, one promise.

## The mapping

The example pages of this documentation share one model — a product catalog. The direct
API needs nothing beyond the mapping the features you query already need; for the examples
here, an analyzed name and a numeric price by convention are enough:

```xml
<esearch:IndexUnitMapping xmi:version="2.0" name="catalog"
    ePackage="https://example.org/catalog#/">
  <documents eClass="https://example.org/catalog#//Product"/>
</esearch:IndexUnitMapping>
```

An unmapped attribute maps by convention (strings become analyzed text, numerics become
points with doc values), so the minimal declaration is the class itself. Everything a
query touches must be *reachable* through the mapping — an undeclared construct is refused
with a `Diagnostic` naming it, never answered approximately.

## Getting one

**Plain Java** — collaborators are explicit and attach as immutable copies:

```java
IndexSearch search = IndexSearch.of(unit, IndexSchema.of(mapping))
        .withConverter(converter)        // parameter-value conversion
        .withCatalog(namedOperations)    // named queries
        .withPrimaryStore(primaryStore); // SOURCE_URI resolution, next section
```

**OSGi** — one `IndexSearch` service is published per mapped index unit, keyed by
`search.unit.alias`, exactly like the suggest, highlight, similarity and grouping
services:

```java
@Reference(target = "(search.unit.alias=catalog)")
IndexSearch search;
```

A `ConverterService`, a `NamedOperations` catalog and a `PrimaryStore` service bind
automatically when present — and unlike the sibling services, a collaborator that arrives
*late* republishes the search services. That is deliberate: the ordinary deployment starts
an in-memory index faster than the JPA store next to it.

## Searching

```java
List<Hit> hits = search.search(query);                       // query IR in, hits out
List<Hit> hits = search.search(query, parameters, options);  // with parameters and options
List<Hit> hits = search.search("expensive", parameters, options); // from the catalog
```

- **Parameters** are the IR's `param(…)` values, converted through the attached
  `ConverterService` exactly as on the resource road.
- **Options** are the backend options — [rank signals](./rank-signals.md) selected under
  `SearchOptions.RANK_SIGNALS` work unchanged.
- **Named queries** resolve through the attached `NamedOperations` catalog, and a query
  carrying a `saveQuery` name is deposited there. Without a catalog both are refused —
  the direct API has no warnings channel to note a silently dropped name on.
- **Order and scores:** hits come in rank order, or in the query's declared sort order —
  and the score is filled either way. A sorted search still computes scores, because the
  carrier promises one.
- **Paging** is the query's own `skip`/`top`. The list is materialized per window; a
  lazily paging answer is the `SERVER_CURSORS` story and will be declared when it exists.

## The primary store

The dominant secondary-index deployment writes originals to JPA or Mongo and indexes them
for search. Such a class is mapped with `SOURCE_URI`
[materialization](./materialization.md): the index stores *where the original lives*, not
a reconstruction.

```xml
<documents eClass="https://example.org/catalog#//Product">
  <materialization kind="SOURCE_URI"/>
</documents>
```

On the resource road, resolving those URIs rides on whatever `ResourceSet` the caller
assembled. The direct API makes the collaborator explicit:

```java
PrimaryStore store = uris -> productRepository.findByUris(uris); // one batch per window

List<Hit> hits = IndexSearch.of(unit, schema)
        .withPrimaryStore(store)
        .search(query);   // hits are the originals, straight from the primary store
```

- **Resolution is batched** — one search window is one `resolve(List<URI>)` call, so a
  JPA-backed store answers with one query instead of one per hit.
- **Without a store, a `SOURCE_URI` hit is an EMF proxy** carrying the original's URI —
  exactly what the index knows, resolvable by whoever wants to.
- **A URI the store cannot answer keeps its proxy.** The hit is not dropped: the index
  said there is a match, and hiding it would misreport the search.
- `PrimaryStore.of(resourceSet)` adapts a `ResourceSet` for the simple case — the explicit
  form of what the resource road does implicitly.

The write half of this deployment — save to the primary store, index on success, under a
transaction bracket — is tracked separately (#50); today the feed is the caller's `save`
on the index resource, or the v2 change stream once it exists.

## What it refuses, and why

| Situation | Answer |
|---|---|
| A `countOnly`, projected or aggregated query | Refused: a direct search answers hits, those shapes have none — run them through `QueryableResource` |
| A query without a root type (`from`) | Refused: there is no URI to take one from |
| A construct the mapping cannot serve | Refused with the `Diagnostic` naming it — the same §2B contract as everywhere else |
| A named query without an attached catalog | Refused, naming `withCatalog(…)` as the way out |
| A `saveQuery` name without an attached catalog | Refused: the promise to keep the query cannot be met, and dropping it silently would be worse |

Refusals are `QueryException` — the exception carries the validation `Diagnostic` where
one exists, so a consumer can read *which* construct was refused without parsing message
text.
