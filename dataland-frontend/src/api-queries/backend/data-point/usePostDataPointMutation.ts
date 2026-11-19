import { useMutation, useQueryClient, type UseMutationReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient.ts';
import { dataPointKeys } from '@/api-queries/backend/data-point/dataPointKeys.ts';
import { type DataPointMetaInformation, type UploadedDataPoint } from '@clients/backend';

export interface PostDataPointVariables {
  uploadedDataPoint: UploadedDataPoint;
  bypassQa?: boolean;
}

/**
 * Vue Query mutation hook that uploads a data point (POST /data-points).
 * On success, all cached data point queries are invalidated.
 *
 * @returns Mutation result resolving to the {@link DataPointMetaInformation} of the stored data point.
 */
export function usePostDataPointMutation(): UseMutationReturnType<
  DataPointMetaInformation,
  Error,
  PostDataPointVariables,
  unknown
> {
  const queryClient = useQueryClient();
  const apiClientProvider = useApiClient();

  return useMutation({
    mutationFn: async ({ uploadedDataPoint, bypassQa }: PostDataPointVariables) => {
      const response = await apiClientProvider.apiClients.dataPointController.postDataPoint(
        uploadedDataPoint,
        bypassQa
      );
      return response.data;
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: dataPointKeys.all });
    },
  });
}
