import { computed, toValue, type MaybeRefOrGetter } from 'vue';
import { useQuery, type UseQueryOptions, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { qaKeys } from '@/api-queries/qa-service/qa/qaKeys';
import { type QaReviewResponse } from '@clients/qaservice';

/**
 * Vue Query hook that fetches the datasets waiting for quality assurance (GET /qa/datasets/queue).
 *
 * @param params - Endpoint parameters.
 * @param params.companyName - Optional company name or identifier to filter by. All pending datasets if undefined.
 * @param options - Additional TanStack query options.
 * @returns Vue Query result containing the pending datasets as {@link QaReviewResponse}s.
 */
export function useGetInfoOnPendingDatasetsQuery(
  params: { companyName?: MaybeRefOrGetter<string | undefined> } = {},
  options: Omit<UseQueryOptions<QaReviewResponse[], Error>, 'queryKey' | 'queryFn'> = {}
): UseQueryReturnType<QaReviewResponse[], Error> {
  const apiClientProvider = useApiClient();

  return useQuery<QaReviewResponse[], Error>({
    queryKey: computed(() => qaKeys.pendingDatasets(toValue(params.companyName))),
    queryFn: async () => {
      const response = await apiClientProvider.apiClients.qaController.getInfoOnPendingDatasets(
        toValue(params.companyName)
      );
      return response.data;
    },
    ...options,
  });
}
