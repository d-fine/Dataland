import { type Ref, computed } from 'vue';
import { useQuery, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient.ts';
import { enhancedRequestsKeys } from '@/api-queries/data-sourcing/enhanced-request/enhancedRequestsKeys.ts';
import type { RequestSearchFilterString } from '@clients/datasourcingservice';

/**
 * Vue Query hook that fetches the number of enhanced requests based on filters.
 *
 * Uses POST /enhanced-requests/search/count.
 *
 * @param filters - Reactive request search filters.
 * @returns Query result containing the number of matching requests.
 */
export function usePostEnhancedRequestsSearchCountQuery(
  filters: Readonly<Ref<RequestSearchFilterString>>
): UseQueryReturnType<number, Error> {
  const apiClientProvider = useApiClient();

  const queryKey = computed(() => enhancedRequestsKeys.searchCount(filters.value));

  return useQuery<number, Error>({
    queryKey,
    queryFn: async () => {
      const response = await apiClientProvider.apiClients.enhancedRequestController.postRequestCountQuery(
        filters.value
      );

      return response.data;
    },
  });
}
