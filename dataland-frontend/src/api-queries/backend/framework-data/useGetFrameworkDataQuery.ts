import type { Ref } from 'vue';
import { computed } from 'vue';
import { useQuery } from '@tanstack/vue-query';
import type { DataTypeEnum } from '@clients/backend';
import { useApiClient } from '@/utils/useApiClient';
import { getFrameworkDataApiForIdentifier } from '@/frameworks/FrameworkApiUtils';
import { frameworkDataKeys } from './frameworkDataKeys';

export function useGetFrameworkDataQuery(options: { framework: Ref<DataTypeEnum>; dataId: Ref<string> }) {
  const apiClientProvider = useApiClient();

  return useQuery({
    queryKey: frameworkDataKeys.byFrameworkAndId(options.framework.value, options.dataId.value),

    queryFn: async () => {
      const framework = options.framework.value;
      const dataId = options.dataId.value;

      const api = getFrameworkDataApiForIdentifier(framework, apiClientProvider);
      if (!api) {
        throw new Error(`No data API for framework: ${framework}`);
      }

      const response = await api.getFrameworkData(dataId);
      return response.data;
    },

    enabled: computed(() => !!options.framework.value && !!options.dataId.value),
  });
}
