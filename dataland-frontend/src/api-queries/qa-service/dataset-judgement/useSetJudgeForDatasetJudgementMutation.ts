import { useMutation, useQueryClient, type UseMutationReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient.ts';
import { datasetJudgementKeys } from '@/api-queries/qa-service/dataset-judgement/datasetJudgementKeys.ts';
import { type DatasetJudgementResponse } from '@clients/qaservice';

export interface SetJudgeForDatasetJudgementVariables {
  datasetJudgementId: string;
}

/**
 * Vue Query mutation hook that assigns the current user as judge of a dataset judgement.
 * On success, the corresponding dataset judgement detail query is invalidated.
 *
 * @returns Mutation result resolving to the updated {@link DatasetJudgementResponse}.
 */
export function useSetJudgeForDatasetJudgementMutation(): UseMutationReturnType<
  DatasetJudgementResponse,
  Error,
  SetJudgeForDatasetJudgementVariables,
  unknown
> {
  const queryClient = useQueryClient();
  const apiClientProvider = useApiClient();

  return useMutation({
    mutationFn: async ({ datasetJudgementId }: SetJudgeForDatasetJudgementVariables) => {
      const response = await apiClientProvider.apiClients.datasetJudgementController.setJudge(datasetJudgementId);
      return response.data;
    },
    onSuccess: async (_data, variables) => {
      await queryClient.invalidateQueries({ queryKey: datasetJudgementKeys.detail(variables.datasetJudgementId) });
    },
  });
}
