# API Queries (TanStack Query)

This directory contains the TanStack Query (`@tanstack/vue-query`) hooks used for all backend communication from Vue components. This document is the authoritative server-state standard for `dataland-frontend`: sections 1-7 apply to every new or modified file here, section 8 applies to every component or composable that consumes these hooks, and section 9 applies to their component tests. Some existing files do not comply yet; do not copy their deviations, follow this document instead. To migrate a legacy component, follow the `dataland-tanstack-query-migration` skill.

## Reference files

- Query: `backend/data-point/useGetDataPointByIdQuery.ts`
- Mutation: `qa-service/dataset-judgement/useSetJudgeForDatasetJudgementMutation.ts`
- Key factory: `backend/data-point/dataPointKeys.ts`

## 1. One hook = one endpoint, no logic

- `queryFn` / `mutationFn` calls exactly one generated client method and returns `response.data`.
- No filtering, mapping, defaulting, sorting, batching, or orchestration of multiple calls inside a hook.
- Transformations belong to the consumer (`computed`) or to a `select` passed in via `options`.
- Multi-call workflows (e.g. "accept all remaining data points") belong in a feature composable outside `api-queries/` that composes the simple hooks.
- Keeping hooks thin makes them reusable and keeps the query cache consistent for every consumer.
- Narrow exception: endpoints that answer a yes/no question with a status code (e.g. a `HEAD` request that returns 404 for "no") map exactly that status to `false` in `queryFn` and throw all other errors, so that the answer is cached like any other result. Reference: `community-manager/company-roles/useHasCompanyAtLeastOneOwnerQuery.ts`.

## 2. Location and naming

- Path: `src/api-queries/<service>/<resource>/`, e.g. `qa-service/dataset-judgement/`.
- Queries: `use<Verb><Resource>[By<X>]Query.ts`, mutations: `use<Verb><Resource>Mutation.ts`.
- The verb and name should map recognisably to the generated client method (`getDataPoint` -> `useGetDataPointByIdQuery`, `setJudge` -> `useSetJudgeForDatasetJudgementMutation`).
- One exported hook per file, file name equals hook name.

## 3. Queries

- Signature: `useXQuery(params, options?)`.
  - `params` is a single object of endpoint parameters, each typed as `MaybeRefOrGetter<T>` and read with `toValue()`. Omit `params` only if the endpoint has no parameters.
  - `options` is optional and typed `Omit<UseQueryOptions<T, Error>, 'queryKey' | 'queryFn'>`, spread last into `useQuery`. A passed `enabled` replaces the default.
- `queryKey` is a `computed` from the key factory and includes every parameter that affects the response.
- Default `enabled` to "all required parameters are present". If all parameters are optional filters and an unfiltered request would list everything on Dataland, default to "at least one filter is set" (e.g. `useSearchForDocumentMetaInformationQuery`). Endpoints whose unfiltered result is intended (e.g. the company search) stay enabled.
- If the generated client expects a `Set`, accept an array in `params` and convert it in `queryFn`; the key factory sorts it (section 5).
- Explicit return type `UseQueryReturnType<T, Error>`; `T` is the exact response body type (no invented `| null`).

## 4. Mutations

- Signature: `useXMutation()` with no hook arguments. All inputs are passed as a typed variables object to `mutate()` / `mutateAsync()`, with an exported `<Name>Variables` interface.
- `mutationFn` returns `response.data`, never the `AxiosResponse`.
- `onSuccess` only invalidates (or updates) the affected key factory entries, including those of other resources whose responses depend on the mutated data.
- By default, `await` the invalidation in `onSuccess`: the mutation stays pending until active queries are refetched, so consumer callbacks see fresh data. Do not await it (`void`) if consumers usually navigate away afterwards or if the refetched data can unmount the calling component (e.g. users removing their own access). TanStack skips `mutate` callbacks of unmounted components.
- No `onError` / `console.error` in the hook. Consumers handle errors (user feedback) via the `mutate` callbacks or the returned `error`.
- Explicit return type `UseMutationReturnType<T, Error, Variables, unknown>`.
- Use a mutation also for POST endpoints that only compute or validate on an explicit user action and whose result is not shared via the cache (e.g. `backend/company-data/usePostCompanyValidationMutation.ts`). Such hooks have no `onSuccess`.
- If refetching an affected query would fail (e.g. the deleted resource itself), invalidate only the lists that contain it.

## 5. Key factories

- One `<resource>Keys.ts` next to the hooks, exporting `<resource>Keys`.
- Hierarchical and built on `all`, so `invalidateQueries({ queryKey: xKeys.all })` hits everything of the resource:

```ts
export const dataPointKeys = {
  all: ['dataPoint'] as const,
  byDataPointId: (dataPointId: string | undefined) => [...dataPointKeys.all, 'byDataPointId', dataPointId] as const,
};
```

- The root segment matches the resource name; never reuse a root across resources.
- Key segments must be JSON-serialisable. Objects are hashed by value (property order and `undefined` fields do not matter for identical keys); a `Set` serialises to `{}`, so put it into the key as a sorted array.
- Invalidation matches key prefixes, and object segments match if the filter's properties match. An explicit `undefined` in a filter object must match as well, so when invalidating a subset, leave unset properties out of the filter or invalidate a higher-level key such as `all`.

## 6. API clients

- Obtain clients only via `useApiClient()` from `@/utils/useApiClient`. Never instantiate controllers directly.

## 7. Documentation

