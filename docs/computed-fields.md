# Computed fields

A field mapping usually reads one attribute of the object being indexed. Sometimes the value
you want to search on is somewhere else: the manufacturer's name on the product, the authors
of all its reviews, a name and a description in one searchable string.

`sources` says where a field's value comes from (the model is the
[shared catalog](./getting-started.md#the-example-model)):

```xml
<documents eClass="https://example.org/catalog#//Product">
  <!-- one index field, fed from the referenced manufacturer -->
  <fields xsi:type="esearch:KeywordFieldMapping" name="maker" docValues="true">
    <facet/>                      <!-- counted below, like any declared dimension -->
    <sources xsi:type="esearch:PathSource"
        segments="https://example.org/catalog#//Product/manufacturer
                  https://example.org/catalog#//Manufacturer/name"/>
  </fields>

  <!-- name and description as one searchable string -->
  <fields xsi:type="esearch:TextFieldMapping" name="searchable" separator=" ">
    <sources xsi:type="esearch:FeatureSource"
        feature="https://example.org/catalog#//Product/name"/>
    <sources xsi:type="esearch:FeatureSource"
        feature="https://example.org/catalog#//Product/description"/>
  </fields>
</documents>
```

## The ladder

| Source | Reads | Checked |
|---|---|---|
| `FeatureSource` | one attribute of the object | against the model, when the mapping is read |
| `PathSource` | a chain of features ending on an attribute (`manufacturer.name`) | against the model, segment by segment |
| `OclSource` | an m2x OCL expression | **not served — see below** |

The rule is **the weakest rung that suffices**: a declaration that can be checked without
running anything is one that fails in front of a human, not in production. A path whose
segments do not line up with the model, a path that ends on a reference, a field that
declares both a `feature` and `sources` — all refused when the mapping is loaded.

Where a source finds nothing — an unset attribute, a path through an unset reference — the
field simply carries no value. That is not an error; it is the same answer as for an
attribute nobody set.

### Several sources, one field

Each source contributes its values, so the field is **multi-valued** by default. Set
`separator` to join them into one value instead — the difference between "these are two
things to match" (keyword) and "this is one string to search" (text).

A path over a many-valued reference fans out: `reviews.author` yields every author.

### Virtual fields

A field with sources and no `feature` exists **in the index and in no EClass** — it has to
carry a `name`, since there is no attribute to take one from.

That has one consequence worth planning around: **the canonical query IR addresses features,
so no query can name a virtual field.** They are for [facets](./facets.md),
[suggest](./suggest.md), [highlighting](./highlighting.md) and as targets of full-text
matching. The `maker` field above declared a facet, so counting it works like any other
dimension — this is what a virtual field is *for*:

```java
IndexSchema schema = IndexSchema.of(mapping);   // unit and mapping: see getting started

FacetResults results = FacetSearch.of(unit, schema).count(FacetRequest
    .over(QueryBuilder.from(product).build())
    .dimension("maker"));

results.dimension("maker").orElseThrow().values();   // one count per manufacturer name
```

When a computed value has to be *queryable*, do not compute it in the mapping — put it on a
**derived `EStructuralFeature`** with the m2x derivation annotation. EMF computes it,
`eGet` returns it, the query IR can name it, and this mapper treats it as an ordinary
feature with no special case at all. That is also the road a
[group key](./grouping.md#the-group-key) takes: the catalog model's derived
`manufacturerName` attribute is this pattern, verbatim.

## Why OCL is refused

`OclSource` is declared in the metamodel and refused by the mapping loader, naming the two
ways out above.

Evaluating expressions means an expression engine, and an expression engine in the load path
of the search core means *every* deployment carries it — including the ones whose mapping
computes nothing. This backend does not charge for a feature nobody declared, and the derived
feature above is the better answer anyway: it is computed where the model lives, and it is
queryable.

## What a navigating source costs

**A computed value is recomputed when its owner is saved.** A change to a referenced object
does not refresh the documents that read it. This is the same exposure `EMBED` and `NESTED`
references already carry — an index is a copy, and a copy is as fresh as its last write.

Because the dependency is a *declaration* rather than an expression, it can be reported
rather than discovered:

```java
schema.dependencies(productClass);   // ["manufacturer.name", "reviews.author"]
```

That list is what a change-fed index needs to know which incoming change should have
refreshed which document.

## Changing a mapping means rebuilding

A computed field makes the mapping part of what the index *means*: documents written under
one declaration and read under another are silently wrong.

```java
schema.fingerprint();   // "3f6a91c2" — stable for the same mapping, different for a changed one
```

Record it beside the data — the [index unit's](./index-units.md) commit data is the place —
and a deployment can notice that its index predates the mapping it is being read with. The
honest answer to that is a rebuild; there is no migration for "this field used to mean
something else".
