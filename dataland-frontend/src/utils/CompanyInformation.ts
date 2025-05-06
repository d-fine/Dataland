import { type ApiClientProvider } from '@/services/ApiClients.ts';
import { type CompanyInformation, IdentifierType } from '@clients/backend';

/**
 * Method to get the company information from the backend
 * @param companyId companyId
 * @param apiClientProvider the ApiClientProvider to use for the connection
 */
export async function getCompanyInformation(
  companyId: string,
  apiClientProvider: ApiClientProvider
): Promise<CompanyInformation> {
  const companyDataController = apiClientProvider.backendClients.companyDataController;
  return (await companyDataController.getCompanyInfo(companyId)).data;
}

/**
 * Method to get the company name from the backend
 * @param companyId companyId
 */
export async function getCompanyName(companyId: string, apiClientProvider: ApiClientProvider): Promise<string> {
  const companyInformation = await getCompanyInformation(companyId, apiClientProvider);
  return companyInformation.companyName;
}

/**
 * Get the id of the parent company. This function may throw an exception.
 * @param companyId the company whose parent shall be found
 * @param apiClientProvider the ApiClientProvider to use for the connection
 */
export async function getParentCompanyId(
  companyId: string,
  apiClientProvider: ApiClientProvider
): Promise<string | undefined> {
  const companyInformation = await getCompanyInformation(companyId, apiClientProvider);
  if (!companyInformation?.parentCompanyLei) return undefined;

  return (
    await apiClientProvider.backendClients.companyDataController.getCompanyIdByIdentifier(
      IdentifierType.Lei,
      companyInformation.parentCompanyLei
    )
  ).data.companyId;
}
