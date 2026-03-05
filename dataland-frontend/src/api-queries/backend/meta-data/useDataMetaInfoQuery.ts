// src/api-queries/backend/meta-data/useDataMetaInfoQuery.ts
import { computed, type Ref } from 'vue';
import { useQuery } from '@tanstack/vue-query';
import type { DataMetaInformation } from '@clients/backend';
import { useApiClient } from '@/utils/useApiClient';
import { metaDataKeys } from '@/api-queries/backend/meta-data/metaDataKeys';

export function useDataMetaInfoQuery(dataId: Ref<string>) {
  const apiClientProvider = useApiClient();

  return useQuery<DataMetaInformation>({
    queryKey: computed(() => metaDataKeys.listByDataId(dataId.value)),
    queryFn: async () => {
      const id = dataId.value;
      const apiController = apiClientProvider.backendClients.metaDataController;
      const response = await apiController.getDataMetaInfo(id);
      return response.data;
    },
    enabled: computed(() => !!dataId.value),
  });
}
