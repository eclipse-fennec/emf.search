// The published, user-facing pages (allowlist). Shared by the sync script and the
// VitePress config so the set and its order are defined exactly once.
//   file  — source markdown in ../docs (the single source of truth)
//   slug  — route name under the section
//   title — sidebar / nav label
//
// GUIDES   -> /guides/   (the user manual)
// EXAMPLES -> /examples/ (worked examples)
//
// Publication is deliberately an allowlist: internal design docs (the
// `search-access.md` blueprint, concept rounds, test plans) stay in ../docs and are
// browsed on GitHub. Add a page here the moment it becomes user-facing — every task
// ships its documentation (see docs/search-access.md §2.2).
export const GUIDES = [
  { file: 'overview.md', slug: 'overview', title: 'Overview' },
  { file: 'architecture.md', slug: 'architecture', title: 'Architecture' },
];

export const EXAMPLES = [];
