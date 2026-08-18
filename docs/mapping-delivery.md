# Mapping delivery

A mapping (`IndexUnitMapping`) tells the backend how EObjects become documents. This page
is about how a mapping **reaches a unit at runtime** — authored as a file, or shipped by
the model itself.

## Authoring an `*.esearch`

An `*.esearch` file is plain XMI over the esearch metamodel. The minimal file names the
unit and its package; everything else is an override of the conventions:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<esearch:IndexUnitMapping xmi:version="2.0"
    xmlns:xmi="http://www.omg.org/XMI"
    xmlns:esearch="https://eclipse.org/fennec/search/esearch/1.0.0"
    name="catalog"
    ePackage="https://example.org/catalog#/">
  <documents eClass="https://example.org/catalog#//Product">
    <references eReference="https://example.org/catalog#//Product/reviews" strategy="NESTED"/>
  </documents>
</esearch:IndexUnitMapping>
```

The `name` inside the mapping is the truth: lookup is by unit name, never by the file name
or the key a delivery happened to use.

## The deployment path: the EObject registry

Authored files are deployed through emf.osgi's EObject registry — two factory
configurations, nothing search-specific:

```json
"FileEObjectProvider~search": {
  "emf.eobject.provider.name": "search-mapping-files",
  "locations": [ "/etc/myapp/mappings" ]
},
"EObjectRegistry~search": {
  "name": "search-mappings",
  "initialProvider.target": "(emf.eobject.provider.name=search-mapping-files)"
}
```

The lucene `Resource.Factory` binds the registry named `search-mappings` (override with
the `mappingRegistry.target` property) and resolves a unit's mapping the moment the first
resource for that unit is created. A **changed registry entry is noticed**: the next
resource sees the new mapping, and a warning states the consequence — documents written
under the previous shape are not rewritten; a shape-relevant change means a rebuild of
the index.

### Deliver the mapping before the units

The mapping registry should be up **before** the unit configurations, because a unit reads
its index order from the mapping at activation and that order is fixed when the writer is
created (see the index-units guide). A registry that arrives late therefore restarts the
unit component — correct, but it discards an in-memory index and briefly replaces the
`IndexUnit` service, so anything already holding one sees a closed unit. Order the
configurations (or let the provider's files be present at framework start) and the unit
activates once, with its order. The per-unit services — suggest, highlight, similarity —
follow a replaced unit on their own.

In plain Java the same mechanism is one line, not a parallel implementation:

```java
EObjectRegistryWriter writer = EObjectRegistries.createRegistry("search-mappings",
    new FileEObjectProvider("files", resourceSet, List.of(mappingDir),
        FileEObjectProvider.featureKeys("name")));
MappingSource source = RegistryMappingSource.of(writer.getRegistry());
```

## The shipped path: the metadata aspect

A model bundle can carry its own index mapping as a **metadata aspect** — the same slot
the codec and orm aspects use. The model side attaches it with a `MetadataHandler` when
its package registers:

```java
AspectEntry entry = MetadataFactory.eINSTANCE.createAspectEntry();
entry.setTypeId("esearch");
entry.setContent(indexUnitMapping);
packageMetadata.getAspects().add(entry);
```

The backend reads it via `MetadataService.getPackageAspect(ePackage, "esearch")`
(`AspectMappingSource`), for every EPackage registered as a service.

## Precedence

When both exist, **the registry entry wins**: a deployment overriding what a model ships
with is the normal direction — the reverse would make a shipped mapping impossible to
correct without rebuilding the model bundle. Which source served is logged at resolution
time, because "which mapping am I actually running" is the first question when an index
looks wrong.

## Validation

Resolution is where configuration mistakes surface, not the first document: a mapping
whose documents name classes outside its declared package is refused when the schema is
derived, an unmapped unit is refused by the factory naming both delivery roads, and a
missing unit configuration is refused naming the factory PID and alias property.
