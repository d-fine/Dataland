# Shared Elements Package

- This package contains shared frontend code consumed by `dataland-frontend` and `dataland-website`.
- Keep changes minimal and compatibility-aware because multiple packages depend on these components.
- Use Vue 3 and TypeScript for shared UI code.

# Commands

- Install dependencies: `npm install`
- Verify the package builds: `npm run build`

# Verification

- Always run `npm run build` after changing shared elements.
- There is no dedicated CI job for this package; verify it through the affected consumers.
- Also run the relevant checks in each consumer affected by the change:
  - `dataland-frontend`: `npm --prefix ../dataland-frontend run lint`, `npm --prefix ../dataland-frontend run typecheck`, `npm --prefix ../dataland-frontend run build`
  - `dataland-website`: `npm --prefix ../dataland-website run lintci`, `npm --prefix ../dataland-website run formatci`, `npm --prefix ../dataland-website run typecheck`

# Package-Specific Rules

- Prefer extending existing shared components and exports instead of adding parallel variants.
- Be careful with public interfaces and exported paths because downstream packages import this package directly.
- When in doubt, verify both consuming packages before finishing.
