// src/api-queries/backend/meta-data/useDataMetaInfoQuery.ts
import { computed, type Ref } from 'vue';
import { useQuery, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { metaDataKeys } from '@/api-queries/backend/meta-data/metaDataKeys';
import { DataMetaInformation } from '@clients/backend';

/**
 * Hook to fetch metadata information for a given data identifier.
 * @param {Ref<string | undefined>} dataId - Reactive reference containing the id of the
 *   data whose metadata should be fetched. If `dataId.value` is false or not defined the query is
 *   disabled and no request will be made.
 * @returns {UseQueryReturnType<DataMetaInformation, Error>} A Vue Query result object. The
 *   `data` property (when available) contains the backend response's `data`.
 */
export function useDataMetaInfoQuery(dataId: Ref<string | undefined>): UseQueryReturnType<DataMetaInformation, Error> {
  const apiClientProvider = useApiClient();

  return useQuery({
    queryKey: computed(() => metaDataKeys.listByDataId(dataId.value)),
    queryFn: async () => {
      const id = dataId.value!;
      const apiController = apiClientProvider.backendClients.metaDataController;
      const response = await apiController.getDataMetaInfo(id);
      return response.data;
    },
    enabled: computed(() => !!dataId.value),
  });
}
