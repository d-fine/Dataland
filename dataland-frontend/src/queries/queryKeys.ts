export const queryKeys = {
  creditsBalance: (companyId: string) => ['creditsBalance', companyId] as const,
  companyInformation: (companyId: string) => ['companyInformation', { companyId }] as const,
  hasCompanyOwnership: (companyId: string) => ['hasCompanyOwnership', companyId] as const,
  userAdmin: () => ['userAdmin'] as const,
  userCompanyRoles: (companyId: string) => ['userCompanyRoles', companyId] as const,
  companyRights: (companyId: string) => ['companyRights', companyId] as const,
  isUserCompanyOwner: (companyId: string) => ['isUserCompanyOwner', companyId] as const,
};
