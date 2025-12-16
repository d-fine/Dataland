export const queryKeys = {
  creditsBalance: (companyId: string) => ['creditsBalance', companyId] as const,
  companyInformation: (companyId: string) => ['companyInformation', { companyId }] as const,
  hasCompanyOwnership: (companyId: string) => ['hasCompanyOwnership', companyId] as const,
  userAdmin: () => ['userAdmin'] as const,
};
