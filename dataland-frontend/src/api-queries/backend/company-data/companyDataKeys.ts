export const companyDataKeys = {
  all: ['companyData'] as const,
  info: (companyId: string | undefined) => [...companyDataKeys.all, 'info', companyId] as const,
};
