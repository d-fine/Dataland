import { useMutation, useQueryClient, type UseMutationReturnType } from '@tanstack/vue-query';
import { type Ref } from 'vue';
import { useApiClient } from '@/utils/useApiClient.ts';
import { type DatasetJudgementResponse, type DatasetJudgementState } from '@clients/qaservice';
import { datasetReviewKeys } from '@/api-queries/qa-service/dataset-review/datasetReviewKeys.ts';
import { type AxiosResponse } from 'axios';

/**
 * Set a dataset judgement's state.
 *
 * @param {Ref<string | undefined>} datasetJudgementId - Reactive id of the dataset judgement; mutation throws if undefined.
 * @param {DatasetJudgementState} targetState - State to set on the judgement.
 * @returns {UseMutationReturnType<AxiosResponse<DatasetJudgementResponse>, Error, void, unknown>} Mutation result; on success invalidates the review detail query.
 */
export function useSetDatasetReviewStateMutation(
  datasetJudgementId: Ref<string | undefined>,
  targetState: DatasetJudgementState
): UseMutationReturnType<AxiosResponse<DatasetJudgementResponse>, Error, void, unknown> {
  const apiClient = useApiClient();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async () => {
      const id = datasetJudgementId.value;

      if (id === undefined || id === null) {
        throw new Error('datasetJudgementId is undefined');
      }
      return await apiClient.apiClients.datasetJudgementController.setJudgementState(id, targetState);
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: datasetReviewKeys.detail(datasetJudgementId.value) });
    },
    onError: (error) => {
      console.error('Error setting dataset judgement state:', error);
    },
  });
}
