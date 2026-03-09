import { computed, type Ref } from 'vue';
import { useQuery, type UseQueryReturnType } from '@tanstack/vue-query';
import type { DatasetReviewResponse } from '@clients/qaservice';
import { useApiClient } from '@/utils/useApiClient';
import { datasetReviewKeys } from '@/api-queries/qa-service/dataset-review/datasetReviewKeys.ts';

/**
 * Fetch a dataset review by id.
 *
 * @param {{ datasetReviewId: Ref<string> }} options - Reactive ref with the dataset review id.
 * @returns {UseQueryReturnType<DatasetReviewResponse | null, Error>} Vue Query result; `data` holds
 *   the backend response (or null). The query is disabled when `datasetReviewId.value` is falsy.
 */
export function useDatasetReviewQuery(options: {
  datasetReviewId: Ref<string>;
}): UseQueryReturnType<DatasetReviewResponse | null, Error> {
  const apiClientProvider = useApiClient();

  return useQuery<DatasetReviewResponse | null>({
    queryKey: computed(() => datasetReviewKeys.detail(options.datasetReviewId.value)),

    queryFn: async () => {
      const { datasetReviewController } = apiClientProvider.apiClients;
      const id = options.datasetReviewId.value;

      const { data } = await datasetReviewController.getDatasetReview(id);
      return data;
    },
    enabled: computed(() => !!options.datasetReviewId.value),
  });
}
