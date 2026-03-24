import { useApiClient } from '@/utils/useApiClient.ts';
import { Ref } from 'vue';
import { DatasetJudgementResponse, JudgementDetailsPatch } from '@clients/qaservice';
import { useMutation, UseMutationReturnType, useQueryClient } from '@tanstack/vue-query';
import { datasetReviewKeys } from '@/api-queries/qa-service/dataset-judgement/datasetReviewKeys.ts';
import { AxiosResponse } from 'axios';

export interface PatchJudgementArgs {
  judgmentId: string;
  datapointId: string;
  details: JudgementDetailsPatch;
}

export function usePatchJudgmentDetailsForADatapointMutation() {
  const apiClientProvider = useApiClient();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ judgmentId, datapointId, details }: PatchJudgementArgs) =>
      apiClientProvider.apiClients.datasetJudgementController.patchJudgementDetails(judgmentId, datapointId, details),

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
