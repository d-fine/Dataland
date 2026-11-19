import { computed, toValue, type MaybeRefOrGetter } from 'vue';
import { useQuery, type UseQueryOptions, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { enhancedRequestKeys } from '@/api-queries/data-sourcing/enhanced-request/enhancedRequestKeys';
import type { RequestSearchFilterString } from '@clients/datasourcingservice';

/**
 * Vue Query hook that counts the data requests matching the given filters
 * (POST /data-sourcing/enhanced-requests/search/count).
 *
 * @param params - Endpoint parameters.
 * @param params.requestSearchFilterString - Filters the requests have to match. An empty object matches all requests.
 * @param options - Additional TanStack query options.
 * @returns Vue Query result containing the number of matching requests.
 */
export function usePostRequestCountQuery(
  params: { requestSearchFilterString: MaybeRefOrGetter<RequestSearchFilterString> },
  options: Omit<UseQueryOptions<number, Error>, 'queryKey' | 'queryFn'> = {}
): UseQueryReturnType<number, Error> {
  const apiClientProvider = useApiClient();

  return useQuery<number, Error>({
    queryKey: computed(() => enhancedRequestKeys.searchCount(toValue(params.requestSearchFilterString))),
    queryFn: async () => {
      const response = await apiClientProvider.apiClients.enhancedRequestController.postRequestCountQuery(
        toValue(params.requestSearchFilterString)
      );
      return response.data;
    },
    ...options,
  });
}
