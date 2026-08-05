---
layout: home

hero:
  name: Fennec EMF Search
  text: Lucene search for EMF models
  tagline: A capability-honest search backend for the Fennec persistence stack — plain Java first, OSGi-ready.
  image:
    src: /fennec-logo.png
    alt: Eclipse Fennec logo
  actions:
    - theme: brand
      text: Overview
      link: /guides/overview
    - theme: alt
      text: Architecture
      link: /guides/architecture
    - theme: alt
      text: View on GitHub
      link: https://github.com/eclipse-fennec/emf.search

features:
  - icon: 🔎
    title: Full-text search over EMF
    details: Ranked full-text, facets, suggest and highlighting over EMF models — declared once in a mapping model instead of hand-coded per use case.
    link: /guides/overview
    linkText: What this is
  - icon: 🧭
    title: Capability-honest
    details: The canonical query IR goes in, EObjects come out. What embedded Lucene cannot answer honestly is refused explicitly rather than faked — consumers route it to the primary store.
    link: /guides/architecture
    linkText: How it works
  - icon: 🧩
    title: Plain Java first, OSGi-ready
    details: Every core is an ordinary Java library with no OSGi imports. Declarative Services are a thin layer on top — the same code runs in and out of a framework.
    link: /guides/architecture
    linkText: The split
---

::: warning Under construction
`emf.search` is in early development — the wave-1 line (index lifecycle, mapping model,
query translation, facets, suggest, highlighting, geo) is being built now. Nothing is
published to Maven Central yet. The
[blueprint](https://github.com/eclipse-fennec/emf.search/blob/snapshot/docs/search-access.md)
and the [issue board](https://github.com/eclipse-fennec/emf.search/issues) are the place to
follow along.
:::
