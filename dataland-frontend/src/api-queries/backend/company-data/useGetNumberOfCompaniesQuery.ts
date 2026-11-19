import { computed, toValue, type MaybeRefOrGetter } from 'vue';
import { useQuery, type UseQueryOptions, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { companyDataKeys } from '@/api-queries/backend/company-data/companyDataKeys';
import { type DataTypeEnum } from '@clients/backend';

/**
 * Vue Query hook that counts the companies matching a search string and the given filters
 * (GET /companies/numberOfCompanies).
 *
 * @param params - Endpoint parameters. Undefined or empty filters are not applied.
 * @param params.searchString - Text matched against company names and identifiers.
 * @param params.dataTypes - Only count companies with data for one of these frameworks.
 * @param params.countryCodes - Only count companies from one of these countries.
 * @param params.sectors - Only count companies of one of these sectors.
 * @param params.reportingPeriods - Only count companies with data for one of these reporting periods.
 * @param options - Additional TanStack query options.
 * @returns Vue Query result containing the number of matching companies.
 */
export function useGetNumberOfCompaniesQuery(
  params: {
    searchString?: MaybeRefOrGetter<string | undefined>;
    dataTypes?: MaybeRefOrGetter<DataTypeEnum[] | undefined>;
    countryCodes?: MaybeRefOrGetter<string[] | undefined>;
    sectors?: MaybeRefOrGetter<string[] | undefined>;
    reportingPeriods?: MaybeRefOrGetter<string[] | undefined>;
  },
  options: Omit<UseQueryOptions<number, Error>, 'queryKey' | 'queryFn'> = {}
): UseQueryReturnType<number, Error> {
  const apiClientProvider = useApiClient();

  return useQuery<number, Error>({
    queryKey: computed(() =>
      companyDataKeys.numberOfCompanies({
        searchString: toValue(params.searchString),
        dataTypes: toValue(params.dataTypes),
        countryCodes: toValue(params.countryCodes),
        sectors: toValue(params.sectors),
        reportingPeriods: toValue(params.reportingPeriods),
      })
    ),
    queryFn: async () => {
      const response = await apiClientProvider.backendClients.companyDataController.getNumberOfCompanies(
        toValue(params.searchString),
        toOptionalSet(toValue(params.dataTypes)),
        toOptionalSet(toValue(params.countryCodes)),
        toOptionalSet(toValue(params.sectors)),
        toOptionalSet(toValue(params.reportingPeriods))
      );
      return response.data;
    },
    ...options,
  });
}

/**
 * Converts an optional array parameter into the Set expected by the generated client.
 * @param values the parameter values
 * @returns a Set of the values, or undefined if no values are given
 */
function toOptionalSet<T>(values: T[] | undefined): Set<T> | undefined {
  return values ? new Set(values) : undefined;
}
