import { useMutation, useQueryClient } from '@tanstack/vue-query';
import { Ref } from 'vue';
import { useApiClient } from '@/utils/useApiClient.ts';
import { DatasetReviewState } from '@clients/qaservice';
import { datasetReviewKeys } from '@/api-queries/qa-service/dataset-review/datasetReviewKeys.ts';

export function useSetDatasetReviewStateMutation(
  datasetReviewId: Ref<string | undefined>,
  targetState: DatasetReviewState
) {
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
