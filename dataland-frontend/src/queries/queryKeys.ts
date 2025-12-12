export const queryKeys = {
    creditsBalance: (companyId: string) => ['creditsBalance', companyId] as const,
    companyInformation: (companyId: string) => ['companyInformation', companyId] as const,
}; 