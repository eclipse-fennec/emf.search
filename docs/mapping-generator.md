# Generating a mapping to start from

`MappingGenerator` reads an ecore and proposes an `IndexUnitMapping` — the file you then
edit and commit. It is the counterpart of the eorm mapper in the persistence stack, and it
lives in the core bundle `org.eclipse.fennec.search`.

```java
Suggestions suggestions = MappingGenerator.forPackage(catalogPackage).generate();

suggestions.mapping();        // the IndexUnitMapping — save it as catalog.esearch
suggestions.explanations();   // one line per declaration, saying why
```

## This is not the convention layer

An unmapped class is **already indexed** by convention: ids and enums become keywords,
strings analyzed text, numerics points with doc values. You need no mapping at all to
search a model, and the generator does not change that.

What it adds is the layer above — the declarations a model usually wants and a newcomer
does not know to write, made **visible and editable** instead of implicit:

| It sees | It proposes | Because |
|---|---|---|
| an enum attribute | keyword + facet dimension | a small closed set of values is what a filter sidebar counts |
| a many-valued string | keyword + multi-valued facet | the tag shape: exact values a query matches and a sidebar counts |
| `name` / `title` / `label` | keyword with doc values, plus a suggestion source | a result list sorts by the label and an exact match asks for it — analyzed text can do neither |
| a containment reference | `NESTED` | parent and children as one block is what makes a quantifier over the children answerable |
| a non-containment reference | `ID_ONLY` | an index has no join: a cross-document reference is an id a query compares |
| a class holding exactly two floating-point values | a geographic position | the packed point shape, recognised **structurally** — never by the class being called `GeoPoint` |
| a numeric, temporal or boolean attribute | *nothing* | convention already indexes it correctly; saying it twice is not a suggestion |

## Names are a guess, and that is the point

Conventions **never** guess from a name — a wrong guess would silently change what a query
answers. The generator may, because its output is a proposal a human reads before it ever
runs: `name`, `title` and `label` are treated as the human-readable label of a class.

That is also why every declaration comes with its reason:

```
Product.condition: a facet dimension, because an enum is a small closed set of values —
    exactly what a filter sidebar counts.
Product.name: a keyword projection with doc values, because a human-readable label is what
    a result list sorts by and what an exact match asks for — analyzed text can do
    neither. Declare it as a sub-field of a TextFieldMapping if you also want full-text
    search over it.
Product.reviews: NESTED, so parent and children are indexed as one block: that is what
    makes a quantifier over the children ('any review rated 4 or more') answerable. Use
    EMBED instead if you only ever match the children's values without asking which child
    matched.
```

Read them once, keep what fits, delete the rest. A generated mapping you disagreed with
and edited is a better mapping than one you accepted unread.

## Saving it

The generator returns a model, not a file — writing EMF content is EMF's own two-line
idiom, and keeping it out of the backend keeps the XMI machinery off your runtime path:

```java
ResourceSet resourceSet = new ResourceSetImpl();
resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
        .put("esearch", new XMIResourceFactoryImpl());
Resource resource = resourceSet.createResource(URI.createFileURI("catalog.esearch"));
resource.getContents().add(suggestions.mapping());
resource.save(null);
```

From there it is an ordinary authored mapping — see
[mapping delivery](./mapping-delivery.md) for how it reaches the runtime.

## What it will not do

- **It never merges into an existing mapping.** A mapping is authored; generating over
  authored declarations is how generators destroy work. Generate into a new file and
  diff it against yours.
- **It declares nothing it cannot justify.** A model whose classes are all served by
  convention produces an empty mapping and says so — a complete answer, not a failure.
- **It proposes no rank signals, computed fields or materialization.** Those encode
  intent the ecore does not carry: which number means popularity, which value is worth
  computing, whether the originals live somewhere else. See
  [rank signals](./rank-signals.md), [computed fields](./computed-fields.md) and
  [materialization](./materialization.md) — each of those pages shows the declaration to
  add by hand.

## Selected classes only

```java
MappingGenerator.forClasses(List.of(productClass, reviewClass), "catalog").generate();
```

A unit indexes one `EPackage` universe, so classes from two packages are refused with that
answer — generate one mapping per package.
