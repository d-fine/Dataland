export interface CompaniesBySearchStringFilter {
  searchString?: string;
  resultLimit?: number;
}

export const companyDataKeys = {
  all: ['companyData'] as const,
  info: (companyId: string | undefined) => [...companyDataKeys.all, 'info', companyId] as const,
  namesBySearchString: (filter: CompaniesBySearchStringFilter) =>
    [...companyDataKeys.all, 'namesBySearchString', filter] as const,
  aggregatedFrameworkDataSummary: (companyId: string | undefined) =>
    [...companyDataKeys.all, 'aggregatedFrameworkDataSummary', companyId] as const,
};
