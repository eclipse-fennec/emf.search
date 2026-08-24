# Highlighting

Highlighting answers "*where* did my query match" — the passages behind every result list
that shows a snippet with the search terms marked. In `emf.search` it is **an own API next
to the persistence contract**: the result contract of `QueryableResource` is "EObjects or
rows out", and an EObject has no slot for per-hit passages. Snippets ride on a
search-local hit type instead, keeping the shared query IR free of search-only vocabulary
— the same reasoning as facets and suggest. Unlike suggest, the API lives in the core
bundle — `HighlightSearch` in `org.eclipse.fennec.search` — because the
`UnifiedHighlighter` needs the executed Lucene query and the unit's live searcher, which
only the core has.

All examples use the [shared catalog model](./getting-started.md#the-example-model).

## What is highlightable

A field is highlightable when it is **analyzed text whose original value is stored** —
which is the default for a string attribute, both by convention and under an explicit
`TextFieldMapping`. The highlighter re-reads the stored value and fragments it around the
query's terms. Everything else is refused by name with the way out:

- a **keyword** (or numeric/boolean/date) field carries no token positions to fragment
  around — map the attribute as analyzed text if you want snippets from it;
- a text field mapped **`stored="false"`** has no value to fragment — remove the opt-out.

Declared term vectors (`TextFieldMapping termVectors="true"`) make highlighting faster on
long fields, because the highlighter reads positions and offsets instead of re-analyzing
the stored value. They are never required — an accelerator, not a prerequisite.

For the `description` the example below highlights, **convention already gives all of
this**: a string attribute is analyzed text with its original value stored, no declaration
needed. The explicit form only exists to add the accelerator:

```xml
<documents eClass="https://example.org/catalog#//Product">
  <fields xsi:type="esearch:TextFieldMapping"
      feature="https://example.org/catalog#//Product/description" termVectors="true"/>
</documents>
```

## Requesting snippets

`unit` and `mapping` are set up exactly as in [getting started](./getting-started.md) —
this page only adds the request:

```java
IndexSchema schema = IndexSchema.of(mapping);
HighlightSearch highlights = HighlightSearch.of(unit, schema);

List<HighlightedHit> hits = highlights.search(HighlightRequest
    .over(QueryBuilder.from(product)                       // the Product EClass
        .where(path(description).contains("coffee"))       // its description EAttribute
        .build())
    .field(description)          // the EAttribute to highlight; repeatable
    .maxPassages(2));            // passages joined into one snippet, default 1

for (HighlightedHit hit : hits) {
    hit.object();                        // the hit, reconstructed like every other read
    hit.score();                         // its relevance
    hit.highlight("description");        // Optional<String> — "… <b>coffee</b> …"
}
```

The request's base **is** a canonical query, translated by the same processor the query
path uses — predicates, quantifiers, sort, `skip`/`top` and refusals behave identically,
and the hits come back in the query's order (rank order unless the query sorts). The hit
object is the same partial reconstruction (or materialization) any load delivers,
children of `NESTED` blocks included.

Snippet contract:

- matched terms are wrapped in `<b>…</b>` (Lucene's default markers);
- a field the query did **not** match in gets **no snippet** (`Optional.empty()`), never
  the field's leading text — no match means no highlight;
- with `maxPassages > 1` the best-scoring passages are joined in document order; the
  single default passage is the *best* one, not necessarily the first.

## What is refused

Refusals name the problem and the way out, as everywhere in this backend:

- a request without fields, and any field that is not analyzed stored text (above);
- a query whose shape is not objects — a `COUNT`, projection or pipeline result has no
  hits to carry passages; drop the projection or pipeline from the query.

## In OSGi

The thin DS layer publishes one `HighlightSearch` per configured index unit that has a
mapping, addressable by the unit's alias — same shape as suggest:

```java
@Reference(target = "(search.unit.alias=catalog)")
HighlightSearch highlights;
```

Highlightability is a per-field question answered at request time, so every mapped unit
gets the service — there is no per-unit opt-in to declare.
