# Grouping with representatives

Grouping answers the question a facet cannot: not *how many* products each manufacturer
has, but **which** — the best three of each, as objects, in one search.

```java
GroupResults results = GroupSearch.of(unit, schema)
        .search(GroupRequest.over(query)   // the ordinary query IR, as everywhere
                .by(manufacturerName)      // the attribute whose value forms the groups
                .representatives(3)        // documents per group, best first
                .topGroups(10));           // groups, best group first

for (Group group : results.groups()) {
    group.key();              // "ACME"
    group.totalHits();        // 42 — the whole group, not the three returned
    group.representatives();  // 3 reconstructed EObjects
}
results.totalGroups();        // how many groups the match set has in all
```

## Why this is its own API

"Top-N per group" is a **result shape**, and the shared query IR has no words for it: a
`GroupBy` stage produces one row of key and aggregates per group, never documents. The
shape is reserved upstream as a later `BottomTop` stage
([emf.persistence-jpa#214](https://github.com/eclipse-fennec/emf.persistence-jpa/issues/214)),
and inventing search-only vocabulary here is not something this backend does.

So grouping takes the road facets, suggest, highlighting and similarity already take: an
API of this backend's own, with the canonical query as its base. That base is translated by
the same processor the query path uses, so predicates, quantifiers, parameters and every
refusal behave exactly as they do in an ordinary search — only the answer has a different
shape.

## The group key

The key is one attribute, and it has to be a **keyword projection carrying doc values**:
grouping reads one exact term per document out of the doc-values column.

- An **enum, boolean or id** attribute is a keyword by convention, doc values included —
  nothing to declare.
- A **string** is analyzed text by convention and is refused as a key; declare a
  `KeywordFieldMapping` (`docValues="true"`) for it, or a keyword sub-field beside the text
  projection, and group by that.
- A **numeric** attribute is refused: grouping by an exact number is rarely what anyone
  means, and grouping by ranges would need ranges nobody declared.
- A **many-valued** attribute is refused: an object would belong to several groups at once.
  Counting values across objects is what a [facet](./facets.md) does.

**An object without a value for the key is in no group.** It does not form a group of its
own and it does not count towards `totalGroups` — "everything without a manufacturer" is
not a group anyone asked for. If those objects matter, ask for them with a predicate.

## Ordering

Groups come back ordered by their **best hit**, and the representatives inside a group by
relevance, so "the best three of every manufacturer" reads in the order it is meant. Both
orders are ordinal statements about relevance; score values are not a contract.

`topGroups` truncates the answer, and `totalGroups()` tells you that it did — it is the one
number a caller cannot recompute from a truncated list.

## What comes back

Representatives are reconstructed exactly like every other read
(see [loading & materialization](./materialization.md)): partial from stored fields by
default, complete where the mapping declares `STORED_OBJECT`, with the children of `NESTED`
blocks in place.

The result is deliberately **not** a `QueryResult`. A grouped answer is neither the objects
nor the rows the persistence contract knows, and dressing it up as either would misreport
what it is.

## In OSGi

One `GroupSearch` service per mapped index unit, under `search.unit.alias`, like the other
own APIs:

```java
@Reference(target = "(search.unit.alias=catalog)")
GroupSearch grouping;
```
