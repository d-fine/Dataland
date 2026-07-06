import { computed, type Ref } from 'vue';
import { useQuery, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient.ts';
import { specificationKeys } from '@/api-queries/specification/specificationKeys.ts';
import type { DataPointBaseTypeSpecification } from '@clients/specificationservice';

/**
 * Fetch the specification for a single data point base type by id.
 *
 * @param {{ dataPointBaseTypeId: Ref<string | undefined> }} options - Reactive ref with the base type id.
 * @returns {UseQueryReturnType<DataPointBaseTypeSpecification, Error>} Vue Query result; `data` holds the
 *   data point base type specification. The query is disabled while `dataPointBaseTypeId.value` is falsy.
 */
export function useDataPointBaseTypeSpecificationQuery(options: {
  dataPointBaseTypeId: Ref<string | undefined>;
}): UseQueryReturnType<DataPointBaseTypeSpecification, Error> {
  const apiClientProvider = useApiClient();

  return useQuery<DataPointBaseTypeSpecification, Error>({
    queryKey: computed(() => specificationKeys.dataPointBaseType(options.dataPointBaseTypeId.value)),

    queryFn: async () => {
      const { specificationController } = apiClientProvider.apiClients;
      const dataPointBaseTypeId = options.dataPointBaseTypeId.value;
      if (!dataPointBaseTypeId) {
        throw new Error('dataPointBaseTypeId is undefined');
      }
      const { data } = await specificationController.getDataPointBaseType(dataPointBaseTypeId);
      return data;
    },
    enabled: computed(() => !!options.dataPointBaseTypeId.value),
  });
}
