import type { Ref } from 'vue';
import { computed } from 'vue';
import { useQuery, type UseQueryReturnType } from '@tanstack/vue-query';
import type { DataTypeEnum } from '@clients/backend';
import { useApiClient } from '@/utils/useApiClient';
import { getFrameworkDataApiForIdentifier } from '@/frameworks/FrameworkApiUtils';
import { frameworkDataKeys } from './frameworkDataKeys';

/**
 * Hook to fetch framework-specific data by framework type and data id.
 *
 * @param {{ framework: Ref<DataTypeEnum>; dataId: Ref<string> }} options - Options for the query.
 * @param {Ref<DataTypeEnum>} options.framework - Reactive reference containing the framework/data type
 *   (used to resolve the correct backend API).
 * @param {Ref<string>} options.dataId - Reactive reference containing the identifier of the data to fetch.
 * @returns {UseQueryReturnType<unknown, Error>} - Query result containing the fetched data or error.
 * @throws {Error} If no API implementation exists for the provided framework value.
 */
export function useGetFrameworkDataQuery(options: {
  framework: Ref<DataTypeEnum>;
  dataId: Ref<string>;
}): UseQueryReturnType<unknown, Error> {
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
