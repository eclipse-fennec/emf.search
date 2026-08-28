# Example: a product search box

The classic: one input field, scored results, a filter sidebar, completions while typing,
highlighted snippets. Four APIs, one index, one mapping.

Everything on this page runs in every build:
[`SearchBoxExampleTest`](https://github.com/eclipse-fennec/emf.search/blob/snapshot/org.eclipse.fennec.search.examples/test/org/eclipse/fennec/search/examples/SearchBoxExampleTest.java)
in the `org.eclipse.fennec.search.examples` project, over the
[shared catalog model](./getting-started.md#the-example-model)
([`catalog.ecore`](https://github.com/eclipse-fennec/emf.search/blob/snapshot/org.eclipse.fennec.search.examples/test/org/eclipse/fennec/search/examples/catalog.ecore))
and its authored mapping
([`catalog.esearch`](https://github.com/eclipse-fennec/emf.search/blob/snapshot/org.eclipse.fennec.search.examples/test/org/eclipse/fennec/search/examples/catalog.esearch)).

## The mapping

The search box needs four declarations beyond convention — everything else (the analyzed
`description`, the numeric `price`) maps on its own:

```xml
<documents eClass="https://example.org/catalog#//Product">
  <!-- name additionally as a sortable keyword beside the analyzed text -->
  <fields xsi:type="esearch:KeywordFieldMapping"
      feature="https://example.org/catalog#//Product/name" docValues="true"/>
  <!-- condition and tags as facet dimensions; tags is many-valued -->
  <fields xsi:type="esearch:KeywordFieldMapping"
      feature="https://example.org/catalog#//Product/condition" docValues="true">
    <facet/>
  </fields>
  <fields xsi:type="esearch:KeywordFieldMapping"
      feature="https://example.org/catalog#//Product/tags">
    <facet multiValued="true"/>
  </fields>
  <!-- suggestions from product names, popular ones first -->
  <suggestions name="names"
      feature="https://example.org/catalog#//Product/name"
      weight="https://example.org/catalog#//Product/views"/>
</documents>
```

## The corpus

Three products, written through the resource road and made visible with one refresh
(see [getting started](./getting-started.md#end-to-end) for the `ResourceSet` wiring):

| id | name | description | condition | views | tags |
|---|---|---|---|---|---|
| p-1 | Espresso Machine | A compact espresso machine with a built-in coffee grinder | NEW | 800 | kitchen, coffee |
| p-2 | Coffee Grinder | A manual burr grinder for filter coffee | USED | 120 | kitchen, coffee |
| p-3 | Electric Kettle | An electric kettle with temperature control | NEW | 40 | kitchen |

```java
unit.refresh();   // an index is near-real-time: ask for visibility, don't hope
```

## One query, four answers

The user typed *coffee*. Everything below shares one canonical query:

```java
Query query = QueryBuilder.from(product)
        .where(path(description).contains("coffee"))
        .build();
IndexSchema schema = IndexSchema.of(mapping);
```

**The hits** — scored plain objects, best first ([direct search API](./search-api.md)):

```java
List<Hit> hits = IndexSearch.of(unit, schema).search(query);
// → Espresso Machine, Coffee Grinder — each with hit.score() > 0
```

**The filter sidebar** — facet counts over the same match set ([facets](./facets.md)):

```java
FacetResults facets = FacetSearch.of(unit, schema)
        .count(FacetRequest.over(query).dimension("condition").dimension("tags"));
// condition → NEW: 1, USED: 1        (the kettle is not in the match set)
// tags      → coffee: 2, kitchen: 2
```

**The completions** — while the user was still typing ([suggest](./suggest.md)):

```java
SuggestSearch.of(unit, schema).suggest("names", "e", 5);
// → "Espresso Machine", "Electric Kettle" — 800 views rank before 40
```

**The snippets** — matches marked in the stored text ([highlighting](./highlighting.md)):

```java
List<HighlightedHit> highlighted = HighlightSearch.of(unit, schema)
        .search(HighlightRequest.over(query).field(description));
// → "…with a built-in <b>coffee</b> grinder", "…for filter <b>coffee</b>"
```

## What to notice

- **One mapping serves all four APIs.** The facet dimensions, the suggest source and the
  keyword projection are declarations on the same `DocumentMapping` the hits come from.
- **The suggest weight is data** (`views`), not a constant — popular products complete
  first, and the ordering changes when the data does.
- **The kettle is nowhere**: it does not match *coffee*, so it is not a hit, not a facet
  count, not a snippet. Facets count the match set, not the corpus.
