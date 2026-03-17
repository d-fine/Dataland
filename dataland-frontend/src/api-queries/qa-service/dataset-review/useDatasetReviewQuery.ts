import { computed, type Ref } from 'vue';
import { useQuery, type UseQueryReturnType } from '@tanstack/vue-query';
import type { DatasetJudgementResponse } from '@clients/qaservice';
import { useApiClient } from '@/utils/useApiClient';
import { datasetReviewKeys } from '@/api-queries/qa-service/dataset-review/datasetReviewKeys.ts';

/**
 * Fetch a dataset judgement by id.
 *
 * @param {{ datasetJudgementId: Ref<string> }} options - Reactive ref with the dataset judgement id.
 * @returns {UseQueryReturnType<DatasetJudgementResponse | null, Error>} Vue Query result; `data` holds
 *   the backend response (or null). The query is disabled when `datasetJudgementId.value` is falsy.
 */
export function useDatasetReviewQuery(options: {
  datasetJudgementId: Ref<string>;
}): UseQueryReturnType<DatasetJudgementResponse | null, Error> {
  const apiClientProvider = useApiClient();

  return useQuery<DatasetJudgementResponse | null>({
    queryKey: computed(() => datasetReviewKeys.detail(options.datasetJudgementId.value)),

    queryFn: async () => {
      const { datasetJudgementController } = apiClientProvider.apiClients;
      const id = options.datasetJudgementId.value;

      const { data } = await datasetJudgementController.getDatasetJudgement(id);
      return data;
    },
    enabled: computed(() => !!options.datasetJudgementId.value),
  });
}
