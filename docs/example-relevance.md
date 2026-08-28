# Example: relevance from your data

Two products match a query equally well. Which one first? The one your data already
answers for — a view count folded into the score as a declared
[rank signal](./rank-signals.md) — and "more like this" without any vector machinery,
from the corpus's own term statistics ([similarity](./similarity.md)).

Runs in every build:
[`RelevanceExampleTest`](https://github.com/eclipse-fennec/emf.search/blob/snapshot/org.eclipse.fennec.search.examples/test/org/eclipse/fennec/search/examples/RelevanceExampleTest.java)
in `org.eclipse.fennec.search.examples`, over the
[shared catalog model](./getting-started.md#the-example-model).

## The mapping

`views` should stay an ordinary number (comparable, sortable, usable as a suggest weight)
**and** shape the score. That is exactly what the sub-field form is for — the signal rides
beside the numeric projection and is selected under its compound name `views.signal`:

```xml
<fields xsi:type="esearch:NumericFieldMapping"
    feature="https://example.org/catalog#//Product/views" docValues="true">
  <subFields xsi:type="esearch:RankSignalFieldMapping" name="signal"
      function="SATURATION" pivot="500.0"/>
</fields>
```

`SATURATION` with pivot 500: a product with 500 views gets half the maximum contribution,
and no view count — however wild — can bury a better textual match.

## The corpus

| id | name | description | views |
|---|---|---|---|
| p-1 | Espresso Machine | A compact espresso machine for fresh coffee | 800 |
| p-2 | Coffee Grinder | A manual burr grinder for fresh coffee | 120 |
| p-3 | Travel Press | A compact espresso machine for travel | 60 |

## Popular products rank first

The query matches p-1 and p-2 with the same text quality — one word each. The signal
separates them:

```java
Query query = QueryBuilder.from(product)
        .where(path(description).contains("coffee"))
        .build();

List<Hit> ranked = IndexSearch.of(unit, schema).search(query, null,
        Map.of(SearchOptions.RANK_SIGNALS, List.of("views.signal")));

// → Espresso Machine (800 views), then Coffee Grinder (120)
```

The signal **adds score, it never decides what matches**: the Travel Press has the most
views per word of description, but no *coffee* — it stays out of the result whatever the
signal says.

## More like this

No embeddings, no configuration — the anchor's rare terms find its neighbour:

```java
List<SimilarHit> similar = SimilaritySearch.of(unit, schema)
        .search(SimilarityRequest.to(espressoMachine).field(description));

// → Travel Press first: it shares the rare terms (compact, espresso, machine);
//   the grinder shares only the common ones
```

The anchor must be **indexed** — similarity reads the corpus's term statistics, and an
unseen object has none. An unindexed anchor is refused with exactly that answer.

## What to notice

- **The relevance decision is in the mapping**, reviewable and versioned — the query only
  selects it by name. There is no way to send a scoring formula, by design.
- **Sub-fields keep one attribute usable twice**: `views` the number, `views.signal` the
  score shaper. The [search-box example](./example-search-box.md) reads the same `views`
  as its suggest weight.
- **Similarity is the honest baseline** before vectors: when wave 2's KNN lands, this is
  what it has to beat.
