import { computed, type Ref } from 'vue';
import { useQuery, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient.ts';
import { specificationKeys } from '@/api-queries/specification/specificationKeys.ts';
import type { DataPointTypeSpecification } from '@clients/specificationservice';

/**
 * Fetch the specification for a single data point type by id.
 *
 * @param {{ dataPointTypeId: Ref<string | undefined> }} options - Reactive ref with the data point type id.
 * @returns {UseQueryReturnType<DataPointTypeSpecification, Error>} Vue Query result; `data` holds the
 *   data point type specification. The query is disabled while `dataPointTypeId.value` is falsy.
 */
export function useDataPointTypeSpecificationQuery(options: {
  dataPointTypeId: Ref<string | undefined>;
}): UseQueryReturnType<DataPointTypeSpecification, Error> {
  const apiClientProvider = useApiClient();

  return useQuery<DataPointTypeSpecification, Error>({
    queryKey: computed(() => specificationKeys.dataPointType(options.dataPointTypeId.value)),

    queryFn: async () => {
      const { specificationController } = apiClientProvider.apiClients;
      const dataPointTypeId = options.dataPointTypeId.value;
      if (!dataPointTypeId) {
        throw new Error('dataPointTypeId is undefined');
      }
      const { data } = await specificationController.getDataPointTypeSpecification(dataPointTypeId);
      return data;
    },
    enabled: computed(() => !!options.dataPointTypeId.value),
  });
}
