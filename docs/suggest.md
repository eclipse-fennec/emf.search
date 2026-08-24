# Suggest

Completion is not query vocabulary — a suggester answers "what might you mean" over one
declared source, not "which objects match". So suggest is **an own API** (`SuggestSearch`,
bundle `org.eclipse.fennec.search.suggest`), like facets: same mapping model, same unit,
own surface.

The predecessor's mistake is corrected structurally: the old stack kept a *parallel*
suggest index — own directory, own writer, a commit per entry, weights hardcoded. Here a
suggester is **derived from the unit's own documents**: the declared source field's stored
values, and the weight attribute's doc values, read through the same searcher every query
uses. One mapping, one index, one lifecycle.

All examples use the [shared catalog model](./getting-started.md#the-example-model).

## Declaring a source

```xml
<documents eClass="https://example.org/catalog#//Product">
  <suggestions name="names" feature="https://example.org/catalog#//Product/name"
      weight="https://example.org/catalog#//Product/views" kind="FUZZY"/>
</documents>
```

- `name` is what a lookup selects the source by; unique per unit.
- `feature` must be stored (it is by default; an explicit `stored="false"` refuses with
  the way out).
- `weight` is optional and is an **attribute reference**: each object ranks its own
  suggestion by that value — a single-valued numeric field with doc values. Unset weights
  all entries equally.
- `analyzer` optionally names one of the unit's analyzers; unset uses the unit's default.

## Kinds

| Kind | Behaviour |
|---|---|
| `ANALYZING` | analyzed prefix completion of whole stored values |
| `FUZZY` | the same, forgiving typos (edit distance) |
| `FREE_TEXT` | n-gram next-word prediction rather than completion of a known entry |
| `COMPLETION` | **refused by name** — index-time suggest fields change the document shape; they arrive as a follow-up, and filter `contexts` arrive with them |

## Looking up

`unit` and `mapping` are set up as in [getting started](./getting-started.md):

```java
IndexSchema schema = IndexSchema.of(mapping);
SuggestSearch suggest = SuggestSearch.of(unit, schema);

suggest.rebuild();   // snapshot the current documents; the first lookup would do it lazily
List<Suggestion> top = suggest.suggest("names", "espreso", 5);   // text + weight, rank-descending
```

In OSGi one `SuggestSearch` service is published **per index unit** whose mapping declares
a source, under the unit's `search.unit.alias` — a unit without sources gets no service;
absence is the signal.

## Freshness — the price, stated

The suggesters are **snapshots** (weighted FSTs). A lookup answers from the last
`rebuild()`; the first lookup builds lazily. This is deliberate: rebuilding on every write
would be the old stack's commit-per-entry amplification wearing a new hat. Callers own the
cadence — rebuild after a bulk load, on commit, or on a schedule. A rebuild swaps
atomically: lookups in flight keep answering from the previous snapshot, never from a
half-built one.

### Rebuilding on commit, if you ask for it

A commit is the natural cadence for many deployments, and one call subscribes to it:

```java
try (AutoCloseable subscription = suggest.rebuildOnCommit()) {
    // every commit of the unit now refreshes the suggesters
}   // closing the handle returns to the caller-owned cadence
```

In OSGi it is configuration on the `SearchSuggest` component, per unit alias:

```properties
rebuildOnCommit = ["catalog"]
```

Three properties of that subscription, stated because they are the ones that bite:

- **The rebuild runs on the committing thread.** Whoever commits pays for the freshness.
  A unit tuned to commit per document should therefore not opt in — that is the
  amplification this API avoids by default, and opting in is you choosing it.
- **Commits collapse.** At most one rebuild runs at a time; commits arriving during a
  rebuild are covered by one trailing rebuild. The FSTs are snapshots — the latest commit
  is what matters, not each intermediate one.
- **A failed rebuild does not fail the commit** (it is already durable), and it does not
  silently serve stale suggestions either: the failure is kept and thrown by the next
  lookup, naming `rebuild()` as the way back.
