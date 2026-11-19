import { computed, toValue, type MaybeRefOrGetter } from 'vue';
import { useQuery, type UseQueryOptions, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { getFrameworkDataApiForIdentifier } from '@/frameworks/FrameworkApiUtils';
import { frameworkDataKeys } from '@/api-queries/backend/framework-data/frameworkDataKeys';
import { type CompanyAssociatedData } from '@/api-models/CompanyAssociatedData';

/**
 * Vue Query hook that fetches the active dataset of a framework for a company and reporting period
 * (getCompanyAssociatedDataByDimensions). Disabled while any parameter is empty.
 *
 * @param params - Endpoint parameters.
 * @param params.framework - Framework identifier used to select the API.
 * @param params.companyId - Id of the company.
 * @param params.reportingPeriod - Reporting period of the dataset.
 * @param options - Additional TanStack query options. A passed `enabled` replaces the default.
 * @returns Vue Query result containing the company associated data.
 */
export function useGetCompanyAssociatedDataByDimensionsQuery(
  params: {
    framework: MaybeRefOrGetter<string | undefined>;
    companyId: MaybeRefOrGetter<string | undefined>;
    reportingPeriod: MaybeRefOrGetter<string | undefined>;
  },
  options: Omit<UseQueryOptions<CompanyAssociatedData<object>, Error>, 'queryKey' | 'queryFn'> = {}
): UseQueryReturnType<CompanyAssociatedData<object>, Error> {
  const apiClientProvider = useApiClient();

  return useQuery<CompanyAssociatedData<object>, Error>({
    queryKey: computed(() =>
      frameworkDataKeys.byDimensions(
        toValue(params.framework),
        toValue(params.companyId),
        toValue(params.reportingPeriod)
      )
    ),
    queryFn: async () => {
      const framework = toValue(params.framework);
      const companyId = toValue(params.companyId);
      const reportingPeriod = toValue(params.reportingPeriod);
      if (!framework || !companyId || !reportingPeriod) throw new Error('all parameters must be defined');
      const api = getFrameworkDataApiForIdentifier(framework, apiClientProvider);
      if (!api) throw new Error(`No data controller found for framework ${framework}`);
      const response = await api.getCompanyAssociatedDataByDimensions(reportingPeriod, companyId);
      return response.data;
    },
    enabled: computed(
      () => !!toValue(params.framework) && !!toValue(params.companyId) && !!toValue(params.reportingPeriod)
    ),
    ...options,
  });
}
