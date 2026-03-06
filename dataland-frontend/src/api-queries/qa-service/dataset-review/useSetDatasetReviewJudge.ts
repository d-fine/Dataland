import { useMutation, useQueryClient } from '@tanstack/vue-query';
import type { Ref } from 'vue';
import { useApiClient } from '@/utils/useApiClient.ts';
import { datasetReviewKeys } from '@/api-queries/qa-service/dataset-review/datasetReviewKeys.ts';

export function useSetDatasetReviewJudge(datasetReviewId: Ref<string | undefined>) {
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
