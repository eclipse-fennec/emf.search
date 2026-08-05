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
