import { type Ref, computed } from 'vue';
import { useQuery, type UseQueryOptions, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient.ts';
import { nonSourceabilityKeys } from '@/api-queries/backend/non-sourceability/nonSourceabilityKeys.ts';
import type { BasicDataDimensions, DataDimensionSearchRequest } from '@clients/backend';

/**
 * Vue Query hook that fetches the set of non-sourceable data dimensions matching the given search request.
 *
 * Uses POST /non-sourceable/search.
 *
 * @param request - Reactive search request (companyIds/dataTypes/reportingPeriods filters).
 * @param options - Additional query options (e.g. enabled).
 * @returns Query result containing the matching non-sourceable BasicDataDimensions.
 */
export function useSearchNonSourceableDimensionsQuery(
  request: Readonly<Ref<DataDimensionSearchRequest>>,
  options?: Omit<UseQueryOptions<Set<BasicDataDimensions>, Error>, 'queryKey' | 'queryFn'>
): UseQueryReturnType<Set<BasicDataDimensions>, Error> {
  const apiClientProvider = useApiClient();
  const queryKey = computed(() => nonSourceabilityKeys.search(request.value));

  return useQuery<Set<BasicDataDimensions>, Error>({
    queryKey,
    queryFn: async () => {
      const response = await apiClientProvider.backendClients.nonSourceabilityController.searchNonSourceableDimensions(
        request.value
      );
      return response.data;
    },
    ...options,
  });
}
