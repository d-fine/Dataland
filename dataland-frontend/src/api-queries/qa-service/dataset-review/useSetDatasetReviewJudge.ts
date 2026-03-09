import { useMutation, useQueryClient, type UseMutationReturnType } from '@tanstack/vue-query';
import type { Ref } from 'vue';
import { useApiClient } from '@/utils/useApiClient.ts';
import { datasetReviewKeys } from '@/api-queries/qa-service/dataset-review/datasetReviewKeys.ts';
import { DatasetReviewResponse } from '@clients/qaservice';
import { AxiosResponse } from 'axios';

/**
 * Set the judge for a dataset review.
 *
 * @param {Ref<string | undefined>} datasetReviewId - Reactive ref with the dataset review id.
 * @returns {UseMutationReturnType<AxiosResponse<DatasetReviewResponse>, Error, void, unknown>} Mutation result; on success it
 * invalidates the corresponding dataset review detail query.
 */
export function useSetDatasetReviewJudge(
  datasetReviewId: Ref<string | undefined>
): UseMutationReturnType<AxiosResponse<DatasetReviewResponse>, Error, void, unknown> {
  const queryClient = useQueryClient();
  const apiClientProvider = useApiClient();

  return useMutation({
    mutationFn: async () => {
      const id = datasetReviewId.value;
      if (!id) {
        throw new Error('datasetReviewId is undefined');
      }
      return apiClientProvider.apiClients.datasetReviewController.setReviewer(id);
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: datasetReviewKeys.detail(datasetReviewId.value) });
    },
    onError: (error) => {
      console.error('Error setting dataset review judge:', error);
    },
  });
}
