import { computed, type Ref } from 'vue';
import { useQuery, type UseQueryReturnType } from '@tanstack/vue-query';
import { isAxiosError } from 'axios';
import { useApiClient } from '@/utils/useApiClient.ts';
import { specificationKeys } from '@/api-queries/specification/specificationKeys.ts';

/**
 * Check whether the specification service knows about a given framework id. Used to conditionally
 * show links to the Specification Explorer for frameworks that do not (yet) have a specification.
 *
 * @param {{ frameworkId: Ref<string | undefined> }} options - Reactive ref with the framework id.
 * @returns {UseQueryReturnType<boolean, Error>} Vue Query result; `data` is true if a specification
 *   exists for the given framework id. The query is disabled while `frameworkId.value` is falsy.
 */
export function useFrameworkSpecificationExistsQuery(options: {
  frameworkId: Ref<string | undefined>;
}): UseQueryReturnType<boolean, Error> {
  const apiClientProvider = useApiClient();

  return useQuery<boolean, Error>({
    queryKey: computed(() => specificationKeys.frameworkExists(options.frameworkId.value)),

    queryFn: async () => {
      const { specificationController } = apiClientProvider.apiClients;
      const frameworkId = options.frameworkId.value;
      if (!frameworkId) {
        throw new Error('frameworkId is undefined');
      }
      try {
        await specificationController.doesFrameworkSpecificationExist(frameworkId);
        return true;
      } catch (error) {
        if (isAxiosError(error) && error.response?.status === 404) {
          return false;
        }
        throw error;
      }
    },
    enabled: computed(() => !!options.frameworkId.value),
  });
}
