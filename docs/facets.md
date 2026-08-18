# Facets

Facets answer "which values occur in my matches, and how often" — the counts behind every
drill-down UI. In `emf.search` they are **an own API next to the persistence contract**,
because counting values over a match set is not query vocabulary; the one aggregation that
*is* honest query vocabulary (group by one key, count) is answered from the same machinery.

## Declaring a dimension

A facet is declared per field in the mapping — the declaration is what makes the mapper
write the doc values the counting reads, so there is no separate "facet index" to keep in
step:

```xml
<documents eClass="...#//Product">
  <fields xsi:type="esearch:KeywordFieldMapping" feature="...#//Product/condition">
    <facet/>                                    <!-- dimension name defaults to the field name -->
  </fields>
  <fields xsi:type="esearch:KeywordFieldMapping" feature="...#//Product/tags">
    <facet multiValued="true"/>
  </fields>
</documents>
```

Counts come from SortedSet doc values inside the unit's own index, so they follow the
unit's commits and refresh policy like every other read. `TAXONOMY` and `hierarchical`
are refused by name for now: a taxonomy brings a side-car index with its own lifecycle,
and an attribute value is a single path component — neither has earned its cost yet.

## Counting

```java
FacetSearch facets = FacetSearch.of(unit, schema);

FacetResults results = facets.count(FacetRequest
    .over(QueryBuilder.from(product).where(path(price).gt(100.0)).build())
    .dimension("condition")
    .dimension("tags")
    .drillDown("condition", "NEW")      // narrows the base like a clicked filter
    .topN(10));

results.dimension("tags").orElseThrow().values();   // value + count, count-descending
```

The base of a request **is** a canonical query, translated by the same processor the query
path uses — predicates, quantifiers and refusals behave identically. Counts count
**objects**: children of a `NESTED` block never inflate a dimension, exactly as they never
count as hits. An undeclared dimension is refused naming the declared ones; an empty index
has empty counts, not an error.

## The group-by subset of the query path

The one pipeline the backend serves through `QueryableResource` is the shape facets answer
exactly — **one group key that carries a facet declaration, one `COUNT`**:

```java
Query query = QueryBuilder.from(product)
    .groupBy(condition)      // the attribute needs a declared facet
    .countOf("n")
    .build();

try (QueryResult result = resource.query(query)) {
    result.rows();           // columns: "condition" (EMF-typed key) and "n", count-descending
}
```

Everything beyond — expression keys, more keys, other aggregates, compute or having
stages — is refused by name: those need a pipeline engine, and pretending the index has
one would answer differently from the primary store. Route them there instead.
