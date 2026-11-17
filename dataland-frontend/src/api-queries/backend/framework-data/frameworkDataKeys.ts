export const frameworkDataKeys = {
  all: ['frameworkData'] as const,
  byFrameworkAndId: (framework: string | undefined, dataId: string | undefined) =>
    [...frameworkDataKeys.all, framework, dataId] as const,
  allByFrameworkAndCompany: (framework: string | undefined, companyId: string | undefined) =>
    [...frameworkDataKeys.all, 'allByCompany', framework, companyId] as const,
  byDimensions: (framework: string | undefined, companyId: string | undefined, reportingPeriod: string | undefined) =>
    [...frameworkDataKeys.all, 'byDimensions', framework, companyId, reportingPeriod] as const,
};
