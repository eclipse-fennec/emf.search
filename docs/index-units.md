# Index units

An **index unit** is one Lucene index: one directory, with the writer and searcher sides its
configuration asks for. Everything else in `emf.search` — mapping, query translation,
suggest — runs on top of a unit.

::: warning Status
The unit lifecycle exists; the mapping and query layers above it are being built. See the
[issue board](https://github.com/eclipse-fennec/emf.search/issues).
:::

## Three axes

A unit is described by three independent choices, kept apart because collapsing them is how
an index configuration ends up promising something it does not do.

| Axis | Values | What it decides |
|---|---|---|
| **Access** | `READ_WRITE`, `READ_ONLY`, `BULK_LOAD` | whether writes are allowed, and whether a searcher is opened at all |
| **Visibility** | `NRT`, `COMMITTED` | whether a searcher can see uncommitted writes |
| **Refresh** | background, on commit, manual | when the searcher is reopened |

**Visibility is the one that surprises people.** `NRT` builds the searcher from the writer,
so a refresh exposes writes that have not been committed. `COMMITTED` builds it from the
directory, so no amount of refreshing will show an uncommitted write — with `COMMITTED`, it
is the *commit* cadence that governs what readers see, not the refresh cadence.

`BULK_LOAD` opens no searcher and no reopen thread: nothing pays for visibility while an
index is being built. Searching such a unit refuses rather than returning an empty result.

`READ_ONLY` refuses every write. It still opens an `IndexWriter` — one code path is easier
to trust than two, and a near-real-time searcher needs the writer anyway — which has one
visible consequence: **a read-only unit still takes the directory's write lock**, so it can
neither open an index on a write-protected filesystem nor share a directory with another
writer. That fails when the unit opens, with a message saying exactly this.

## In plain Java

```java
try (IndexUnit unit = IndexUnit.open(
        IndexUnitConfig.builder("catalog", IndexLocation.path(Path.of("/var/index/catalog")))
            .visibility(Visibility.NRT)
            .refresh(RefreshTrigger.background(Duration.ofSeconds(1)))
            .commit(CommitPolicy.afterDocuments(1000))
            .build())) {

    unit.addDocument(document);
    unit.refresh();

    long hits = unit.search(searcher -> searcher.count(query));
}
```

Writes go through the unit rather than through a handed-out writer. That is what lets a
document-count commit trigger work at all, and `search(...)` releases the searcher for you —
the release is the part that gets forgotten.

For tests, `IndexUnitConfig.inMemory("name")` with `RefreshTrigger.manual()` gives a unit
with no background threads and no timing: write, `refresh()`, assert.

## In OSGi

The same configuration, as a Configuration Admin factory configuration for the PID
`SearchIndexUnit`. Each configuration opens one unit and publishes it under its alias:

```properties
# SearchIndexUnit~catalog.cfg
alias = catalog
location = /var/index/catalog
visibility = NRT
refresh = BACKGROUND
refresh.interval.ms = 1000
commit.max.documents = 1000
```

```java
@Reference(target = "(search.unit.alias=catalog)")
IndexUnit unit;
```

`location = memory` gives an in-memory index. The other keys map one-to-one onto the plain
Java options above — deliberately, because the OSGi layer does nothing but turn
configuration into the same `IndexUnitConfig` the core already understands. Nothing in the
search behaviour is reachable only through a framework.

## Analyzers

A unit resolves analyzers by name through a small registry that is handed to it:

```java
AnalyzerRegistry analyzers = AnalyzerRegistry.builder()
        .defaultAnalyzer(new StandardAnalyzer())
        .register("german", new GermanAnalyzer())
        .build();
```

In OSGi the registry is built from `Analyzer` services carrying the property
`search.analyzer.name`, and a unit names its default with `default.analyzer`. Registering an
unknown name fails with the names that *do* exist, because the usual cause is a typo in a
mapping or an analyzer bundle that is not installed.

The registry is immutable, and an analyzer service appearing or disappearing reactivates the
unit rather than mutating the registry underneath it. Swapping an analyzer under a live
writer would change how documents are analyzed halfway through an index — which is a corrupt
index, not a configuration change.

## Checkpoints — where the index came from

A commit can carry a checkpoint: a small string map that travels *inside* the same commit
point as the documents it belongs to.

```java
unit.addDocument(document);
unit.commit(Map.of("stream.offset", "4711"));   // content and position, one commit

unit.checkpoint(Map.of("stream.offset", "4712")); // stage; the unit's commit policy decides when
unit.pendingCheckpoint();                          // what the next commit will carry
unit.checkpoint();                                 // what the newest commit on disk carries
```

The point is not storage — anywhere can store an offset — but that it **cannot drift**. A
commit is all-or-nothing, so after a crash the surviving checkpoint describes exactly the
surviving documents: a feed resumes from it without replaying what is already indexed and
without skipping what is not. An offset kept beside the index gives you two facts that can
disagree, and no way to tell which one is right.

Details worth knowing:

- **Staging is not publishing.** `checkpoint(map)` only sets what the next commit will
  carry; until then `checkpoint()` still answers with the older, durable position. The last
  value staged before a commit is the one that lands.
- **A checkpoint alone is committable** — "I read up to here and it produced nothing to
  index" is recordable, so an empty stretch of the stream is not replayed after a restart.
- **Reading is a read.** `checkpoint()` comes from the directory, not from the writer, so it
  answers on a `READ_ONLY` unit too — which is how you inspect a suspect index. Writing one
  needs a writable unit.
- **No commit yet, no checkpoint**: an empty map, meaning "resume from the beginning",
  rather than an error.
- The keys are yours. `stream.offset` is what the examples use; nothing in the unit
  interprets them.

## Measured behaviour

The choices above have a cost, so there is a suite that measures it rather than a paragraph
that guesses at it (issue #38):

```bash
./gradlew perfTest                          # the default corpus, a few seconds
./gradlew perfTest -Dsearch.perf.scale=20   # twenty times the corpus
```

Three areas, and two kinds of statement in them. **Structure gates**: document counts, block
sizes, and the invariant that a concurrent searcher never observes half a block. **Timing is
logged and never asserted**, because a busy machine must not be able to fail a build.

What the current numbers say, on a developer machine and as an order of magnitude rather than
a promise:

| Measurement | Observation |
|---|---|
| Plain documents | ~150 000 objects/s, roughly 60 bytes per document on disk |
| Blocks of four | ~50 000 objects/s; a block costs its child documents, about 2.9× the flattened form |
| `CommitPolicy.afterDocuments(1000)` vs `onClose()` | **+430 % per document** — committing is the expensive part of indexing, not mapping |
| NRT visibility | median equal to the configured reopen interval (50 ms), p90 ~77 ms |
| Query latency while indexing | +8 % against a quiet index |
| Storing one text field | +15 % index size |

Two of those are worth carrying into a decision. Commit policy dominates write throughput, so
`afterDocuments` is a durability choice with a real price rather than a tuning knob — and
`AccessMode.BULK_LOAD` exists precisely so an initial load pays neither for commits nor for a
searcher. And "near real time" means exactly the reopen interval: `RefreshTrigger.background`
is the whole latency budget, so a consumer that needs its write visible sooner has to ask for
a `refresh()`, not hope.

## Index order

A unit can be physically sorted: the mapping declares the dominant order once
(`IndexUnitMapping.sort`, one or more entries over single-valued, doc-values-carrying
fields), and the unit writes its segments in that order — fixed at index creation, which
is why it is *declared* in the mapping rather than configured on the unit: changing the
order is a rebuild, like every shape-relevant mapping change.

```xml
<esearch:IndexUnitMapping name="events" ePackage="...#/">
  <sort>
    <entries feature="...#//Event/timestamp" descending="true" missingLast="true"/>
  </sort>
</esearch:IndexUnitMapping>
```

The payoff is on the read side: a query whose sort matches the index order stops
collecting once its top hits are settled — Lucene does this automatically, no query-side
opt-in — which is what makes "newest first over a very large unit" cheap. That is the
time-ordered case the v2 change feed will lean on.

What a physical order refuses, by name: multi-valued attributes (one document needs one
position), analyzed text (tokens have no value order) and fields without doc values.
`missingLast` is mandatory semantics, not decoration — a sorted index with unspecified
absence ordering would page differently per segment. In OSGi the unit derives the order
from its mapping automatically when the mapping registry is present; in plain Java it is
one line: `IndexUnitConfig.builder(...).indexSort(IndexOrders.indexSort(schema))`.
