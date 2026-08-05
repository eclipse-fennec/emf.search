# Overview

**Fennec EMF Search** (`org.eclipse.fennec.search`) makes [Apache Lucene](https://lucene.apache.org/)
usable as a search backend for [EMF](https://eclipse.dev/modeling/emf/) models — ranked
full-text, facets, suggestions and highlighting over `EObject`s, declared once in a mapping
model instead of hand-coded per use case.

::: warning Status
Early development. The wave-1 line is being built; nothing is published to Maven Central
yet. See the [issue board](https://github.com/eclipse-fennec/emf.search/issues) for what is
in flight, and the [architecture page](./architecture.md) for how the pieces fit.
:::

## What problem this solves

EMF gives you a model. Persistence gives you storage and structured queries. Neither gives
you *"find the twenty most relevant objects for these words"* — and that is what consumers
keep needing: OData `$search`, model catalogue search, any UI with a search box over
modelled data.

Doing it by hand means writing an `EObject` → Lucene `Document` mapper per use case,
maintaining the index on every change, and handing raw Lucene `Query` objects around. That
was the shape of the predecessor project, and it did not scale past its first two consumers.
`emf.search` replaces it with three things:

1. **A declarative mapping model** — which `EClass` becomes which index, which
   `EAttribute` is tokenized, stored, sortable, facetable. Small models need no mapping at
   all; conventions cover them.
2. **Index maintenance behind the store contract** — you save an object, the index follows.
   No hand-built index events.
3. **The canonical Fennec query IR in, `EObject`s out** — the same query language as the
   other Fennec backends, not a Lucene-specific API.

## Two ways to use it

**As a standalone index.** The index *is* the store: objects are saved into it and queried
from it, through the ordinary `PersistenceResource`/`QueryableResource` contracts. Useful
when a Lucene index is genuinely all you need. The contract limits are stated openly —
near-real-time visibility instead of read-your-writes transactions, and reference-heavy
queries are refused rather than emulated.

**As a secondary index next to a primary store** (the common case). JPA or MongoDB stays the
system of record; the Lucene index is fed from the change stream and answers the full-text
part of a query, with hits materialized from the primary store. Index lag exists and is
documented rather than hidden. This mode arrives after the standalone one.

## Capability honesty

The design rule that shapes everything else: **the backend declares what it can do and
refuses the rest explicitly.** Embedded Lucene has no joins, no cross-document paths, no
arithmetic pushdown. A backend that quietly approximates those answers is worse than one
that says no — a refusal is something a consumer can route around (send that predicate to
the primary store, or restructure the query); a wrong answer is not.

Concretely, a query that Lucene cannot honestly answer comes back as a capability refusal
with a diagnostic, not as a partial result set. The one join that *is* offered is an
index-time construction over EMF containment, not a query-time join — see the
[architecture page](./architecture.md).

## Plain Java first, OSGi-ready

Every core is an ordinary Java 21 library: no `org.osgi.*` imports, constructed from plain
config objects, testable with plain JUnit. The OSGi integration — Declarative Services
components, configuration per index unit, whiteboard registration in a
[Fennec EMF OSGi](https://github.com/eclipse-fennec/emf.osgi) runtime — is a thin layer on
top that maps framework configuration onto exactly those objects. The same code runs inside
and outside a framework, which is also why most of the test suite needs no framework at all.

## Where this sits in Fennec

- [`emf.persistence-jpa`](https://github.com/eclipse-fennec/emf.persistence-jpa) owns the
  query IR, the backend SPI, the capability vocabulary and the TCK that this backend is
  verified against. Query vocabulary is never invented here.
- [`emf.osgi`](https://github.com/eclipse-fennec/emf.osgi) provides EMF as OSGi services.
- [`emf.codec`](https://github.com/eclipse-fennec/emf.codec) provides the serialization used
  when a hit is materialized straight from the index.

## Reading further

- [Architecture](./architecture.md) — bundles, mapping model, query translation, the
  plain-Java/OSGi split.
- [The blueprint](./search-access.md)
  — the full internal design document, including the capability profile, the feature radar
  and the task breakdown.
