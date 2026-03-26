import { useApiClient } from '@/utils/useApiClient.ts';
import { type DatasetJudgementResponse, type JudgementDetailsPatch } from '@clients/qaservice';
import { useMutation, type UseMutationReturnType, useQueryClient } from '@tanstack/vue-query';
import { datasetReviewKeys } from '@/api-queries/qa-service/dataset-judgement/datasetReviewKeys.ts';
import { type AxiosResponse } from 'axios';

export interface PatchJudgementArgs {
  judgmentId: string;
  dataPointTypeId: string;
  details: JudgementDetailsPatch;
}

/**
 * Vue Query mutation hook to patch judgement details for a specific data point type within a dataset judgement.
 */
export function usePatchJudgmentDetailsForADatapointMutation(): UseMutationReturnType<
  AxiosResponse<DatasetJudgementResponse>,
  Error,
  PatchJudgementArgs,
  unknown
> {
  const apiClientProvider = useApiClient();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ judgmentId, dataPointTypeId, details }: PatchJudgementArgs) =>
      apiClientProvider.apiClients.datasetJudgementController.patchJudgementDetails(
        judgmentId,
        dataPointTypeId,
        details
      ),

    onSuccess: async (_data, variables) => {
      await queryClient.invalidateQueries({
        queryKey: datasetReviewKeys.detail(variables.judgmentId),
      });
    },

    onError: (error) => {
      console.error('Error patching judgement details:', error);
    },
  });
}
