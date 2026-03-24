import { useApiClient } from '@/utils/useApiClient.ts';
import { Ref } from 'vue';
import { DatasetJudgementResponse, JudgementDetailsPatch } from '@clients/qaservice';
import { useMutation, UseMutationReturnType, useQueryClient } from '@tanstack/vue-query';
import { datasetReviewKeys } from '@/api-queries/qa-service/dataset-judgement/datasetReviewKeys.ts';
import { AxiosResponse } from 'axios';

export function usePatchJudgmentDetailsForADatapointMutation(
  datasetJudgmentIdRef: Ref<string>,
  datapointIdRef: Ref<string>,
  judgementDetailsRef: Ref<JudgementDetailsPatch>
): UseMutationReturnType<AxiosResponse<DatasetJudgementResponse>, Error, void, unknown> {
  const apiClientProvider = useApiClient();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () =>
      apiClientProvider.apiClients.datasetJudgementController.patchJudgementDetails(
        datasetJudgmentIdRef.value,
        datapointIdRef.value,
        judgementDetailsRef.value
      ),

    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: datasetReviewKeys.detail(datasetJudgmentIdRef.value),
      });
    },

    onError: (error) => {
      console.error('Error patching judgement details:', error);
    },
  });
}
