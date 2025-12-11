import { type ApiClientProvider } from '@/services/ApiClients';
import { type CompanyInformation, type CompanyIdAndName, IdentifierType } from '@clients/backend';
import { getCompanyDataForFrameworkDataSearchPageWithoutFilters } from '@/utils/SearchCompaniesForFrameworkDataPageDataRequester';
import type Keycloak from 'keycloak-js';

export interface CompanyInformationResult {
  companyInformation: CompanyInformation | null;
  parentCompany: CompanyIdAndName | null;
  hasParentCompany: boolean;
}

/**
 * Gets company information and parent company information.
 * @param companyId ID of Company
 * @param apiClientProvider API Client Provider
 * @param getKeycloakPromise Function for Keycloak Promise
 */
export async function getCompanyInformation(
  companyId: string,
  apiClientProvider: ApiClientProvider,
  getKeycloakPromise: () => Promise<Keycloak>
): Promise<CompanyInformationResult> {
  let companyInformation: CompanyInformation | null = null;
  let parentCompany: CompanyIdAndName | null = null;
  let hasParentCompany = false;

  try {
    const companyDataControllerApi = apiClientProvider.backendClients.companyDataController;
    companyInformation = (await companyDataControllerApi.getCompanyInfo(companyId)).data;

    if (companyInformation.parentCompanyLei) {
      const companyIdAndNames = await getCompanyDataForFrameworkDataSearchPageWithoutFilters(
        companyInformation.parentCompanyLei,
        getKeycloakPromise(),
        1
      );

      if (companyIdAndNames.length > 0) {
        parentCompany = companyIdAndNames[0]!;
        hasParentCompany = true;
      }
    }
  } catch (error) {
    console.error(`Failed to fetch company information for ${companyId}`, error);
  }

  return { companyInformation, parentCompany, hasParentCompany };
}

/**
 * Gets LEI to be displayed.
 */
export function getDisplayLei(companyInformation: CompanyInformation | null): string {
  return companyInformation?.identifiers?.[IdentifierType.Lei]?.[0] ?? '—';
}
