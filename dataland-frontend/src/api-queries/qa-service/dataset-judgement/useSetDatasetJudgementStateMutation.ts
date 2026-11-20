import { useMutation, useQueryClient, type UseMutationReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { datasetJudgementKeys } from '@/api-queries/qa-service/dataset-judgement/datasetJudgementKeys';
import { qaKeys } from '@/api-queries/qa-service/qa/qaKeys';
import { type DatasetJudgementResponse, type DatasetJudgementState } from '@clients/qaservice';

export interface SetDatasetJudgementStateVariables {
  datasetJudgementId: string;
  datasetJudgementState: DatasetJudgementState;
}

/**
 * Vue Query mutation hook that sets the state of a dataset judgement, e.g. to finish a review
 * (PATCH /qa/dataset-judgements/{datasetJudgementId}/state).
 * On success, the dataset judgement detail query and all QA queue queries are invalidated,
 * because a finished review removes the dataset from the queue.
 *
 * @returns Mutation result resolving to the updated {@link DatasetJudgementResponse}.
 */
export function useSetDatasetJudgementStateMutation(): UseMutationReturnType<
  DatasetJudgementResponse,
  Error,
  SetDatasetJudgementStateVariables,
  unknown
> {
  const queryClient = useQueryClient();
  const apiClientProvider = useApiClient();

  return useMutation({
    mutationFn: async ({ datasetJudgementId, datasetJudgementState }: SetDatasetJudgementStateVariables) => {
      const response = await apiClientProvider.apiClients.datasetJudgementController.setJudgementState(
        datasetJudgementId,
        datasetJudgementState
      );
      return response.data;
    },
    onSuccess: async (_data, variables) => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: datasetJudgementKeys.detail(variables.datasetJudgementId) }),
        queryClient.invalidateQueries({ queryKey: qaKeys.all }),
      ]);
    },
  });
}
