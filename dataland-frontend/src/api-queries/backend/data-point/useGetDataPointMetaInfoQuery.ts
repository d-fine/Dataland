import { computed, toValue, type MaybeRefOrGetter } from 'vue';
import { useQuery, type UseQueryOptions, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { dataPointKeys } from '@/api-queries/backend/data-point/dataPointKeys';
import { type DataPointMetaInformation } from '@clients/backend';

/**
 * Vue Query hook that fetches the meta information of a data point (GET /data-points/{id}/metadata).
 * The query is disabled while `dataPointId` is empty.
 *
 * @param params - Endpoint parameters.
 * @param params.dataPointId - Id of the data point.
 * @param options - Additional TanStack query options. A passed `enabled` replaces the default.
 * @returns Vue Query result containing the {@link DataPointMetaInformation}.
 */
export function useGetDataPointMetaInfoQuery(
  params: { dataPointId: MaybeRefOrGetter<string | undefined> },
  options: Omit<UseQueryOptions<DataPointMetaInformation, Error>, 'queryKey' | 'queryFn'> = {}
): UseQueryReturnType<DataPointMetaInformation, Error> {
  const apiClientProvider = useApiClient();

  return useQuery<DataPointMetaInformation, Error>({
    queryKey: computed(() => dataPointKeys.metaInfoByDataPointId(toValue(params.dataPointId))),
    queryFn: async () => {
      const dataPointId = toValue(params.dataPointId);
      if (!dataPointId) throw new Error('dataPointId is undefined');
      const response = await apiClientProvider.apiClients.dataPointController.getDataPointMetaInfo(dataPointId);
      return response.data;
    },
    enabled: computed(() => !!toValue(params.dataPointId)),
    ...options,
  });
}
