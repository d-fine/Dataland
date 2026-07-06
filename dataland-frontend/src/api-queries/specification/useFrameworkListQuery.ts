import { useQuery, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient.ts';
import { specificationKeys } from '@/api-queries/specification/specificationKeys.ts';
import type { SimpleFrameworkSpecification } from '@clients/specificationservice';

/**
 * Fetch the list of all frameworks known to the specification service.
 *
 * @returns {UseQueryReturnType<Array<SimpleFrameworkSpecification>, Error>} Vue Query result; `data` holds
 *   the list of simple framework specifications.
 */
export function useFrameworkListQuery(): UseQueryReturnType<Array<SimpleFrameworkSpecification>, Error> {
  const apiClientProvider = useApiClient();

  return useQuery<Array<SimpleFrameworkSpecification>, Error>({
    queryKey: specificationKeys.frameworkList(),
    queryFn: async () => {
      const { specificationController } = apiClientProvider.apiClients;
      const { data } = await specificationController.listFrameworkSpecifications();
      return data;
    },
  });
}
