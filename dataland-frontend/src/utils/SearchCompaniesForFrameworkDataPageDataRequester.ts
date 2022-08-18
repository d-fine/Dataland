/**
 * Module for getting stored companies by sending out an API-call and mapping the received stored companies to a
 * specific schema which is needed on the taxonomy search page
 */

import { ApiClientProvider } from "@/services/ApiClients";
import { StoredCompany, CompanyInformation, DataMetaInformation } from "@/../build/clients/backend/api";
import Keycloak from "keycloak-js";
import { frameworkUrlMapping } from "@/utils/FrameworkUrlMapping";

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
 * map the received stored companies of an API-call to the required scheme for the Taxonomy Page to display
 *
 * @param  {Array<StoredCompany>} responseData      the received data with the companiy objects
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
 * send out an API-call to get stored companies and map the response to the required scheme for the Taxonomy Page
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
    | "Dax50Esg",
  onlyCompanyNames: boolean,
  frameworksToFilter: Array<string>,
  keycloakPromise: Promise<Keycloak>
): Promise<Array<object>> {
  let mappedResponse: object[] = [];
  try {
    const companyDataControllerApi = await new ApiClientProvider(keycloakPromise).getCompanyDataControllerApi();
    const response = await companyDataControllerApi.getCompanies(
      searchString,
      new Set([stockIndex]),
      new Set(frameworksToFilter),
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
  return `/companies/${companyId}/frameworks/${frameworkUrlMapping.get(targetData.dataType)}`;
}
