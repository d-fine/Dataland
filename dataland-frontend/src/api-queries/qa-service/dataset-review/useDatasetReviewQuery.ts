import { computed, type Ref } from 'vue';
import { useQuery } from '@tanstack/vue-query';
import type { DatasetReviewResponse } from '@clients/qaservice';
import { useApiClient } from '@/utils/useApiClient';
import { datasetReviewKeys } from '@/api-queries/qa-service/dataset-review/datasetReviewKeys.ts';

export function useDatasetReviewQuery(options: { datasetReviewId: Ref<string> }) {
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
