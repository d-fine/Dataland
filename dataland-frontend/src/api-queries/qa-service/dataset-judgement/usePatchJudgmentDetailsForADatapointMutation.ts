import { useApiClient } from '@/utils/useApiClient.ts';
import { JudgementDetailsPatch } from '@clients/qaservice';
import { useMutation, useQueryClient } from '@tanstack/vue-query';
import { datasetReviewKeys } from '@/api-queries/qa-service/dataset-judgement/datasetReviewKeys.ts';

export interface PatchJudgementArgs {
  judgmentId: string;
  dataPointTypeId: string;
  details: JudgementDetailsPatch;
}

export function usePatchJudgmentDetailsForADatapointMutation() {
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
