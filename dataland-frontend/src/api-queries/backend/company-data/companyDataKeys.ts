import { type DataTypeEnum } from '@clients/backend';

export interface CompanySearchFilter {
  searchString?: string;
  dataTypes?: DataTypeEnum[];
  countryCodes?: string[];
  sectors?: string[];
  reportingPeriods?: string[];
}

export interface CompanySearchChunk {
  chunkSize?: number;
  chunkIndex?: number;
}

export interface CompaniesBySearchStringFilter {
  searchString?: string;
  resultLimit?: number;
}

/**
 * Sorts the set-valued filters, so that the same selection in a different order hits the same cache entry.
 * @param filter the company search filter
 * @returns the filter with sorted arrays
 */
function normalizeCompanySearchFilter(filter: CompanySearchFilter): CompanySearchFilter {
  return {
    searchString: filter.searchString,
    dataTypes: filter.dataTypes ? [...filter.dataTypes].sort() : undefined,
    countryCodes: filter.countryCodes ? [...filter.countryCodes].sort() : undefined,
    sectors: filter.sectors ? [...filter.sectors].sort() : undefined,
    reportingPeriods: filter.reportingPeriods ? [...filter.reportingPeriods].sort() : undefined,
  };
}

export const companyDataKeys = {
  all: ['companyData'] as const,
  info: (companyId: string | undefined) => [...companyDataKeys.all, 'info', companyId] as const,
  namesBySearchString: (filter: CompaniesBySearchStringFilter) =>
    [...companyDataKeys.all, 'namesBySearchString', filter] as const,
  aggregatedFrameworkDataSummary: (companyId: string | undefined) =>
    [...companyDataKeys.all, 'aggregatedFrameworkDataSummary', companyId] as const,
  companies: (filter: CompanySearchFilter, chunk: CompanySearchChunk) =>
    [...companyDataKeys.all, 'companies', normalizeCompanySearchFilter(filter), chunk] as const,
  numberOfCompanies: (filter: CompanySearchFilter) =>
    [...companyDataKeys.all, 'numberOfCompanies', normalizeCompanySearchFilter(filter)] as const,
  availableSearchFilters: () => [...companyDataKeys.all, 'availableSearchFilters'] as const,
};