- Every exported hook has a short TSDoc describing the endpoint, parameters, and (for mutations) what is invalidated.

## 8. Using hooks in components

Hooks own server state; components only derive UI from it.

- **No mirrored state.** Do not copy `data` into local refs, and do not keep manual loading flags such as `waitingForData`. Use the hook result:
  - `isPending`: no data yet (spinner). A disabled query without data stays pending, so check the enabling condition as well if a query may be disabled permanently (e.g. for anonymous users).
  - `isFetched`: the query has completed at least once (success or error). Use it to detect "settled" before side effects; with retries, it only becomes true after the last attempt.
  - `isError` / `error`: error UI. Show a message; never leave a spinner running on failure. A failed background refetch keeps the previous `data`, so keep showing that data instead of replacing it with the error (e.g. `v-if="isError && !data"`).
  - `isFetching`: any request in flight, including background refreshes of shown data. Rarely needed in the UI.
- **Derive, don't fetch imperatively.** Filtering, sorting, and client-side paging are `computed` over `data`. Do not call `refetch()` from watchers or `onMounted`.
- **Reactive params drive fetching.** Pass refs, computeds, or getters as params; a changed key triggers the fetch. Values that are only known asynchronously (Keycloak user id, route params) go in as params and the default `enabled` keeps the query idle until they exist. Extra conditions (role, authentication) go into `options.enabled`. Get the user id and Keycloak roles from `useCurrentUser()` in `@/utils/useCurrentUser`; it is empty for anonymous users, so user-specific queries stay disabled for them.
- **Share queries instead of narrowing them.** If a component needs a subset of data that another component already loads (e.g. the current user's role for one company), call the same hook with the same params and filter in a `computed`, so that both share one request and one cache entry.
- **Applied vs. edited input.** If filters only take effect on an explicit action (button, Enter), keep the live form state separate from an `appliedFilters` ref that is assigned on submit and passed to the hook. Debounce free-text search in the component (e.g. `refDebounced` from `@vueuse/core`) and pass the debounced value.
- **Paged or filtered lists** pass `placeholderData: keepPreviousData`, so the current rows stay visible while the next page or filter result loads. Indicate that state with `isPlaceholderData` (e.g. DataTable `:loading`), not with `isFetching`, so background refreshes stay unobtrusive. Reset the page index in the same handler that applies a new search or filter, so that only the first page of the new result is requested.
- **URL state.** If applied filters live in the URL, write the URL only from user actions. Apply filters coming from a navigation (back button, links) without pushing the URL again.
- **Statuses that will not change.** Queries retry failed requests three times. If a component shows a dedicated UI for a status that a retry cannot change (e.g. 404 "not found"), pass a `retry` function that skips this status (see `CompanyInformation.vue`).
- **Refresh through invalidation.** After a mutation, the hook's `onSuccess` invalidation updates every affected query. Do not refetch manually, emit "please reload" events, or force remounts with `:key` counters.
- **Side effects on loaded data** (navigation, selecting an initial tab) react to query state with `watch` / `whenever` from `@vueuse/core`; use `computed` for everything that can be derived.
- **Mutation feedback is the component's job.** Use `mutate(variables, { onSuccess, onError })`, `mutateAsync` in `try`/`catch`, or the returned `isPending` / `error`.
- **Destructure only what you use**: `const { data, isPending, isError } = useXQuery(...)`. Destructured fields are refs.
- **Legacy children**: if a child component still writes via API clients and only reports success through an event, invalidate the affected key factory entries in the parent's event handler until the child is migrated. Never add new legacy children.

Reference components:

- `src/components/pages/MyDataRequestsOverview.vue`: single query, client-side filtering and paging via `computed`.
- `src/components/pages/AdminAllRequestsOverview.vue`: applied filters, server-side paging, `keepPreviousData`.
- `src/components/pages/QualityAssurance.vue`: debounced search as query param, mutation with navigation on success.
- `src/components/resources/portfolio/PortfolioDialog.vue`: create, replace and delete mutations with component-side error handling.
- `src/components/pages/CompanyCockpitPage.vue`: query params resolved from Keycloak, side effect (initial tab) after queries settled.
- `src/components/pages/CompanyInformation.vue`: dependent queries (parent company), shared role query filtered per company, dedicated 404 state.
- `src/components/pages/SearchCompaniesForFrameworkData.vue`: applied search with URL sync, server-side paging with `keepPreviousData`, controlled filter component.

## 9. Tests

- `cy.mountWithPlugins(Component, { keycloak: minimalKeycloakMock({}) })` and `getMountingFunction({ keycloak })` install `VueQueryPlugin` with a fresh `QueryClient` per mount (retries disabled) and provide `apiClientProvider`. Never install `VueQueryPlugin` in a test yourself; pass `queryClient` in the mount options only if the test must seed or inspect the cache.
- Stub endpoints with `cy.intercept`; do not mock hooks.
- For migrated components, cover the loading state (delayed intercept), the error state (`statusCode: 500`), and, for mutations, that the affected queries are requested again (`cy.wait` on the list intercept).
- For pages users return to, cover the cache: mount with a shared `queryClient`, unmount, delay the next response, mount again and assert the content with a short timeout while the refetch is pending (e.g. `CompanyInformation.cy.ts`). A second `mount` in the same test does not unmount the first component.
- A `retry` option passed by the component overrides the disabled retries of the test client. Drive the retry delays with `cy.clock()` and `cy.tick()` instead of waiting for them.
