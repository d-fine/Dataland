import { computed, toValue, type MaybeRefOrGetter } from 'vue';
import { useQuery, type UseQueryOptions, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { companyDataKeys } from '@/api-queries/backend/company-data/companyDataKeys';
import { type BasicCompanyInformation, type DataTypeEnum } from '@clients/backend';

/**
 * Vue Query hook that searches companies by name or identifier and by the given filters, one chunk at a time
 * (GET /companies). Without any filter, all companies are listed.
 *
 * @param params - Endpoint parameters. Undefined or empty filters are not applied.
 * @param params.searchString - Text matched against company names and identifiers.
 * @param params.dataTypes - Only return companies with data for one of these frameworks.
 * @param params.countryCodes - Only return companies from one of these countries.
 * @param params.sectors - Only return companies of one of these sectors.
 * @param params.reportingPeriods - Only return companies with data for one of these reporting periods.
 * @param params.chunkSize - Maximum number of returned companies.
 * @param params.chunkIndex - Index of the returned chunk.
 * @param options - Additional TanStack query options.
 * @returns Vue Query result containing the matching {@link BasicCompanyInformation}s.
 */
export function useGetCompaniesQuery(
  params: {
    searchString?: MaybeRefOrGetter<string | undefined>;
    dataTypes?: MaybeRefOrGetter<DataTypeEnum[] | undefined>;
    countryCodes?: MaybeRefOrGetter<string[] | undefined>;
    sectors?: MaybeRefOrGetter<string[] | undefined>;
    reportingPeriods?: MaybeRefOrGetter<string[] | undefined>;
    chunkSize?: MaybeRefOrGetter<number | undefined>;
    chunkIndex?: MaybeRefOrGetter<number | undefined>;
  },
  options: Omit<UseQueryOptions<BasicCompanyInformation[], Error>, 'queryKey' | 'queryFn'> = {}
): UseQueryReturnType<BasicCompanyInformation[], Error> {
  const apiClientProvider = useApiClient();

  return useQuery<BasicCompanyInformation[], Error>({
    queryKey: computed(() =>
      companyDataKeys.companies(
        {
          searchString: toValue(params.searchString),
          dataTypes: toValue(params.dataTypes),
          countryCodes: toValue(params.countryCodes),
          sectors: toValue(params.sectors),
          reportingPeriods: toValue(params.reportingPeriods),
        },
        { chunkSize: toValue(params.chunkSize), chunkIndex: toValue(params.chunkIndex) }
      )
    ),
    queryFn: async () => {
      const response = await apiClientProvider.backendClients.companyDataController.getCompanies(
        toValue(params.searchString),
        toOptionalSet(toValue(params.dataTypes)),
        toOptionalSet(toValue(params.countryCodes)),
        toOptionalSet(toValue(params.sectors)),
        toOptionalSet(toValue(params.reportingPeriods)),
        toValue(params.chunkSize),
        toValue(params.chunkIndex)
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
