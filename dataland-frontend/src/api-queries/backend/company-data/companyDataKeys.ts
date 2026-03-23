export const companyDataKeys = {
  all: ['companyData'] as const,
  byCompanyId: (companyId: string | undefined) => ['companyData', companyId] as const,
};
