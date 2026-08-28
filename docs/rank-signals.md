# Rank signals

A rank signal is a number your data already carries — a view count, a rating, a curated
weight, a recency stamp — folded into a query's relevance score. It answers the request
every search box eventually gets: *"same match quality, but show the popular ones first."*

Two properties make this backend's version of it different from a scoring script:

- **The formula lives in the mapping, not in the query.** A consumer selects a declared
  signal *by name*; it can never send an expression. That is what keeps the refusal of
  arithmetic pushdown honest — see [the query path](./query-path.md) — and it means a
  relevance decision is reviewable, versionable and testable, in the same model as the rest
  of the mapping.
- **A signal can only add score, never change what matches.** It joins the query as an
  optional clause. A document without the signal keeps exactly the score its text earned;
  no hit appears or disappears because of a signal.

## Declaring one

A `RankSignalFieldMapping` reads one numeric attribute of a document:

```xml
<documents eClass="https://example.org/catalog#//Product">
  <fields xsi:type="esearch:RankSignalFieldMapping"
      feature="https://example.org/catalog#//Product/views"
      function="SATURATION" pivot="500.0"/>
</documents>
```

The signal is named after its field — `views` here, or the `name` attribute when the
mapping sets one. That name is what a query selects.

| Property | Meaning |
|---|---|
| `function` | `SATURATION` (default), `LOG` or `SIGMOID` — see below |
| `pivot` | The value at which the contribution reaches half its maximum. A good starting point is the median of the signal over the corpus |
| `exponent` | `SIGMOID` only: how sharply the curve turns at the pivot |
| `boost` | The signal's weight relative to the text score (from `FieldMapping`, default `1.0`) |

All three functions **saturate**: a large signal approaches a ceiling instead of growing
without bound, so a wildly popular document cannot bury a better textual match.

- **`SATURATION`** — `value / (value + pivot)`. The general-purpose choice, and the only
  one that works without a declared pivot: Lucene then derives one from the index
  statistics.
- **`LOG`** — for signals spanning orders of magnitude, such as view counts. `pivot` acts
  as the scaling factor (default `1.0`).
- **`SIGMOID`** — a sharper transition around the pivot. A pivot is mandatory here: a
  sigmoid without the point it turns at has no meaning, so it is refused rather than
  guessed.

### Keeping the attribute usable as a value

A signal is written as a Lucene feature — a *quantized weight*, not a value. An attribute
whose only mapping is a rank signal is therefore no longer something you can filter or sort
on, and a predicate over it is refused by name.

When the number is both a signal and an ordinary value, declare the signal as a
**sub-field** beside the primary projection:

```xml
<fields xsi:type="esearch:NumericFieldMapping"
    feature="https://example.org/catalog#//Product/views" docValues="true">
  <subFields xsi:type="esearch:RankSignalFieldMapping" name="signal"
      function="SATURATION" pivot="500.0"/>
</fields>
```

`views` stays comparable and sortable, and the signal is selected under its compound name
`views.signal`.

## Selecting one in a query

Signals are chosen per query, through the backend options of the persistence contract —
the same `options` map every `query(…)` call already takes. `SearchOptions` lives in the
core bundle `org.eclipse.fennec.search`; `resource` is a `lucene://` resource as on the
[query path](./query-path.md) page:

```java
Map<String, Object> options = Map.of(
        SearchOptions.RANK_SIGNALS, List.of("views"));   // or a single name

try (QueryResult result = resource.query(query, parameters, options)) {
    result.objects();   // best first
}
```

Several signals can be selected at once; each contributes independently, weighted by its
declared `boost`.

Because a signal shapes the *score*, it shows where the score is read: in relevance order
(the default order of hits, or an explicit `orderBy(score())`) and in a projected score
column. A query that sorts by a field ignores the score, and therefore the signal too.
A count has no score at all, so selecting a signal for one is **refused** rather than
quietly ignored.

## What it refuses, and why

| Situation | Answer |
|---|---|
| A name no mapping of the query's root type declares | Refused, with the declared names listed — a signal is a declaration, not something a query can invent |
| Selecting a signal for a `countOnly` or aggregation query | Refused: how many documents match does not depend on how they rank |
| `SIGMOID` without a pivot | Refused; use `SATURATION`, which can derive one |
| A many-valued attribute as a signal | Refused at mapping time: one document carries one weight per signal |
| Two declarations sharing a name but disagreeing about function or parameters | Refused: one name is one signal, for every document a query can reach |
| A value that is zero, negative or not finite | Not an error — the document simply carries no signal, exactly like one whose attribute was never set |

## What this is not

There is no scoring-expression channel, and there will not be one:
`lucene-expressions` (JavaScript compiled over doc values) is deliberately out of scope —
it is a code-execution surface driven by query input, and it would reopen the door the
arithmetic refusal closes. If a scoring formula is genuinely needed, it belongs in the
mapping as a declared signal, or in the consumer's own re-ranking after the search.
