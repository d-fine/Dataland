/**
 * Module for getting stored companies by sending out an API-call and mapping the received stored companies to a
 * specific schema which is needed on the search page
 */

import { ApiClientProvider } from '@/services/ApiClients';
import { type BasicCompanyInformation, type CompanyIdAndName, DataTypeEnum } from '@clients/backend';
import type Keycloak from 'keycloak-js';

export interface FrameworkDataSearchFilterInterface {
  companyNameFilter: string;
  frameworkFilter: Array<DataTypeEnum>;
  countryCodeFilter: Array<string>;
  sectorFilter: Array<string>;
}

/**
 * send out an API-call to get stored companies and map the response to the required scheme for the search page
 * @param  {string} searchString           the string that is used to search companies
 *                                         by name, or additionally by identifier values
 * @param {Array<string>} frameworkFilter
 *                                         search for companies that hold at least one data set for at least one of
 *                                         the frameworks mentioned in frameworksToFilter and don't filter if
 *                                         frameworksToFilter is empty
 * @param countryCodeFilter                If not empty only companies whose headquarter is in one of the
 *                                         countries specified by the country codes are returned
 * @param sectorFilter                     If not empty only companies whose sector is in the set is returned
 * @param {any} keycloakPromise            a promise to the Keycloak Object for the Frontend
 * @param chunkSize                        size of requested chunk
 * @param chunkIndex                       index of requested chunk
 * @returns the search result companies
 */
export async function getCompanyDataForFrameworkDataSearchPage(
  searchString: string,
  frameworkFilter: Set<DataTypeEnum>,
  countryCodeFilter: Set<string>,
  sectorFilter: Set<string>,
  keycloakPromise: Promise<Keycloak>,
  chunkSize?: number,
  chunkIndex?: number
): Promise<Array<BasicCompanyInformation>> {
  try {
    const companyDataControllerApi = new ApiClientProvider(keycloakPromise).backendClients.companyDataController;
    return (
      await companyDataControllerApi.getCompanies(
        searchString,
        frameworkFilter,
        countryCodeFilter,
        sectorFilter,
        chunkSize,
        chunkIndex
      )
    ).data;
  } catch (error) {
    console.error(error);
    return [];
  }
}

/**
 * send out a streamlined API-call to get stored companies and map the response to a search string request the search page
 * @param  {string} searchString           the string that is used to search companies
 * @param {any} keycloakPromise            a promise to the Keycloak Object for the Frontend
 * @param chunkSize                        size of requested chunk
 * @returns the search result companies
 */
export async function getCompanyDataForFrameworkDataSearchPageWithoutFilters(
  searchString: string,
  keycloakPromise: Promise<Keycloak>,
  chunkSize?: number
): Promise<Array<CompanyIdAndName>> {
  try {
    const companyDataControllerApi = new ApiClientProvider(keycloakPromise).backendClients.companyDataController;
    return (await companyDataControllerApi.getCompaniesBySearchString(searchString, chunkSize)).data;
  } catch (error) {
    console.error(error);
    return [];
  }
}

/**
 * send out an API-call to count stored companies that satisfy the filters
 * @param  {string} searchString           the string that is used to search companies
 *                                         by name, or additionally by identifier values
 * @param {Array<string>} frameworkFilter
 *                                         search for companies that hold at least one data set for at least one of
 *                                         the frameworks mentioned in frameworksToFilter and don't filter if
 *                                         frameworksToFilter is empty
 * @param countryCodeFilter                If not empty only companies whose headquarter is in one of the
 *                                         countries specified by the country codes are returned
 * @param sectorFilter                     If not empty only companies whose sector is in the set is returned
 * @param {any} keycloakPromise            a promise to the Keycloak Object for the Frontend
 * @returns the number of result companies
 */
export async function getNumberOfCompaniesForFrameworkDataSearchPage(
  searchString: string,
  frameworkFilter: Set<DataTypeEnum>,
  countryCodeFilter: Set<string>,
  sectorFilter: Set<string>,
  keycloakPromise: Promise<Keycloak>
): Promise<number> {
  try {
    if (searchString.length + frameworkFilter.size + countryCodeFilter.size + sectorFilter.size == 0) {
      frameworkFilter = new Set<DataTypeEnum>(Object.values(DataTypeEnum));
    }
    const companyDataControllerApi = new ApiClientProvider(keycloakPromise).backendClients.companyDataController;
    const response = await companyDataControllerApi.getNumberOfCompanies(
      searchString,
      frameworkFilter,
      countryCodeFilter,
      sectorFilter
    );
    return response.data;
  } catch (error) {
    console.error(error);
    return 0;
  }
}
