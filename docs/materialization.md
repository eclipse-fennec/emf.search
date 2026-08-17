# Loading and materialization

A search hit is a Lucene document. What you get back as an **EObject** depends on what the
document's mapping declares — a per-class choice with three tiers, because the three have
genuinely different costs and the index should not pay for completeness nobody asked of it.

| Tier | Declaration | What a hit becomes | Price |
|---|---|---|---|
| **Partial** (default) | none | an EObject rebuilt from the document's stored fields — incomplete by design | none: the stored fields are already there |
| **`STORED_OBJECT`** | `Materialization` with `kind = STORED_OBJECT` | the complete object tree, deserialized from one stored field | blob bytes per object, serialization on every write |
| **`SOURCE_URI`** | `Materialization` with `kind = SOURCE_URI` | an EMF proxy carrying the object's original URI, resolved through your `ResourceSet` against the primary store | one stored string; resolution costs a primary-store read |

## The default: partial objects, honestly labelled

Without a `Materialization`, loading rebuilds objects from what the index itself stores:

- every conventionally mapped attribute comes back — the write convention **stores original
  values by default** (opt out per field with `stored="false"` when a value is large or
  comes back another way),
- `NESTED` children are reassembled into their containment list, in order,
- `ID_ONLY` references come back as proxies under `lucene://<unit>/<Type>/<id>`, so they
  resolve right back through this backend if the target is indexed in the same unit.

What **cannot** come back: fields declared `stored="false"`, references that were never
mapped, and `EMBED` references — a flattened embed has lost which value belonged to which
target, and the reader will not invent that correlation. None of this is silent: the loaded
resource carries a **warning diagnostic per class** naming exactly the features that are
missing.

Partial objects are legitimate read results — list views, pick lists, search result pages.
They are never write sources: saving one back would erase everything the index did not
carry, which is why update-by-selector stays gated on `STORED_OBJECT`.

## `STORED_OBJECT`: the self-sufficient index

```xml
<documents eClass="...#//Product">
  <materialization/>                      <!-- kind defaults to STORED_OBJECT -->
</documents>
```

The whole object tree is serialized into one binary stored field (default name `_source`)
on the root document — nested children ride inside it. Hits come back complete, without a
primary store: this is what makes the standalone role self-sufficient.

The serialization mechanism is pluggable (`ObjectSerializer`, selected by the mapping's
`format` attribute). The default is **`binary`** — EMF's own binary resource format: no
extra dependency, compact, and references leaving the tree follow standard EMF rules
(serialized as URIs, back as proxies).

Two caveats, both enforced rather than documented away:

- **Changing the `format` invalidates stored objects** written with the previous format.
  An unknown format is refused at the first write; unreadable bytes are refused at read
  time with a message naming the rebuild.
- **A document written before the declaration is refused**, not silently served partial —
  a mapping that declares `STORED_OBJECT` promises complete objects, and the index needs a
  rebuild to keep that promise.

## `SOURCE_URI`: the secondary index

```xml
<documents eClass="...#//Product">
  <materialization kind="SOURCE_URI"/>
</documents>
```

The index stores the object's original URI at indexing time — `mongodb://…`, a JPA
resource URI, a file — and a hit materializes by resolving that URI through the caller's
`ResourceSet`. The index finds; the primary store materializes. Loading populates
**proxies** (it never pulls objects out of the resource that owns them); the first access
resolves them.

Consequently, an object that lives in no resource has no primary URI and is refused at
mapping time — as is an object living in the search resource itself, because a stored URI
pointing back into the index would be a circle, not a source.

## Choosing

- Standalone index, hits must be complete → `STORED_OBJECT`.
- Index next to JPA/Mongo/files, hits are entry points into the primary data →
  `SOURCE_URI`.
- Hits feed lists and previews, completeness comes from somewhere else (or nobody needs
  it) → default, and read the warnings once.
