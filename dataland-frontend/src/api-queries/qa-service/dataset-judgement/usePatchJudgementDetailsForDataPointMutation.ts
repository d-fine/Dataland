import { useApiClient } from '@/utils/useApiClient.ts';
import { type DatasetJudgementResponse, type JudgementDetailsPatch } from '@clients/qaservice';
import { useMutation, type UseMutationReturnType, useQueryClient } from '@tanstack/vue-query';
import { datasetJudgementKeys } from '@/api-queries/qa-service/dataset-judgement/datasetJudgementKeys.ts';
import { type AxiosResponse } from 'axios';

export interface PatchJudgementArgs {
  judgementId: string;
  dataPointTypeId: string;
  details: JudgementDetailsPatch;
}

/**
 * Vue Query mutation hook to patch judgement details for a specific data point type within a dataset judgement.
 */
export function usePatchJudgementDetailsForDataPointMutation(): UseMutationReturnType<
  AxiosResponse<DatasetJudgementResponse>,
  Error,
  PatchJudgementArgs,
  unknown
> {
  const apiClientProvider = useApiClient();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ judgementId, dataPointTypeId, details }: PatchJudgementArgs) =>
      apiClientProvider.apiClients.datasetJudgementController.patchJudgementDetails(
        judgementId,
        dataPointTypeId,
        details
      ),

    onSuccess: async (_data, variables) => {
      await queryClient.invalidateQueries({
        queryKey: datasetJudgementKeys.detail(variables.judgementId),
      });
    },

    onError: (error) => {
      console.error('Error patching judgement details:', error);
    },
  });
}
