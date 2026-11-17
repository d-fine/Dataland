import { computed, toValue, type MaybeRefOrGetter } from 'vue';
import { useQuery, type UseQueryOptions, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { getFrameworkDataApiForIdentifier } from '@/frameworks/FrameworkApiUtils';
import { frameworkDataKeys } from '@/api-queries/backend/framework-data/frameworkDataKeys';
import { type DataAndMetaInformation } from '@/api-models/DataAndMetaInformation';

/**
 * Vue Query hook that fetches all datasets of a framework for a company (getAllCompanyData).
 * The query is disabled while `framework` or `companyId` is empty.
 *
 * @param params - Endpoint parameters.
 * @param params.framework - Framework identifier used to select the API.
 * @param params.companyId - Id of the company.
 * @param options - Additional TanStack query options. A passed `enabled` replaces the default.
 * @returns Vue Query result containing the datasets with their meta information.
 */
export function useGetAllCompanyFrameworkDataQuery(
  params: { framework: MaybeRefOrGetter<string | undefined>; companyId: MaybeRefOrGetter<string | undefined> },
  options: Omit<UseQueryOptions<DataAndMetaInformation<object>[], Error>, 'queryKey' | 'queryFn'> = {}
): UseQueryReturnType<DataAndMetaInformation<object>[], Error> {
  const apiClientProvider = useApiClient();

  return useQuery<DataAndMetaInformation<object>[], Error>({
    queryKey: computed(() =>
      frameworkDataKeys.allByFrameworkAndCompany(toValue(params.framework), toValue(params.companyId))
    ),
    queryFn: async () => {
      const framework = toValue(params.framework);
      const companyId = toValue(params.companyId);
      if (!framework || !companyId) throw new Error('framework and companyId must be defined');
      const api = getFrameworkDataApiForIdentifier(framework, apiClientProvider);
      if (!api) throw new Error(`No data controller found for framework ${framework}`);
      const response = await api.getAllCompanyData(companyId);
      return response.data;
    },
    enabled: computed(() => !!toValue(params.framework) && !!toValue(params.companyId)),
    ...options,
  });
}
