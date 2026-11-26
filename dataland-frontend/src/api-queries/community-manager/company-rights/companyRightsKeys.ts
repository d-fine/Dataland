export const companyRightsKeys = {
  all: ['companyRights'] as const,
  byCompanyId: (companyId: string | undefined) => [...companyRightsKeys.all, 'byCompanyId', companyId] as const,
};
