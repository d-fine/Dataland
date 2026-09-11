import { type Ref, computed } from 'vue';
import { useQuery, type UseQueryOptions, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient.ts';
import { nonSourceabilityKeys } from '@/api-queries/backend/non-sourceability/nonSourceabilityKeys.ts';
import type { BasicDataDimensions, DataDimensionSearchRequest } from '@clients/backend';
/**
 * Vue Query hook that fetches non-sourceable data dimensions matching the given search request, grouped by
 * company and framework.
 *
 * Uses POST /non-sourceable/search/grouped.
 *
 * @param request - Reactive search request (companyIds/dataTypes/reportingPeriods filters).
 * @param options - Additional query options (e.g. enabled).
 * @returns Query result containing the matching non-sourceable BasicDataDimensions, grouped first by companyId
 *          and then by framework (dataType).
 */
export function useSearchNonSourceableDimensionsGroupedByCompanyAndFrameworkQuery(
  request: Readonly<Ref<DataDimensionSearchRequest>>,
  options?: Omit<
    UseQueryOptions<Record<string, Record<string, Set<BasicDataDimensions>>>, Error>,
    'queryKey' | 'queryFn'
  >
): UseQueryReturnType<Record<string, Record<string, Set<BasicDataDimensions>>>, Error> {
  const apiClientProvider = useApiClient();
  const queryKey = computed(() => nonSourceabilityKeys.searchGroupedByCompanyAndFramework(request.value));
  return useQuery<Record<string, Record<string, Set<BasicDataDimensions>>>, Error>({
    queryKey,
    queryFn: async () => {
      const response =
        await apiClientProvider.backendClients.nonSourceabilityController.searchNonSourceableDimensionsGroupedByCompanyAndFramework(
          request.value
        );
      return response.data;
    },
    ...options,
  });
}
