---
name: dataland-tanstack-query-migration
description: Migrate a dataland-frontend component from direct API-client calls to TanStack Query hooks in src/api-queries
compatibility: opencode
---

## What I do

- Move the server state of a `dataland-frontend` component (loading and writing backend data) from direct `ApiClientProvider` / `useApiClient()` calls to TanStack Query hooks.
- Apply the standard in `dataland-frontend/src/api-queries/AGENTS.md`. That file defines the rules; this skill defines the process.

## When to use me

- The user asks to migrate a page, component, or composable to TanStack Query.
- A change needs new backend calls in a component that still loads data imperatively and the user agreed to migrate it.
- Not for Options API components: convert them first with the `vue-composition-api-refactor` agent, then migrate in a separate step.

## Workflow

1. **Read the standard**: `dataland-frontend/src/api-queries/AGENTS.md`, including section 8 (usage in components) and the reference files it lists.
2. **Inventory the component**: for every backend call list client, method, arguments, trigger (`onMounted`, watcher, event, dialog `onClose`), the refs holding results, loading/error flags, and how the view refreshes after writes (emits, `:key` counters, manual refetches). Include calls hidden in `src/utils` helpers.
3. **Fix the boundary**: migrate the component and the children whose writes drive its refresh. Leave children that are shared with other pages as they are unless asked, and list them as follow-ups.
4. **Reuse hooks**: search `src/api-queries/` for a hook that wraps the same client method. If a hook you reuse or a key factory you extend violates the standard, fix it and update all of its consumers.
5. **Add missing hooks** under `src/api-queries/<service>/<resource>/`: one hook per endpoint, key factory entries for every query, and invalidation of every affected key in each mutation's `onSuccess`.
6. **Rewrite the component** following section 8: hook results instead of mirrored refs, `computed` for derived data, reactive params instead of watchers that fetch, `keepPreviousData` for paged or filtered lists, invalidation instead of reload emits, and user feedback for mutation errors.
7. **Delete dead code**: refs, emits, props, and `src/utils` helpers that lost their last caller. Search the whole `src/` tree before deleting.
8. **Update tests** (section 9 of the standard): stub endpoints with `cy.intercept`, add loading and error cases, and assert that mutations trigger the affected list requests again.
9. **Check E2E specs**: search `dataland-frontend/tests/e2e` for the migrated endpoint URLs. Specs that `cy.wait` on these requests must still see them in the same order (e.g. the list request after a save).

## Pitfalls

- Queries retry three times by default, so failures take several seconds to surface in the app. Component tests run without retries via the mount helpers.
- `staleTime` is `0`: returning to a page shows cached data immediately and refetches in the background. Do not add `refetchOnMount: 'always'` or `gcTime: 0` without a concrete reason.
- `Set` parameters serialise to `{}` in query keys; use sorted arrays in the key.
- Resolve Keycloak-derived values (user id) once into a ref and pass it as a parameter; do not await Keycloak inside `queryFn`.
- Use `watch` on query data only for side effects that cannot be computed, such as selecting a default tab after the list changed.
- A component cannot `inject` its own `provide`, so hooks cannot be called in `App.vue` itself.

## Verification

Run in `dataland-frontend`:

- `npm run lint`
- `npm run typecheck`
- `npm run formatci`
- `npx cypress run --component --spec <specs of the migrated components and of all other consumers of changed hooks>`
- `npm run checkcypresscompilation` when Cypress TypeScript inputs changed

## Output style

- List new and changed hooks and key factories.
- List migrated components and removed code.
- List changed or added tests and the verification results.
- List remaining direct API-client usage in the migrated component tree as follow-ups.
