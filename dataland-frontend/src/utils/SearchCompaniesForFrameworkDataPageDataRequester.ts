/**
 * Module for getting stored companies by sending out an API-call and mapping the received stored companies to a
 * specific schema which is needed on the search page
 */

import { ApiClientProvider } from "@/services/ApiClients";
import {
  type StoredCompany,
  type DataTypeEnum,
  IdentifierType,
  ReducedCompany,
} from "@clients/backend";
import type Keycloak from "keycloak-js";
import { ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE } from "@/utils/Constants";

export interface DataSearchStoredCompany {
  companyName: string;
  companyId: string;
  permId: string;
}

export interface FrameworkDataSearchFilterInterface {
  companyNameFilter: string;
  frameworkFilter: Array<DataTypeEnum>;
  countryCodeFilter: Array<string>;
  sectorFilter: Array<string>;
}

/**
 * Retrieve the value of the Perm Id of a company. Throws an exception if no perm id is found
 * @param  {StoredCompany} storedCompany      is the company object for which the Perm Id should be retrieved
 * @returns the perm id retrieved from the company object. Empty string if no perm id is known.
 */
function retrievePermIdFromStoredCompany(storedCompany: StoredCompany): string {
  const permIdIdentifier = storedCompany.companyInformation.identifiers[IdentifierType.PermId];
  if (permIdIdentifier.length == 1) {
    return permIdIdentifier[0];
  } else if (permIdIdentifier.length == 0) {
    return "";
  } else {
    console.error("More than one PermId found for a specific company");
    return permIdIdentifier[0];
  }
}

/**
 * send out an API-call to get stored companies and map the response to the required scheme for the search page
 * @param  {string} searchString           the string that is used to search companies
 *                                         companies by name, or additionally by identifier values
 * @param {Array<string>} frameworkFilter
 *                                         search for companies that hold at least one data set for at least one of
 *                                         the frameworks mentioned in frameworksToFilter and don't filter if
 *                                         frameworksToFilter is empty
 * @param countryCodeFilter                If not empty only companies whose headquarter is in one of the
 *                                         countries specified by the country codes are returned
 * @param sectorFilter                     If not empty only companies whose sector is in the set is returned
 * @param {any} keycloakPromise            a promise to the Keycloak Object for the Frontend
 * @returns the search result companies
 */
export async function getCompanyDataForFrameworkDataSearchPage(
  searchString: string,
  frameworkFilter: Set<DataTypeEnum>,
  countryCodeFilter: Set<string>,
  sectorFilter: Set<string>,
  keycloakPromise: Promise<Keycloak>,
): Promise<Array<ReducedCompany>> {
  try {
    const companyDataControllerApi = new ApiClientProvider(keycloakPromise).backendClients.companyDataController;
    if (frameworkFilter.size === 0) {
      frameworkFilter = new Set(ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE);
    }

    return (await companyDataControllerApi.getCompanies2(
      searchString,
      frameworkFilter,
      countryCodeFilter,
      sectorFilter,
    )).data;
  } catch (error) {
    console.error(error);
    return [];
  }
}
