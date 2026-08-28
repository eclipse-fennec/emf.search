# Example: a secondary index beside a primary store

The dominant deployment: objects live in JPA or Mongo, the index answers search, and the
hits are **the originals** — not reconstructions. The whole example is one mapping
declaration and one attached collaborator.

Runs in every build:
[`SecondaryIndexExampleTest`](https://github.com/eclipse-fennec/emf.search/blob/snapshot/org.eclipse.fennec.search.examples/test/org/eclipse/fennec/search/examples/SecondaryIndexExampleTest.java)
in `org.eclipse.fennec.search.examples`, over the
[shared catalog model](./getting-started.md#the-example-model). The primary store is a
plain EMF resource standing in for JPA or Mongo — the pattern is identical.

## The mapping delta

One declaration turns a document into a pointer: with `SOURCE_URI`
[materialization](./materialization.md), the index stores *where the original lives*
instead of a reconstruction.

```xml
<documents eClass="https://example.org/catalog#//Product">
  <materialization kind="SOURCE_URI"/>
</documents>
```

The example applies that as a copy of the
[authored mapping](./getting-started.md#the-mapping), because a mapping is data:

```java
IndexUnitMapping mapping = EcoreUtil.copy(catalog.mapping());
Materialization sourceUri = ESearchFactory.eINSTANCE.createMaterialization();
sourceUri.setKind(MaterializationKind.SOURCE_URI);
productDocument(mapping).setMaterialization(sourceUri);
IndexSchema schema = IndexSchema.of(mapping);
```

## Write side

The originals live in the primary store; the index is fed beside it — in production by
the save path or the change stream, here directly:

```java
Resource primary = ...;                    // stands in for JPA/Mongo
primary.getContents().add(machine);
primary.getContents().add(grinder);

DocumentMapper mapper = DocumentMapper.of(schema);
for (EObject object : List.of(machine, grinder)) {
    MappedDocument mapped = mapper.map(object);
    unit.updateDocuments(mapped.term(), mapped.documents());
}
unit.refresh();
```

An object mapped `SOURCE_URI` **must live in a resource** when it is indexed — the URI is
what gets stored, and an object without one is refused at write time.

## Read side

The [direct search API](./search-api.md) takes the primary store as an explicit
collaborator. Resolution is **batched**: one search window, one `resolve(...)` call — a
JPA-backed store answers with one query, never one per hit:

```java
PrimaryStore store = uris -> productRepository.findByUris(uris);

List<Hit> hits = IndexSearch.of(unit, schema)
        .withPrimaryStore(store)
        .search(QueryBuilder.from(product)
                .where(path(description).contains("coffee"))
                .build());

hits.get(0).object();   // the original, straight from the primary store
```

Without a store, a hit is an **EMF proxy** carrying the original's URI — exactly what the
index knows, resolvable by whoever wants to:

```java
List<Hit> hits = IndexSearch.of(unit, schema).search(query);
hits.get(0).object().eIsProxy();   // true — resolve it when and where you choose
```

## What to notice

- **The index never loads the primary model.** Writing stores the URI; reading hands it
  back or resolves it through *your* store. No ambient `ResourceSet`, no hidden I/O.
- **One batch per search** — the example records the `resolve(...)` calls and asserts
  there was exactly one.
- **A URI the store cannot answer keeps its proxy.** The index said there is a match;
  hiding the hit would misreport the search.
- The write half of this deployment — save to the primary store, index on success, under
  a transaction bracket — is tracked as
  [#50](https://github.com/eclipse-fennec/emf.search/issues/50).
