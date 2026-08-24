# Querying the index

The index speaks the **canonical Fennec query IR** — the same query language as the JPA
and Mongo backends, translated to Lucene by a `QueryProcessor` with `backend=lucene`.
There is no Lucene query API to learn and none is exposed: a consumer writes one query and
decides per deployment which store answers it.

This page is the resource road: `QueryableResource.query(…)` and what comes back. For
scored objects without resource mechanics, see [the direct search API](./search-api.md);
both roads run the identical translation and differ only in what carries the result.

All examples use the [shared catalog model](./getting-started.md#the-example-model).

## Addressing: the `lucene://` URI

A resource is created from a URI of the form `lucene://<unit>/<Type>/<id>` — each segment
narrowing what the resource addresses:

| URI | Addresses |
|---|---|
| `lucene://catalog` | the whole unit |
| `lucene://catalog/Product` | all products — **including subtypes**: a `Bundle` is a `Product`, and an abstract type in the segment is the ordinary polymorphic case |
| `lucene://catalog/Product/p-1` | one object |

A type name the unit does not map is **refused loudly**: the load fails with the
diagnostic on the resource and in the exception — an empty answer would be
indistinguishable from "nothing indexed yet", which would bury a configuration mistake.

`load(…)` populates `getContents()` with what the URI addresses;
`stream(…)` returns the same objects unattached. Both reconstruct through the
[materialization tiers](./materialization.md).

## Running a query

```java
Query query = QueryBuilder.from(productClass)
        .where(Expressions.path(priceAttribute).gt(100.0))
        .orderByDesc(priceAttribute)
        .build();

try (QueryResult result = resource.query(query)) {
    result.objects().forEach(product -> ...);
}
```

**Always close the result** — `QueryResult` is `AutoCloseable`, and closing it also closes
every stream obtained from it. Today's results are materialized per window; the contract
still requires the close, so consumers keep working the day lazy results
(`SERVER_CURSORS`) are declared.

### The three shapes

What the query asks for decides what the result carries — the accessor matching
`result.shape()` is valid, the others throw:

| Query | Shape | Read it with |
|---|---|---|
| plain `from`/`where` | `OBJECTS` | `result.objects()` — reconstructed `EObject`s; `result.hits()` adds scores when the query said `withScores()` |
| `selectAs(…)` columns | `PROJECTION` | `result.rows()` — typed columns, no object reconstruction |
| `countOnly()` | `COUNT` | `result.count()` |
| a group-by pipeline | `AGGREGATION` | `result.rows()` |

### Parameters and options

```java
Query query = QueryBuilder.from(productClass)
        .where(Expressions.path(priceAttribute).gt(Expressions.param("floor")))
        .build();

Map<String, Object> options = Map.of(SearchOptions.RANK_SIGNALS, List.of("views"));

try (QueryResult result = resource.query(query, Map.of("floor", "100"), options)) { ... }
```

- **Parameters** bind `param(…)` placeholders; a bound `ConverterService` converts values
  that arrive as the wrong type (the `"100"` above only matches because it became a
  double).
- **Options** are the backend options of the persistence contract —
  [rank signals](./rank-signals.md) are selected here. An option value of the wrong shape
  is refused, never silently ignored.

### Named queries

A query carrying `saveQuery("expensive")` is deposited in the stack's named-operation
catalog on execution; `resource.query("expensive", parameters, options)` runs it by name.
The catalog is the `NamedOperations` contract — in OSGi a bound service, in plain Java a
constructor argument. Without one, running by name fails, and a `saveQuery` leaves a
warning on the resource naming the query that was not persisted.

## Containment is a block: quantifiers

A `NESTED` reference indexes parent and children as one block, and the IR's quantifiers
run as **index-time block joins** — the one join this backend offers:

```java
// products with at least one 4-star review
QueryBuilder.from(productClass)
        .where(Expressions.any(Expressions.propertyPath(reviewsReference),
                it -> it.path(ratingAttribute).ge(4)))
        .build();
```

This is a *structural* join baked in at write time, not a query-time join over arbitrary
references — the distinction the capability section below is about.

## Capabilities and refusals

The backend **declares** what it serves (`QueryCapabilities` on
`resource.capabilities()`), and every query is validated before translation. A construct
the mapping cannot serve is refused with a `Diagnostic` naming it — never answered
approximately. The refusal travels a fixed contract:

```
IOException  →  cause: QueryException  →  getDiagnostic()
```

plus the same diagnostic on `resource.getErrors()` — so a consumer can tell *"this
mapping does not serve that"* from *"the backend broke"* without parsing message text.

Deliberately refused, with their ways out:

| Refused | Way out |
|---|---|
| Arithmetic in predicates or sorts (`price + 1 > 10`, `SORT_EXPRESSION`, `PROJECTION_EXPRESSION`) | Compute at write time: a [computed field](./computed-fields.md) or a derived feature |
| Query-time joins / paths across documents | Model it into the document: `NESTED` block, `EMBED` copy, or an `ID_ONLY` id comparison |
| Predicates over analyzed text that need exact semantics (equality, anchored matches, fuzzy) | A keyword projection of the attribute — declared as a [sub-field](./getting-started.md#the-mapping) |
| Distance `=`/`≠`, farthest-first sorting | See [geo](./geo.md) |
| A transaction bracket around writes | See [write commands](./write-commands.md) |

The exact capability profile — every declared `QueryFeature`, every gate — lives in the
blueprint (`docs/search-access.md` §5); the TCK holds the backend to it.

## Visibility

An index is **near-real-time**: a committed write becomes visible at the next refresh.
A query after a write sees it only when the unit refreshed in between — configure the
[refresh trigger](./index-units.md) accordingly, or call `unit.refresh()` where
read-your-writes matters. No example on these pages omits that line by accident.
