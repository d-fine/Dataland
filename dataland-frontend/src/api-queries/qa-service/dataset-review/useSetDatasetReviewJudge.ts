import { useMutation, useQueryClient, type UseMutationReturnType } from '@tanstack/vue-query';
import type { Ref } from 'vue';
import { useApiClient } from '@/utils/useApiClient.ts';
import { datasetReviewKeys } from '@/api-queries/qa-service/dataset-review/datasetReviewKeys.ts';
import { type DatasetJudgementResponse } from '@clients/qaservice';
import { type AxiosResponse } from 'axios';

/**
 * Set the judge for a dataset judgement.
 *
 * @param {Ref<string | undefined>} datasetJudgementId - Reactive ref with the dataset judgement id.
 * @returns {UseMutationReturnType<AxiosResponse<DatasetJudgementResponse>, Error, void, unknown>} Mutation result; on success it
 * invalidates the corresponding dataset judgement detail query.
 */
export function useSetDatasetReviewJudge(
  datasetJudgementId: Ref<string | undefined>
): UseMutationReturnType<AxiosResponse<DatasetJudgementResponse>, Error, void, unknown> {
  const queryClient = useQueryClient();
  const apiClientProvider = useApiClient();

  return useMutation({
    mutationFn: async () => {
      const id = datasetJudgementId.value;
      if (!id) {
        throw new Error('datasetJudgementId is undefined');
      }
      return apiClientProvider.apiClients.datasetJudgementController.setJudge(id);
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: datasetReviewKeys.detail(datasetJudgementId.value) });
    },
    onError: (error) => {
      console.error('Error setting dataset judge:', error);
    },
  });
}
