/**
 * Module for getting stored companies by sending out an API-call and mapping the received stored companies to a
 * specific schema which is needed on the search page
 */

import { ApiClientProvider } from "@/services/ApiClients";
import { StoredCompany, CompanyInformation, DataMetaInformation, DataTypeEnum } from "@/../build/clients/backend";
import Keycloak from "keycloak-js";

export interface DataSearchStoredCompany {
  companyName: string;
  companyInformation: CompanyInformation;
  companyId: string;
  permId: string;
  dataRegisteredByDataland: Array<DataMetaInformation>;
}

/**
 * retrieve the value of the Perm Id of a company
 *
 * @param  {StoredCompany} storedCompany      is the company object for which the Perm Id should be retrieved
 */
function retrievePermIdFromStoredCompany(storedCompany: StoredCompany): string {
  const permIdIdentifier = storedCompany.companyInformation.identifiers.filter(
    (identifier) => identifier.identifierType === "PermId"
  );
  if (permIdIdentifier.length == 1) {
    return permIdIdentifier[0].identifierValue;
  } else if (permIdIdentifier.length == 0) {
    return "";
  } else {
    console.error("More than one PermId found for a specific company");
    return permIdIdentifier[0].identifierValue;
  }
}

/**
 * map the received stored companies of an API-call to the required scheme for the search page to display
 *
 * @param  {Array<StoredCompany>} responseData      the received data with the company objects
 */
function mapStoredCompanyToFrameworkDataSearchPage(responseData: Array<StoredCompany>): Array<object> {
  return responseData.map((company) => ({
    companyName: company.companyInformation.companyName,
    companyInformation: company.companyInformation,
    companyId: company.companyId,
    permId: retrievePermIdFromStoredCompany(company),
    dataRegisteredByDataland: company.dataRegisteredByDataland,
  }));
}

/**
 * send out an API-call to get stored companies and map the response to the required scheme for the search page
 *
 * @param  {string} searchString           the string that is used to search companies
 * @param  {'Cdax' | 'Dax' | 'GeneralStandard' | 'Gex' | 'Mdax' | 'PrimeStandard' | 'Sdax' | 'TecDax' | 'Hdax' | 'Dax50Esg'} stockIndex
 *                                         choose one to get companies in that index
 * @param  {boolean} onlyCompanyNames      boolean which decides if the searchString should only be used to query
 *                                         companies by name, or additionally by identifier values
 * @param {Array<string>} frameworksToFilter
 *                                         search for companies that hold at least one data set for at least one of
 *                                         the frameworks mentioned in frameworksToFilter and don't filter if
 *                                         frameworksToFilter is empty
 * @param {any} keycloakPromise            a promise to the Keycloak Object for the Frontend
 */
export async function getCompanyDataForFrameworkDataSearchPage(
  searchString: string,
  stockIndex:
    | "Cdax"
    | "Dax"
    | "GeneralStandard"
    | "Gex"
    | "Mdax"
    | "PrimeStandard"
    | "Sdax"
    | "TecDax"
    | "Hdax"
    | "Dax50Esg"
    | undefined,
  onlyCompanyNames: boolean,
  frameworksToFilter: Array<DataTypeEnum>,
  keycloakPromise: Promise<Keycloak>
): Promise<Array<object>> {
  let mappedResponse: object[] = [];

  const frameworkFilter = frameworksToFilter ? new Set(frameworksToFilter) : new Set(Object.values(DataTypeEnum));
  const stockIndexFilter = stockIndex ? new Set([stockIndex]) : undefined;
  const searchFilter = searchString ? searchString : "";

  try {
    const companyDataControllerApi = await new ApiClientProvider(keycloakPromise).getCompanyDataControllerApi();
    const response = await companyDataControllerApi.getCompanies(
      searchFilter,
      stockIndexFilter,
      frameworkFilter,
      onlyCompanyNames
    );
    const responseData: Array<StoredCompany> = response.data;
    mappedResponse = mapStoredCompanyToFrameworkDataSearchPage(responseData);
  } catch (error) {
    console.error(error);
  }
  return mappedResponse;
}

export function getRouterLinkTargetFramework(companyData: DataSearchStoredCompany): string {
  const dataRegisteredByDataland = companyData.dataRegisteredByDataland;
  const companyId = companyData.companyId;
  if (dataRegisteredByDataland.length === 0) return `/companies/${companyId}`;
  const targetData = dataRegisteredByDataland[0];
  return `/companies/${companyId}/frameworks/${targetData.dataType}`;
}
