import { computed, toValue, type MaybeRefOrGetter } from 'vue';
import { useQuery, type UseQueryOptions, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { enhancedRequestKeys } from '@/api-queries/data-sourcing/enhanced-request/enhancedRequestKeys';
import type { DataSourcingEnhancedRequest, RequestSearchFilterString } from '@clients/datasourcingservice';

/**
 * Vue Query hook that searches data requests matching the given filters, one chunk at a time
 * (POST /data-sourcing/enhanced-requests/search).
 *
 * @param params - Endpoint parameters.
 * @param params.requestSearchFilterString - Filters the requests have to match. An empty object matches all requests.
 * @param params.chunkSize - Maximum number of requests per chunk. Uses the backend default if undefined.
 * @param params.chunkIndex - Zero-based index of the chunk to fetch. Uses the backend default if undefined.
 * @param options - Additional TanStack query options.
 * @returns Vue Query result containing the matching {@link DataSourcingEnhancedRequest}s of the chunk.
 */
export function usePostRequestSearchQuery(
  params: {
    requestSearchFilterString: MaybeRefOrGetter<RequestSearchFilterString>;
    chunkSize?: MaybeRefOrGetter<number | undefined>;
    chunkIndex?: MaybeRefOrGetter<number | undefined>;
  },
  options: Omit<UseQueryOptions<DataSourcingEnhancedRequest[], Error>, 'queryKey' | 'queryFn'> = {}
): UseQueryReturnType<DataSourcingEnhancedRequest[], Error> {
  const apiClientProvider = useApiClient();

  return useQuery<DataSourcingEnhancedRequest[], Error>({
    queryKey: computed(() =>
      enhancedRequestKeys.search(
        toValue(params.requestSearchFilterString),
        toValue(params.chunkSize),
        toValue(params.chunkIndex)
      )
    ),
    queryFn: async () => {
      const response = await apiClientProvider.apiClients.enhancedRequestController.postRequestSearch(
        toValue(params.requestSearchFilterString),
        toValue(params.chunkSize),
        toValue(params.chunkIndex)
      );
      return response.data;
    },
    ...options,
  });
}
