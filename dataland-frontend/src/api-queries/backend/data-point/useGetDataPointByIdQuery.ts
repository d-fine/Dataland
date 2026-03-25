import { computed, type Ref } from 'vue';
import { useQuery, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { dataPointKeys } from '@/api-queries/backend/data-point/dataPointKeys';
import { type UploadedDataPoint } from '@clients/backend';

/**
 * Vue Query hook that fetches a data point for a given data point id.
 *
 * @param {Ref<string|undefined>} dataPointId - Reactive reference containing the data point id; query runs when truthy.
 * @returns {UseQueryReturnType<UploadedDataPoint, Error>} Query result containing the uploaded data point.
 */
export function useGetDataPointByIdQuery(
  dataPointId: Ref<string | undefined>
): UseQueryReturnType<UploadedDataPoint, Error> {
  const apiClientProvider = useApiClient();

  return useQuery({
    queryKey: computed(() => dataPointKeys.ByDataPointId(dataPointId.value)),
    queryFn: async () => {
      const id = dataPointId.value!;
      const apiController = apiClientProvider.apiClients.dataPointController;
      const response = await apiController.getDataPoint(id);
      return response.data;
    },
    enabled: computed(() => !!dataPointId.value),
  });
}
