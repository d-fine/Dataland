import { computed, unref, type MaybeRef } from 'vue';
import { useQuery, type UseQueryReturnType } from '@tanstack/vue-query';
import type { QaReviewResponse } from '@clients/qaservice';
import { useApiClient } from '@/utils/useApiClient.ts';
import { pendingDatasetsKeys } from './pendingDatasetsKeys.ts';

/**
 * Fetches all pending QA datasets, optionally filtered by company name.
 * @param companyNameFilter optional company name search string
 * @param apiClientProvider the API client provider instance
 * @returns the list of pending QA review responses
 */
async function fetchPendingDatasets(
  companyNameFilter: string | undefined,
  apiClientProvider: ReturnType<typeof useApiClient>
): Promise<QaReviewResponse[]> {
  const response = await apiClientProvider.apiClients.qaController.getInfoOnPendingDatasets(companyNameFilter);
  return response.data;
}

/**
 * Query hook for fetching the list of datasets pending QA review.
 * Results are cached per unique company name filter value for 2 minutes,
 * making cache hits directly observable when re-using a previous search term.
 * @param companyNameFilter reactive or static optional company name filter
 * @returns TanStack Query result including data, loading state and dataUpdatedAt timestamp
 */
export function useGetPendingDatasetsQuery(
  companyNameFilter: MaybeRef<string | undefined>
): UseQueryReturnType<QaReviewResponse[], Error> {
  const apiClientProvider = useApiClient();
  const filter = computed(() => unref(companyNameFilter));
  const queryKey = computed(() => pendingDatasetsKeys.byCompanyNameFilter(filter.value));

  return useQuery({
    queryKey,
    queryFn: async () => fetchPendingDatasets(filter.value, apiClientProvider),
    staleTime: 1000 * 60 * 2, // 2 minutes — makes cache hits observable in demos
  });
}
