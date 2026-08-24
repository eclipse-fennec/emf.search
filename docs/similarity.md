# Similarity

Similarity answers "which objects are like this one" — without embeddings, from the term
statistics of the already-indexed corpus (`MoreLikeThis`). It is the honest baseline any
later vector/KNN search has to beat, and it costs almost nothing: no second index, no
model, no training — the unit's own inverted index is the statistic. Like facets, suggest
and highlighting it is **an own API next to the persistence contract**, because "like
this object" is engine-specific machinery, not query vocabulary. The API is
`SimilaritySearch` in the core bundle `org.eclipse.fennec.search`.

All examples use the [shared catalog model](./getting-started.md#the-example-model).

## The anchor must be indexed

Similarity is a statement about the corpus: which of the anchor's terms are frequent in
the anchor and rare in the rest. An object the index has never seen has no term
statistics, so an unindexed anchor is refused with the way out — index it first. The
anchor is passed as the `EObject` itself; its type and id locate the document.

## Which fields feed the statistics

Analyzed text attributes whose terms are recoverable:

- **declared term vectors** (`TextFieldMapping termVectors="true"`) are read directly —
  the fast path, worth declaring on long fields;
- otherwise the **stored original value** (the default) is re-analyzed.

A keyword (or numeric/boolean/date) field has no term statistics to compare and is
refused by name. A text field mapped `stored="false"` *without* term vectors has no
recoverable terms — the refusal names both ways out: keep the stored value or declare
term vectors.

For the `description` field the example below compares on, **convention is enough**: a
string attribute is analyzed text with its value stored. Declaring term vectors is the
one thing worth adding for long descriptions:

```xml
<documents eClass="https://example.org/catalog#//Product">
  <fields xsi:type="esearch:TextFieldMapping"
      feature="https://example.org/catalog#//Product/description" termVectors="true"/>
</documents>
```

## Finding neighbours

`unit`, `mapping` and the `lucene://` resource the write goes through (`anchorResource`
below) are set up exactly as in [getting started](./getting-started.md). The anchor must
be in the index — and visible, so the write is followed by a refresh:

```java
IndexSchema schema = IndexSchema.of(mapping);

anchorResource.getContents().add(anchorProduct);   // index the anchor first
anchorResource.save(Map.of());
unit.refresh();                                    // make it visible

SimilaritySearch similarity = SimilaritySearch.of(unit, schema);

List<SimilarHit> hits = similarity.search(SimilarityRequest
    .to(anchorProduct)          // the just-indexed EObject
    .field(description)         // the EAttributes whose terms define "similar"; repeatable
    .maxHits(10));              // default 10

for (SimilarHit hit : hits) {
    hit.object();               // the neighbour, reconstructed like every other read
    hit.score();                // its similarity — ordinal, not calibrated
}
```

Hits are objects of the anchor's type (concrete subtypes included), never the anchor
itself, best match first. Scores order the neighbourhood; their absolute values are not a
contract. The hit object is the same partial reconstruction (or materialization) any load
delivers, children of `NESTED` blocks included.

### Frequency thresholds

```java
SimilarityRequest.to(anchor).field(description)
    .minTermFreq(2)     // how often a term must occur in the anchor to count
    .minDocFreq(5)      // in how many documents a term must occur to count
```

Both default to **1**, so similarity answers on any corpus size. Lucene's own defaults
(2/5) assume a corpus large enough that rare terms are noise — raise the thresholds for
precision on large indexes. An anchor whose terms all fall below the thresholds has no
neighbours: an empty list, not an error.

## What is refused

- an unindexed anchor, a request without fields, and any field that cannot feed term
  statistics (above) — refused by name with the way out.

## In OSGi

The thin DS layer publishes one `SimilaritySearch` per configured index unit that has a
mapping, addressable by the unit's alias — same shape as suggest and highlighting:

```java
@Reference(target = "(search.unit.alias=catalog)")
SimilaritySearch similarity;
```
