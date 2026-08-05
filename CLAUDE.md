# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repository is

`emf.search` — embedded Lucene as a **capability-honest search backend** for the Fennec persistence stack
(Eclipse Fennec). Two roles: a standalone index that *is* a `QueryableResource`/`PersistenceResource`, and (v2)
a secondary index next to JPA/Mongo fed by the change stream. It replaces the retired `org.gecko.search`
architecture.

**Read `docs/search-access.md` first** — it is the blueprint and the source of truth for the mission, the
planned bundle layout, the `esearch.ecore` mapping model, the Lucene capability profile (§5), the feature radar
(§7, wave 1 vs. wave 2) and the S1–S19 issue breakdown (§8). Three rules from it that constrain almost every
change here:

- **Missing query vocabulary is never invented in this repo.** New IR concepts go to `emf.persistence-jpa` as
  an issue (the route SCORE and geo took); only engine-specific machinery (analyzers, suggest, highlighting,
  similarity) lives here.
- **Refusal is a feature.** What Lucene cannot do honestly (query-time joins, cross-document paths, arithmetic
  pushdown) is declared unsupported via `QueryCapabilities`, not faked. The one join that *is* offered is the
  index-time block join over containment (§5.2).
- **Wave 2 is reserved, not started.** Vector/KNN search, standing queries (`lucene-monitor`), analysis-chain
  enrichment and classification are planned but out of scope until wave 1 lands; the metamodel keeps a reserved
  vector-field slot so adding them stays additive.

**Current state: scaffold only** (workspace + CI are set up; no bundle projects exist yet — S1 of the
blueprint). Two consequences:

- Apart from `search-access.md`, `docs/` was carried over from the `emf.util` template workspace and describes
  utilities (protobuf, SOAP, OpenAPI, sensinact mapping) that are *not* in this repository — style reference
  only, replace as real content appears. `docs-site/` (VitePress) is empty and built by CI.
- `build.gradle`'s `coverageFloorBundles` list is intentionally empty; add a bundle name there once it carries
  plain-JUnit tests (30% instruction floor, wired into `check`).

## Reference repositories (checked out locally)

Code lives in sibling clones under `/opt/git`; see `docs/search-access.md` §2.1 for what each contributes.

| Path | Why you'd open it |
|---|---|
| `/opt/git/emf.persistence-jpa` | The consumed contracts: `org.eclipse.fennec.query.model` + `expression.model` (IR), `persistence.query` (`QueryProcessor` SPI, capabilities), `persistence` (`PersistenceResource`), `persistence.tck` (the TCK this repo binds). `persistence.mongo` is the closest precedent for a capability-limited backend; `persistence.orm` for the mapping-processor pipeline. Design companions in its `docs/unified-persistence/`. |
| `/opt/git/emf.osgi` | The EMF-in-OSGi runtime (`ResourceSet`/`EPackage`/`Resource.Factory` as services) and `emf.osgi.codegen`, used for `esearch.ecore`. |
| `/opt/git/emf.codec` | Precedents for the `_type` discriminator and for config resolution / annotation scoping of mapping declarations. |
| `/opt/git/org.gecko.search` | The retired predecessor. Mine the Lucene lifecycle mechanics (`LuceneIndexImpl`, `SearcherManager`/NRT, `CommitCallback`, suggest); do not carry over `IndexContextObject`/`ContextObjectFactory` or raw-`Query`/raw-`Document` APIs. |
| `/opt/git/org.gecko.libraries` | Source of the Lucene OSGi bundles (below). |

**Lucene dependencies** come — for now — from the `org.gecko.libraries` workspace as
`org.geckoprojects.libraries:org.apache.lucene.*` at Lucene **9.12.3** (`org.apache.lucene.core` bundles core +
analysis-common; separate bundles for facet, suggest, queryparser, spatial, highlighter, …). These supersede
the older `org.geckoprojects.search:*` 9.12.0 bundles used by `org.gecko.search`. Add the coordinates to
`cnf/ext/central.mvn` before putting them on a `-buildpath`.

## Workspace model (bnd + Gradle hybrid)

This is a **bnd workspace driven by Gradle** via the `biz.aQute.bnd.workspace` plugin. The most important
consequence: *bnd owns the dependencies and the project graph, Gradle only drives the build.*

- **Projects are discovered, not declared.** Any top-level directory containing a `bnd.bnd` automatically
  becomes a Gradle subproject — `settings.gradle` lists none of them. Non-project root folders must be listed
  in `bnd_exclude` in `gradle.properties` (currently `build,docs,docs-site`).
- **Dependencies come from bnd, not Gradle.** To use a new library: add its Maven coordinate to
  `cnf/ext/central.mvn`, then reference the bundle on `-buildpath` (and `-runrequires` for runtime) in the
  project's `bnd.bnd`, typically with `;version=latest`. The `dependencies` block in `build.gradle` exists only
  to supply JUnit/Mockito/AssertJ to plain (non-OSGi) Gradle tests.
