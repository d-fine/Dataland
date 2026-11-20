import { computed, toValue, type MaybeRefOrGetter } from 'vue';
import { useQuery, type UseQueryOptions, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { nonSourceabilityKeys } from '@/api-queries/backend/non-sourceability/nonSourceabilityKeys';
import type { BasicDataDimensions, DataDimensionSearchRequest } from '@clients/backend';

/**
 * Vue Query hook that fetches the non-sourceable data dimensions matching a search request
 * (POST /non-sourceable/search).
 * The query is disabled while `dataDimensionSearchRequest` is undefined.
 *
 * @param params - Endpoint parameters.
 * @param params.dataDimensionSearchRequest - Filters by company ids, data types and reporting periods.
 * @param options - Additional TanStack query options. A passed `enabled` replaces the default.
 * @returns Vue Query result containing the matching non-sourceable {@link BasicDataDimensions}.
 */
export function useSearchNonSourceableDimensionsQuery(
  params: { dataDimensionSearchRequest: MaybeRefOrGetter<DataDimensionSearchRequest | undefined> },
  options: Omit<UseQueryOptions<Set<BasicDataDimensions>, Error>, 'queryKey' | 'queryFn'> = {}
): UseQueryReturnType<Set<BasicDataDimensions>, Error> {
  const apiClientProvider = useApiClient();

  return useQuery<Set<BasicDataDimensions>, Error>({
    queryKey: computed(() => nonSourceabilityKeys.search(toValue(params.dataDimensionSearchRequest))),
    queryFn: async () => {
      const dataDimensionSearchRequest = toValue(params.dataDimensionSearchRequest);
      if (!dataDimensionSearchRequest) throw new Error('dataDimensionSearchRequest is undefined');
      const response =
        await apiClientProvider.backendClients.nonSourceabilityController.searchNonSourceableDimensions(
          dataDimensionSearchRequest
        );
      return response.data;
    },
    enabled: computed(() => !!toValue(params.dataDimensionSearchRequest)),
    ...options,
  });
}
