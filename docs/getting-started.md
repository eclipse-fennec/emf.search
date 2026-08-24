# Getting started

One page from zero to a scored search result — plain Java, no OSGi required. Everything on
this page is the foundation the other guides build on: they all use **the same example
model** and show only their delta against what happens here.

## Getting the bundles

Group id `org.eclipse.fennec.search`; SNAPSHOTs publish to the Sonatype snapshot
repository (`https://central.sonatype.com/repository/maven-snapshots/`), releases will go
to Maven Central.

| Artifact | You need it for |
|---|---|
| `org.eclipse.fennec.search` | The backend: index units, mapping, query translation, `IndexSearch`, highlight/similarity/facet/grouping |
| `org.eclipse.fennec.search.model` | The `esearch` mapping metamodel (generated EMF code) |
| `org.eclipse.fennec.search.suggest` | Suggestions — only if you use them |
| `org.eclipse.fennec.search.osgi` | The DS layer — only in OSGi deployments |

Queries are written with the Fennec query IR, which comes from the persistence stack:
`org.eclipse.fennec.query.model` (the `QueryBuilder`/`Expressions` API) — in a bnd
workspace both stacks arrive through the `fennecPersistence` workspace library.

In a bnd workspace:

```properties
-buildpath: \
    org.eclipse.fennec.search;version=snapshot,\
    org.eclipse.fennec.search.model;version=snapshot,\
    org.eclipse.fennec.query.model;version=latest
```

## The example model

A product catalog — every guide of this documentation uses it. Package nsURI
`https://example.org/catalog`:

| EClass | Features |
|---|---|
| `Product` | `id : EString` (the EMF ID), `name : EString`, `description : EString`, `price : EDouble`, `stock : EInt`, `available : EBoolean`, `released : EDate`, `views : ELong`, `tags : EString[*]`, `condition : Condition` (enum `NEW`/`USED`/`REFURBISHED`), `reviews : Review[*]` (containment), `manufacturer : Manufacturer` |
| `Review` | `id : EString`, `author : EString`, `text : EString`, `rating : EInt` |
| `Manufacturer` | `id : EString`, `name : EString`, `location : GeoPoint` (containment) |
| `GeoPoint` | `coordinates : EDouble[2]` — `[lon, lat]` |
| `Bundle` | extends `Product`, adds `itemCount : EInt` |

Where a guide needs a feature this table does not carry, it says so explicitly — everything
else on every page refers to this model.

## The mapping

**Conventions first**: an unmapped `EClass` is indexed by convention — the id becomes a
stored keyword, strings become analyzed text, numerics become points with doc values,
enums become keywords. A declaration is an override, never a prerequisite. The smallest
useful mapping is therefore just the unit and its classes:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<esearch:IndexUnitMapping xmi:version="2.0"
    xmlns:xmi="http://www.omg.org/XMI"
    xmlns:esearch="https://eclipse.org/fennec/search/esearch/1.0.0"
    name="catalog" ePackage="https://example.org/catalog#/">
  <documents eClass="https://example.org/catalog#//Product">
    <!-- name additionally as a sortable keyword beside the analyzed text -->
    <fields xsi:type="esearch:KeywordFieldMapping"
        feature="https://example.org/catalog#//Product/name" docValues="true"/>
    <!-- reviews as a block: parent and children are indexed together (§ block join) -->
    <references eReference="https://example.org/catalog#//Product/reviews" strategy="NESTED"/>
  </documents>
</esearch:IndexUnitMapping>
```

The same mapping in plain Java — the XMI above and this code build the identical model
instance:

```java
IndexUnitMapping mapping = ESearchFactory.eINSTANCE.createIndexUnitMapping();
mapping.setName("catalog");
mapping.setEPackage(catalogPackage);

DocumentMapping product = ESearchFactory.eINSTANCE.createDocumentMapping();
product.setEClass(productClass);
KeywordFieldMapping name = ESearchFactory.eINSTANCE.createKeywordFieldMapping();
name.setFeature(nameAttribute);
name.setDocValues(true);
product.getFields().add(name);
ReferenceMapping reviews = ESearchFactory.eINSTANCE.createReferenceMapping();
reviews.setEReference(reviewsReference);
reviews.setStrategy(ReferenceStrategy.NESTED);
product.getReferences().add(reviews);
mapping.getDocuments().add(product);
```

From the mapping, one line derives the **schema** — the resolved view every API on these
pages takes beside the unit:

```java
IndexSchema schema = IndexSchema.of(mapping);
```

## End to end

```java
try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.inMemory("catalog").build())) {
    IndexSchema schema = IndexSchema.of(mapping);

    // Write through the resource contract: a ResourceSet with the lucene factory.
    ResourceSet resourceSet = new ResourceSetImpl();
    resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap()
            .put("lucene", new SearchResourceFactory(unit, DocumentMapper.of(schema)));

    try (PersistenceResource products = (PersistenceResource) resourceSet
            .createResource(URI.createURI("lucene://catalog/Product/p-1"))) {
        products.getContents().add(espressoMachine);   // a Product instance
        products.save(Map.of());
    }

    // An index is near-real-time: a write becomes visible at the next refresh —
    // ask for it, don't hope. (Production units refresh on commit or interval.)
    unit.refresh();

    // Read through the direct search API: hits as plain objects.
    IndexSearch search = IndexSearch.of(unit, schema);
    List<Hit> hits = search.search(QueryBuilder.from(productClass)
            .where(Expressions.path(descriptionAttribute).contains("espresso"))
            .build());

    hits.get(0).object();   // the reconstructed Product
    hits.get(0).score();    // and how well it matched
}
```

Three things this example just demonstrated, because every guide relies on them:

- **`lucene://<unit>/<Type>/<id>`** is the URI scheme of the resource road: unit, type
  (optional), id (optional). `lucene://catalog/Product` addresses all products — the
  [query path](./query-path.md) page has the details.
- **`unit.refresh()`** makes writes visible. Every example that indexes and then searches
  needs it (or a unit configured to refresh on commit) — see
  [index units](./index-units.md).
- **`IndexSchema.of(mapping)`** is the bridge from the declared mapping to every API:
  `IndexSearch.of(unit, schema)`, `FacetSearch.of(unit, schema)`,
  `HighlightSearch.of(unit, schema)`, and so on.

## The same thing in OSGi

No code changes — the collaborators become configuration. The unit is a factory
configuration, the mapping arrives through a registry or as a bundle's metadata aspect
([mapping delivery](./mapping-delivery.md)), and one service per mapped unit is published
for each API, keyed by `search.unit.alias`:

```java
@Reference(target = "(search.unit.alias=catalog)")
IndexSearch search;
```

## Where to go next

- [Querying the index](./query-path.md) — the canonical query IR against the index:
  shapes, capabilities, refusals, options.
- [The direct search API](./search-api.md) — hits as objects, the attachable primary store.
- [Loading & materialization](./materialization.md) — what comes back, and the three tiers.
- Per feature: [facets](./facets.md), [suggest](./suggest.md),
  [highlighting](./highlighting.md), [similarity](./similarity.md),
  [rank signals](./rank-signals.md), [grouping](./grouping.md), [geo](./geo.md),
  [computed fields](./computed-fields.md), [write commands](./write-commands.md).
