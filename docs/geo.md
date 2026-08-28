# Geographic queries

Geo is the one place where an embedded Lucene index is the strongest backend in the
Fennec persistence stack — and unusually for this repository, it needs **no own API**: box,
polygon and distance are part of the canonical query IR (`GeoWithin`, `GeoDistance`), so
they arrive through the ordinary `QueryProcessor` and cost you nothing new to learn.

Two capabilities carry it: `GEO_WITHIN` and `GEO_DISTANCE`, both declared by this backend.

## Declaring a position

A position is a field mapping like any other — `GeoPointFieldMapping` — and it accepts the
three shapes real models are written in. The examples run on the
[shared catalog model](./getting-started.md#the-example-model), whose
`Manufacturer.location : GeoPoint` is the packed shape (2); the split pair (1) and the
combined attribute (3) show the same manufacturer as models with `lat`/`lon` or a
many-valued `corner` attribute would declare it. Whichever you declare, the mapper
resolves it at index time into one `LatLonPoint`, after which nothing downstream can tell
the difference:

```xml
<!-- 1. the split pair: two attributes, the dominant Ecore shape -->
<fields xsi:type="esearch:GeoPointFieldMapping" name="position"
        latitude="https://example.org/catalog#//Manufacturer/lat"
        longitude="https://example.org/catalog#//Manufacturer/lon" docValues="true"/>

<!-- 2. the packed point: a GeoJSON-style child object with [lon, lat] — the shared
     model's shape -->
<fields xsi:type="esearch:GeoPointFieldMapping"
        pointReference="https://example.org/catalog#//Manufacturer/location"
        coordinates="https://example.org/catalog#//GeoPoint/coordinates" docValues="true"/>

<!-- 3. the combined attribute: one many-valued numeric attribute holding [lon, lat] -->
<fields xsi:type="esearch:GeoPointFieldMapping"
        coordinates="https://example.org/catalog#//Manufacturer/corner"/>
```

Notes that save debugging time:

- **`[lon, lat]`, longitude first** in both packed shapes — GeoJSON order, the same order
  the IR's `geoPoint(lon, lat)` literal takes.
- **The split pair needs a `name`**; a pair of attributes has no natural field name. The
  packed shapes default to the reference (or attribute) name, which is exactly what the
  query names, so leaving `name` unset is the normal case there.
- **A coordinates attribute is a position, not a value.** It is not indexed a second time
  as a plain number — one index holds one field type per name, and `corner = 11.586`
  answers nothing. A path to it refuses with that message. The split pair is the opposite:
  `lat` and `lon` stay ordinary numeric fields you can compare on their own.
- **`docValues="true"` is what a distance *sort* reads.** Predicates work without it.
- **Declare several positions on one class** if your model carries several — each query
  binding picks the field it was declared over.

## Asking

```java
// inside a box; the corners are [lon, lat] literals, south-west then north-east
Expressions.geoWithin(subject, Expressions.geoBox(
        Expressions.geoPoint(11.3, 50.5), Expressions.geoPoint(12.5, 51.5)));

// inside a polygon: implicitly closed, at least three points
Expressions.geoWithin(subject, Expressions.geoPolygon(
        Expressions.geoPoint(11.3, 50.6),
        Expressions.geoPoint(12.5, 50.6),
        Expressions.geoPoint(11.9, 51.4)));

// within 37 km of a point — a distance is a value, so it is compared
Expressions.geoDistance(subject, Expressions.geoPoint(11.586, 50.927)).le(37_000);
```

The `subject` is the coordinate binding, and it must name the features the mapping declared
the position over — the pair for a split field (variant 1), the reference (or attribute)
for a packed one. `location` is the shared model's `Manufacturer.location` reference;
`lat`/`lon` exist only under variant 1:

```java
GeoSubject split  = Expressions.geoSubject(Expressions.propertyPath(lat),
                                           Expressions.propertyPath(lon));
GeoSubject packed = Expressions.geoSubject(Expressions.propertyPath(location));
```

A binding that matches no declared position refuses by name and lists the ones that exist,
rather than guessing which position you meant.

### Nearest first

`GeoDistance` is a value expression, so it is also a sort key — and because the sort reads
the real position of every candidate from doc values, "nearest k" is *sort + limit*, exact
rather than approximate:

```java
QueryBuilder.from(manufacturer)   // the Manufacturer EClass; packed as above
    .orderBy(Expressions.geoDistance(packed, Expressions.geoPoint(11.586, 50.927)))
    .limit(10);
```

Objects without a position sort last, which is what UNKNOWN means for an ordering.

## What it does and does not promise

- **Distances are metres**, spherical WGS84. Lucene measures over the mean earth radius
  6 371 008.7714 m, the reference engine over 6 371 008.8 — a 4.5e-9 relative difference,
  orders below the tolerance the vocabulary itself declares, so backend and reference agree
  on every threshold that is not sitting on the boundary.
- **A box may cross the antimeridian**: south-west longitude greater than north-east
  longitude *is* the wrap-around box, and it is matched natively rather than split in two.
  Polygons may not cross it.
- **Missing coordinates are UNKNOWN**, not "far away". An object with no position is
  excluded from a geo predicate *and* from its negation — the three-valued discipline every
  other predicate here follows.
- **An impossible position is refused when indexing**, not silently dropped: latitude
  outside -90..90 or longitude outside -180..180 fails the write, naming the object.
  A *missing* coordinate is not an error, though — a packed point whose `coordinates` does
  not hold exactly two numbers is simply absent, the packed analogue of a null.
- **`<` is served as `<=`.** The two differ only for a point sitting exactly on the radius,
  which is below the accuracy the vocabulary declares — and below Lucene's own encoding
  resolution. What is refused instead is `=` and `!=` on a distance: those ask which
  objects sit exactly on a circle, a measure-zero question no backend can answer honestly.
- **Farthest-first sorting is refused.** Lucene's distance sort has no reversed form, and
  faking one would need per-document arithmetic this backend does not push down. Bound the
  far side with a predicate instead: `geoDistance(...) >= r`.
