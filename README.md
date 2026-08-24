# emf.search

**Apache Lucene as a search backend for EMF models** — ranked full-text, facets,
suggestions, highlighting, similarity and grouping over `EObject`s, declared once in a
mapping model instead of hand-coded per use case. Part of
[Eclipse Fennec](https://projects.eclipse.org/projects/modeling.fennec).

> **Status: early development.** The wave-1 feature line is built and TCK-conformant;
> nothing is released to Maven Central yet. SNAPSHOTs publish from the `snapshot` branch.

## What it does

EMF gives you a model, the Fennec persistence stack gives you storage and structured
queries. Neither answers *"find the twenty most relevant objects for these words"* — this
repository does, in two roles:

- **A standalone index.** The index *is* the store: a `PersistenceResource` /
  `QueryableResource` you save into and query from, speaking the canonical Fennec query IR
  like every other backend — no Lucene API in sight.
- **A secondary index beside JPA or Mongo.** Objects live in the primary store; the index
  answers search and hands back the originals through an attachable `PrimaryStore`.

Three principles shape everything here:

1. **Declarative mapping, conventions first.** Which `EClass` becomes which index, which
   attribute is tokenized, stored, sortable, facetable — declared in an `esearch` mapping
   model. Small models need no mapping at all; every declaration is an override, never a
   prerequisite.
2. **Capability honesty.** What Lucene cannot do honestly — query-time joins, cross-document
   paths, arithmetic pushdown — is *declared unsupported* and refused with a diagnostic
   naming the way out, never faked. What it can do well (relevance, facets, suggest,
   block joins over containment) is first-class.
3. **OSGi-ready, OSGi-optional.** Every service has a plain-Java constructor; the OSGi
   layer only wraps. Nothing here requires a framework to run or to test.

## A taste

```java
// A mapping — one class, everything else by convention:
IndexUnitMapping mapping = ...;             // authored as *.esearch, or built in code

// An in-memory unit, an object, a search:
try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.inMemory("catalog").build())) {
    IndexSearch search = IndexSearch.of(unit, IndexSchema.of(mapping));
    List<Hit> hits = search.search(QueryBuilder.from(product)
            .where(path(name).contains("espresso"))
            .build());
    hits.get(0).object();   // a plain EObject
    hits.get(0).score();    // and how well it matched
}
```

The same queries run through the persistence contracts (`resource.query(…)`), and the
own-API family adds [facets](docs/facets.md), [suggest](docs/suggest.md),
[highlighting](docs/highlighting.md), [similarity](docs/similarity.md),
[grouping](docs/grouping.md), [geo](docs/geo.md) and [rank signals](docs/rank-signals.md).

## Documentation

The user guides are published at **<https://eclipse-fennec.github.io/emf.search/>** and
live in [`docs/`](docs/):

| Start here | |
|---|---|
| [Overview](docs/overview.md) | What problem this solves, the two roles, the honesty contract |
| [Getting started](docs/getting-started.md) | Zero to a scored search result, plain Java — and the shared example model |
| [Querying the index](docs/query-path.md) | The canonical query IR against the index: shapes, capabilities, refusals |
| [Architecture](docs/architecture.md) | Bundles, contracts, how the pieces fit |
| [Index units](docs/index-units.md) | Units, lifecycle, refresh/commit, configuration |
| [Mapping delivery](docs/mapping-delivery.md) | How a mapping reaches the runtime — authored file, registry, metadata aspect |
| [Loading & materialization](docs/materialization.md) | The three reconstruction tiers, including the secondary-index tier |
| [The direct search API](docs/search-api.md) | Hits as objects, the attachable primary store |
| [Write commands](docs/write-commands.md) | Selector-addressed writes, and the honest transaction answer |

The internal design blueprint is [`docs/search-access.md`](docs/search-access.md) — the
source of truth for the capability profile and the feature radar.

## Repository layout

| Bundle | Role |
|---|---|
| `org.eclipse.fennec.search.model` | The `esearch` mapping metamodel and generated code |
| `org.eclipse.fennec.search` | The backend as a plain-Java library: index lifecycle, mapping, query translation, the search/highlight/similarity/facet/grouping APIs |
| `org.eclipse.fennec.search.suggest` | The suggest API over declared sources |
| `org.eclipse.fennec.search.osgi` | The thin DS layer: config-driven units, one service per mapped unit, the `lucene` `Resource.Factory` |
| `org.eclipse.fennec.search.tck` | The binding of the published persistence TCK to this backend |

## Building

A [bnd](https://bnd.bndtools.org/) workspace driven by Gradle:

```bash
./gradlew build              # compile + unit tests + coverage gates
./gradlew build testOSGi     # incl. OSGi integration tests
```

Work is tracked as [issues](https://github.com/eclipse-fennec/emf.search/issues);
`snapshot` publishes SNAPSHOTs, `main` releases. Contributions follow the
[Eclipse Contributor Agreement](https://www.eclipse.org/legal/ECA.php).

## License

[EPL-2.0](LICENSE)
