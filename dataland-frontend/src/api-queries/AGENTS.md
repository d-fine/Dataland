# API Queries (TanStack Query)

This directory contains the TanStack Query (`@tanstack/vue-query`) hooks used for all backend communication from Vue components. Follow these rules for every new or modified file here. Some existing files do not comply yet; do not copy their deviations, follow this document instead.

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
- Default `enabled` to "all required parameters are present".
- Explicit return type `UseQueryReturnType<T, Error>`; `T` is the exact response body type (no invented `| null`).

## 4. Mutations

- Signature: `useXMutation()` with no hook arguments. All inputs are passed as a typed variables object to `mutate()` / `mutateAsync()`, with an exported `<Name>Variables` interface.
- `mutationFn` returns `response.data`, never the `AxiosResponse`.
- `onSuccess` only invalidates (or updates) the affected key factory entries.
- No `onError` / `console.error` in the hook. Consumers handle errors (user feedback) via the `mutate` callbacks or the returned `error`.
- Explicit return type `UseMutationReturnType<T, Error, Variables, unknown>`.

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

## 6. API clients

- Obtain clients only via `useApiClient()` from `@/utils/useApiClient`. Never instantiate controllers directly.

## 7. Documentation

- Every exported hook has a short TSDoc describing the endpoint, parameters, and (for mutations) what is invalidated.
