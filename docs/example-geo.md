# Example: search by position

Manufacturers with a location, found by bounding box and by distance — with **no geo API
of this backend's own**: box and distance are part of the canonical query IR, so they run
through the same `search(query)` as everything else.

Runs in every build:
[`GeoExampleTest`](https://github.com/eclipse-fennec/emf.search/blob/snapshot/org.eclipse.fennec.search.examples/test/org/eclipse/fennec/search/examples/GeoExampleTest.java)
in `org.eclipse.fennec.search.examples`, over the
[shared catalog model](./getting-started.md#the-example-model).

## The mapping

`Manufacturer.location` is the packed shape: a GeoJSON-style point object whose
`coordinates` holds exactly `[lon, lat]` — longitude first. One declaration indexes it as
a searchable position ([the geo guide](./geo.md) shows the two other shapes real models
come in):

```xml
<documents eClass="https://example.org/catalog#//Manufacturer">
  <fields xsi:type="esearch:GeoPointFieldMapping"
      pointReference="https://example.org/catalog#//Manufacturer/location"
      coordinates="https://example.org/catalog#//GeoPoint/coordinates" docValues="true"/>
</documents>
```

## The corpus

| id | name | position (lon, lat) |
|---|---|---|
| m-1 | Jena Roasters | 11.586, 50.927 |
| m-2 | Erfurt Kettles | 11.030, 50.980 |
| m-3 | Munich Grinders | 11.580, 48.140 |

## By bounding box

The query names its subject once — the same `location` reference the mapping declares:

```java
GeoSubject location = geoSubject(propertyPath(locationReference));

List<Hit> hits = IndexSearch.of(unit, schema)
        .search(QueryBuilder.from(manufacturer)
                .where(geoWithin(location,
                        geoBox(geoPoint(10.5, 50.5), geoPoint(12.5, 51.5))))
                .build());

// → Jena Roasters, Erfurt Kettles — Munich is south of the box
```

## By distance

```java
// Jena↔Erfurt ≈ 39 km, Jena↔Munich ≈ 310 km
List<Hit> hits = IndexSearch.of(unit, schema)
        .search(QueryBuilder.from(manufacturer)
                .where(geoDistance(location, geoPoint(11.586, 50.927)).le(50_000))
                .build());

// → Jena Roasters, Erfurt Kettles
```

`geoDistance` is a value expression: comparing it against a threshold is the radius
search above; using it as a sort key delivers nearest-first (which makes *sort + limit*
an exact k-nearest-neighbours); projecting it returns the distance per hit.

## What to notice

- **`[lon, lat]`, longitude first**, everywhere — GeoJSON order, in the model, the
  mapping and `geoPoint(...)`.
- **Distances are metres** (`50_000` = 50 km), on the WGS84 mean radius.
- **The refusals have reasons**: distance `=`/`≠` (a measure-zero comparison on a
  continuum) and farthest-first sorting are refused with their ways out — see
  [the geo guide](./geo.md).