- **Fennec bnd libraries** are enabled through `-library:` in `cnf/ext/fennec.bnd` (`fennec`, `fennecTest`,
  `fennecJacoco`, `fennecEMF`, `fennecM2X`, `fennecJPA`, `fennecEMFModels`, `fennecCodec`) and resolved from
  Maven Central via `central.mvn`.
- **Project coordinates have a single source: `gradle.properties`** (`github_org`, `github_repository`,
  `maven_group_id`). Gradle reads it natively; bnd imports the same file via `-include` in `cnf/ext/fennec.bnd`,
  which derives `github-orga`, `github-project` and `-groupid`. Never duplicate these values elsewhere.
- **Toolchain:** Java 21 (`javac.source`/`javac.target` in `cnf/ext/fennec.bnd`), bnd `7.4.0-SNAPSHOT` from the
  bndtools JFrog snapshot repo. Do not downgrade to 7.3.0 — it has a `-pom` snapshot-version regression that
  breaks bundle jar builds with the fennec library (see the comment in `gradle.properties`).

## Commands

```bash
./gradlew build                          # compile + unit tests + coverage checks
./gradlew build testOSGi                 # incl. OSGi integration tests (bndrun-launched)
./gradlew :<project>:test --tests '*Name*'   # a single plain-JUnit test
./gradlew :<project>:testOSGi            # OSGi tests of one bundle
./gradlew resolve                        # recompute -runbundles in the .bndrun files
./gradlew clean build
```

Test tags (configured in the root `build.gradle`, excluded from the normal `test` task):

```bash
./gradlew perfTest      # @Tag("perf")   — slow scaling tests; structural assertions gate, timing only logs
./gradlew remoteTest    # @Tag("remote") — hits public endpoints; needs internet, never gates the build
```

A `fennec-bnd-osgi` skill is available and covers this workspace layout, `-generate`/EMF codegen, `resolve`, and
`org.osgi.test` integration tests in more depth.

## Conventions

- **Work is tracked as issues** in `eclipse-fennec/emf.search`: #1 (epic: foundation, S1–S5+S11), #2 (epic:
  wave-1 features), #3 (tracking: gated + wave 2). Each task issue carries its blueprint section; the S-numbers
  in `docs/search-access.md` §8 carry the issue number.
- **OSGi-ready, OSGi-optional** (`docs/search-access.md` §2.2): no `org.osgi.*` in a core bundle; index units,
  analyzers and registries are constructed from plain config objects, and the `.osgi` bundle only maps DS
  config onto them. Every whiteboard lookup needs a programmatic counterpart for plain Java — and plain-Java
  design must not introduce static state that breaks OSGi dynamics.
- **Plain JUnit first.** Mapping, translation, 3VL, block joins, facets, suggest, highlighting, scoring and the
  TCK binding are plain JUnit 5 against `ByteBuffersDirectory`/`MemoryIndex`. `testOSGi` is for wiring only:
  component activation, config, `Resource.Factory` discovery through a `ResourceSet`, service dynamics. If an
  OSGi test asserts search behaviour, it belongs in a plain-JUnit test.
- **Docs ship with the change**: markdown in `docs/` (source of truth) plus an allowlist entry in
  `docs-site/guides.mjs` when user-facing — the emf.util wiring (`sync-guides.mjs`, VitePress). Internal design
  docs stay in `docs/` unpublished and are link-rewritten to GitHub blob URLs.
- **Planned bundles** (`docs/search-access.md` §2), group id `org.eclipse.fennec.search`:
  `…search.model` (`esearch.ecore` + generated code), `…search.lucene` (backend), `…search.suggest`,
  `…search.index` (v2 stream-fed secondary index), `…search.tck` (binding of the published persistence TCK).
  Fennec house style within a bundle family: plain-Java core usable without OSGi, `.tests` for plain JUnit, a
  thin DS/OSGi layer that wraps the core and never leaks into it, `.osgi.tests` run via `testOSGi`.
- **Consuming the persistence stack**: add
  `org.eclipse.fennec.persistence:org.eclipse.fennec.persistence.workspace.library:<version>` to
  `cnf/ext/central.mvn` and enable `-library: fennecPersistence` in `cnf/ext/fennec.bnd` — never source-copy
  contracts from the local `emf.persistence-jpa` clone.
- **License headers**: every `.java` file needs the EPL-2.0 header from `.licenserc.yaml` (checked in CI by
  skywalking-eyes). Generated sources, `cnf/**`, `docs-site/**`, and most non-Java file types are exempt.

## Branches and CI

- `snapshot` (the current branch) → publishes SNAPSHOTs; `main` → releases to Maven Central. Every other branch
  and all PRs run verify only.
- All workflows delegate to pinned reusable workflows in `eclipse-fennec/.github`, so CI behaviour is not
  configured in this repo — only which reusable runs when.
