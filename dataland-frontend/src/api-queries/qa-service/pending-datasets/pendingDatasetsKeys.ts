export const pendingDatasetsKeys = {
  all: ['pendingDatasets'] as const,
  byCompanyNameFilter: (companyNameFilter: string | undefined) =>
    ['pendingDatasets', 'byFilter', companyNameFilter] as const,
};
