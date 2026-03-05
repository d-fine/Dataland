import { computed, type Ref } from 'vue';
import { useQuery } from '@tanstack/vue-query';
import type { DatasetReviewResponse } from '@clients/qaservice';
import { useApiClient } from '@/utils/useApiClient';
import { datasetReviewKeys } from '@/api-queries/qa-service/dataset-review/datasetReviewKeys.ts';

export function useDatasetReviewQuery(options: {
  dataId: Ref<string | undefined>;
  datasetReviewId?: Ref<string | undefined>;
}) {
  const apiClientProvider = useApiClient();

  return useQuery<DatasetReviewResponse | null>({
    queryKey: computed(() =>
      options.datasetReviewId?.value
        ? datasetReviewKeys.detail(options.datasetReviewId.value)
        : datasetReviewKeys.listByDataId(options.dataId.value)
    ),

    queryFn: async () => {
      const { datasetReviewController } = apiClientProvider.apiClients;
      const dataId = options.dataId.value;
      const datasetReviewId = options.datasetReviewId?.value;

      if (datasetReviewId) {
        const { data } = await datasetReviewController.getDatasetReview(datasetReviewId);
        return data;
      }

      if (dataId) {
        const { data } = await datasetReviewController.getDatasetReviewsByDatasetId(dataId);
        return data[0] ?? null;
      }

      return null;
    },
    enabled: computed(() => !!options.dataId.value || !!options.datasetReviewId?.value),
  });
}
