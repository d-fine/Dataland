import { useQuery, type UseQueryOptions, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { companyDataKeys } from '@/api-queries/backend/company-data/companyDataKeys';
import { type CompanyAvailableDistinctValues } from '@clients/backend';

/**
 * Vue Query hook that fetches the country codes, sectors and reporting periods that can be used as company search
 * filters (GET /companies/meta-information).
 *
 * @param options - Additional TanStack query options.
 * @returns Vue Query result containing the {@link CompanyAvailableDistinctValues}.
 */
export function useGetAvailableCompanySearchFiltersQuery(
  options: Omit<UseQueryOptions<CompanyAvailableDistinctValues, Error>, 'queryKey' | 'queryFn'> = {}
): UseQueryReturnType<CompanyAvailableDistinctValues, Error> {
  const apiClientProvider = useApiClient();

  return useQuery<CompanyAvailableDistinctValues, Error>({
    queryKey: companyDataKeys.availableSearchFilters(),
    queryFn: async () => {
      const response = await apiClientProvider.backendClients.companyDataController.getAvailableCompanySearchFilters();
      return response.data;
    },
    ...options,
  });
}
