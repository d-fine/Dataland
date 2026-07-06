import { computed, type Ref } from 'vue';
import { useQuery, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient.ts';
import { specificationKeys } from '@/api-queries/specification/specificationKeys.ts';
import type { FrameworkSpecification } from '@clients/specificationservice';

/**
 * Fetch the full specification for a single framework by id.
 *
 * @param {{ frameworkId: Ref<string | undefined> }} options - Reactive ref with the framework id.
 * @returns {UseQueryReturnType<FrameworkSpecification, Error>} Vue Query result; `data` holds the
 *   framework specification. The query is disabled while `frameworkId.value` is falsy.
 */
export function useFrameworkSpecificationQuery(options: {
  frameworkId: Ref<string | undefined>;
}): UseQueryReturnType<FrameworkSpecification, Error> {
  const apiClientProvider = useApiClient();

  return useQuery<FrameworkSpecification, Error>({
    queryKey: computed(() => specificationKeys.framework(options.frameworkId.value)),

    queryFn: async () => {
      const { specificationController } = apiClientProvider.apiClients;
      const frameworkId = options.frameworkId.value;
      if (!frameworkId) {
        throw new Error('frameworkId is undefined');
      }
      const { data } = await specificationController.getFrameworkSpecification(frameworkId);
      return data;
    },
    enabled: computed(() => !!options.frameworkId.value),
  });
}
