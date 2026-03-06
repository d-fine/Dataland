// src/api-queries/backend/meta-data/useDataMetaInfoQuery.ts
import { computed, type Ref } from 'vue';
import { useQuery } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { metaDataKeys } from '@/api-queries/backend/meta-data/metaDataKeys';

export function useDataMetaInfoQuery(dataId: Ref<string | undefined>) {
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
