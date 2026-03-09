import { useMutation, useQueryClient, type UseMutationReturnType } from '@tanstack/vue-query';
import { type Ref } from 'vue';
import { useApiClient } from '@/utils/useApiClient.ts';
import { type DatasetReviewState } from '@clients/qaservice';
import { datasetReviewKeys } from '@/api-queries/qa-service/dataset-review/datasetReviewKeys.ts';

/**
 * Set a dataset review's state.
 *
 * @param {Ref<string | undefined>} datasetReviewId - Reactive id of the dataset review; mutation throws if undefined.
 * @param {DatasetReviewState} targetState - State to set on the review.
 * @returns {UseMutationReturnType<unknown, Error, void, unknown>} Mutation result; on success invalidates the review detail query.
 */
export function useSetDatasetReviewStateMutation(
  datasetReviewId: Ref<string | undefined>,
  targetState: DatasetReviewState
): UseMutationReturnType<unknown, Error, void, unknown> {
  const apiClient = useApiClient();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async () => {
      const id = datasetReviewId.value;

      if (id === undefined || id === null) {
        throw new Error('datasetReviewId is undefined');
      }
      return await apiClient.apiClients.datasetReviewController.setReviewState(id, targetState);
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: datasetReviewKeys.detail(datasetReviewId.value) });
    },
    onError: (error) => {
      console.error('Error setting dataset review state:', error);
    },
  });
}
