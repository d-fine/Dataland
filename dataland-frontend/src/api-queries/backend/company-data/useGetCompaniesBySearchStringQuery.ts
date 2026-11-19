import { computed, toValue, type MaybeRefOrGetter } from 'vue';
import { useQuery, type UseQueryOptions, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { companyDataKeys } from '@/api-queries/backend/company-data/companyDataKeys';
import { type CompanyIdAndName } from '@clients/backend';

/**
 * Vue Query hook that searches companies by name or identifier and returns their ids and names
 * (GET /companies/names).
 * The query is disabled while `searchString` is empty.
 *
 * @param params - Endpoint parameters.
 * @param params.searchString - Text matched against company names and identifiers.
 * @param params.resultLimit - Maximum number of returned companies.
 * @param options - Additional TanStack query options. A passed `enabled` replaces the default.
 * @returns Vue Query result containing the matching {@link CompanyIdAndName}s.
 */
export function useGetCompaniesBySearchStringQuery(
  params: {
    searchString: MaybeRefOrGetter<string | undefined>;
    resultLimit?: MaybeRefOrGetter<number | undefined>;
  },
  options: Omit<UseQueryOptions<CompanyIdAndName[], Error>, 'queryKey' | 'queryFn'> = {}
): UseQueryReturnType<CompanyIdAndName[], Error> {
  const apiClientProvider = useApiClient();

  return useQuery<CompanyIdAndName[], Error>({
    queryKey: computed(() =>
      companyDataKeys.namesBySearchString({
        searchString: toValue(params.searchString),
        resultLimit: toValue(params.resultLimit),
      })
    ),
    queryFn: async () => {
      const searchString = toValue(params.searchString);
      if (!searchString) throw new Error('searchString is undefined');
      const response = await apiClientProvider.backendClients.companyDataController.getCompaniesBySearchString(
        searchString,
        toValue(params.resultLimit)
      );
      return response.data;
    },
    enabled: computed(() => !!toValue(params.searchString)),
    ...options,
  });
}
