import { computed, toValue, type MaybeRefOrGetter } from 'vue';
import { useQuery, type UseQueryOptions, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { dataPointKeys } from '@/api-queries/backend/data-point/dataPointKeys';
import { type UploadedDataPoint } from '@clients/backend';

/**
 * Vue Query hook that fetches a data point for a given data point id.
 * The query is disabled while `dataPointId` is empty.
 *
 * @param params - Endpoint parameters.
 * @param params.dataPointId - Id of the data point to fetch.
 * @param options - Additional TanStack query options. A passed `enabled` replaces the default.
 * @returns Vue Query result containing the {@link UploadedDataPoint}.
 */
export function useGetDataPointByIdQuery(
  params: { dataPointId: MaybeRefOrGetter<string | undefined> },
  options: Omit<UseQueryOptions<UploadedDataPoint, Error>, 'queryKey' | 'queryFn'> = {}
): UseQueryReturnType<UploadedDataPoint, Error> {
  const apiClientProvider = useApiClient();

  return useQuery<UploadedDataPoint, Error>({
    queryKey: computed(() => dataPointKeys.byDataPointId(toValue(params.dataPointId))),
    queryFn: async () => {
      const dataPointId = toValue(params.dataPointId);
      if (!dataPointId) throw new Error('dataPointId is undefined');
      const response = await apiClientProvider.apiClients.dataPointController.getDataPoint(dataPointId);
      return response.data;
    },
    enabled: computed(() => !!toValue(params.dataPointId)),
    ...options,
  });
}
