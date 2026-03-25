import { computed, type Ref } from 'vue';
import { useQuery, UseQueryOptions, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { dataPointKeys } from '@/api-queries/backend/data-point/dataPointKeys';
import { type UploadedDataPoint } from '@clients/backend';
/**
 * Vue Query hook that fetches a data point for a given data point id.
 *
 * @param {Ref<string>} dataPointId - Reactive reference containing the data point id.
 * @returns {UseQueryReturnType<UploadedDataPoint, Error>} Query result containing the uploaded data point.
 */
export function useGetDataPointByIdQuery(
  dataPointId: Ref<string>,
  options: Omit<UseQueryOptions<UploadedDataPoint, Error>, 'queryKey' | 'queryFn'> = {}
): UseQueryReturnType<UploadedDataPoint, Error> {
  const apiClientProvider = useApiClient();

  const queryKey = computed(() => dataPointKeys.byDataPointId(dataPointId.value));

  return useQuery<UploadedDataPoint, Error>({
    queryKey,
    queryFn: async () => {
      const apiController = apiClientProvider.apiClients.dataPointController;
      const response = await apiController.getDataPoint(dataPointId.value);
      return response.data;
    },
    ...options,
  });
}
