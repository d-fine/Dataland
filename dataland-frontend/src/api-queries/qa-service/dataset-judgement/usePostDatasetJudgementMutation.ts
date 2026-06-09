import { useMutation, useQueryClient } from '@tanstack/vue-query';
import type { DatasetJudgementResponse } from '@clients/qaservice';
import { useApiClient } from '@/utils/useApiClient.ts';
import { pendingDatasetsKeys } from '@/api-queries/qa-service/pending-datasets/pendingDatasetsKeys.ts';

/**
 * Mutation hook to create a new dataset judgement (review) for a given dataset.
 * On success, invalidates the pending datasets cache so the QA table refreshes automatically.
 */
export function usePostDatasetJudgementMutation(): ReturnType<typeof useMutation<DatasetJudgementResponse, Error, string>> {
  const apiClientProvider = useApiClient();
  const queryClient = useQueryClient();

  return useMutation<DatasetJudgementResponse, Error, string>({
    mutationFn: async (datasetId: string) => {
      const response = await apiClientProvider.apiClients.datasetJudgementController.postDatasetJudgement(datasetId);
      return response.data;
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: pendingDatasetsKeys.all });
    },
    onError: (error) => {
      console.error('Failed to create dataset judgement:', error);
    },
  });
}
