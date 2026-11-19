import { useMutation, useQueryClient, type UseMutationReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { qaKeys } from '@/api-queries/qa-service/qa/qaKeys';
import { type DatasetJudgementResponse } from '@clients/qaservice';

export interface PostDatasetJudgementVariables {
  datasetId: string;
}

/**
 * Vue Query mutation hook that starts the review of a dataset by creating a dataset judgement
 * (POST /qa/dataset-judgements/{datasetId}).
 * On success, all QA queue queries are invalidated because the dataset now has an ongoing review.
 *
 * @returns Mutation result resolving to the created {@link DatasetJudgementResponse}.
 */
export function usePostDatasetJudgementMutation(): UseMutationReturnType<
  DatasetJudgementResponse,
  Error,
  PostDatasetJudgementVariables,
  unknown
> {
  const queryClient = useQueryClient();
  const apiClientProvider = useApiClient();

  return useMutation({
    mutationFn: async ({ datasetId }: PostDatasetJudgementVariables) => {
      const response = await apiClientProvider.apiClients.datasetJudgementController.postDatasetJudgement(datasetId);
      return response.data;
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: qaKeys.all });
    },
  });
}
