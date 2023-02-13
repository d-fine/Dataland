/**
 * Module for getting stored companies by sending out an API-call and mapping the received stored companies to a
 * specific schema which is needed on the search page
 */

import { ApiClientProvider } from "@/services/ApiClients";
import {
  StoredCompany,
  CompanyInformation,
  DataMetaInformation,
  DataTypeEnum,
  DatasetQualityStatus,
} from "@clients/backend";
import Keycloak from "keycloak-js";
import { ARRAY_OF_FRONTEND_INCLUDED_FRAMEWORKS } from "@/utils/Constants";
import { useFiltersStore } from "@/stores/filters";

export interface DataSearchStoredCompany {
  companyName: string;
  companyInformation: CompanyInformation;
  companyId: string;
  permId: string;
  dataRegisteredByDataland: Array<DataMetaInformation>;
}

export interface FrameworkDataSearchFilterInterface {
  companyNameFilter: string;
  frameworkFilter: Array<DataTypeEnum>;
  countryCodeFilter: Array<string>;
  sectorFilter: Array<string>;
}

/**
 * Retrieve the value of the Perm Id of a company. Throws an exception if no perm id is found
 *
 * @param  {StoredCompany} storedCompany      is the company object for which the Perm Id should be retrieved
 * @returns the perm id retrieved from the company object. Empty string if no perm id is known.
 */
function retrievePermIdFromStoredCompany(storedCompany: StoredCompany): string {
  const permIdIdentifier = storedCompany.companyInformation.identifiers.filter(
    (identifier): boolean => identifier.identifierType === "PermId"
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
 * @returns a list of companies in the format expected by the search page
 */
function mapStoredCompanyToFrameworkDataSearchPage(responseData: Array<StoredCompany>): Array<DataSearchStoredCompany> {
  return responseData.map(
    (company): DataSearchStoredCompany => ({
      companyName: company.companyInformation.companyName,
      companyInformation: company.companyInformation,
      companyId: company.companyId,
      permId: retrievePermIdFromStoredCompany(company),
      dataRegisteredByDataland: company.dataRegisteredByDataland,
    })
  );
}

/**
 * send out an API-call to get stored companies and map the response to the required scheme for the search page
 *
 * @param  {string} searchString           the string that is used to search companies
 * @param  {boolean} onlyCompanyNames      boolean which decides if the searchString should only be used to query
 *                                         companies by name, or additionally by identifier values
 * @param {Array<string>} frameworkFilter
 *                                         search for companies that hold at least one data set for at least one of
 *                                         the frameworks mentioned in frameworksToFilter and don't filter if
 *                                         frameworksToFilter is empty
 * @param countryCodeFilter                If not empty only companies whose headquarter is in one of the
 *                                         countries specified by the country codes are returned
 * @param sectorFilter                     If not empty only companies whose sector is in the set is returned
 * @param {any} keycloakPromise            a promise to the Keycloak Object for the Frontend
 */
export async function getCompanyDataForFrameworkDataSearchPage(
  searchString: string,
  onlyCompanyNames: boolean,
  frameworkFilter: Set<DataTypeEnum>,
  countryCodeFilter: Set<string>,
  sectorFilter: Set<string>,
  keycloakPromise: Promise<Keycloak>
): Promise<Array<DataSearchStoredCompany>> {
  let mappedResponse: Array<DataSearchStoredCompany> = [];

  try {
    const companyDataControllerApi = await new ApiClientProvider(keycloakPromise).getCompanyDataControllerApi();
    if (frameworkFilter.size === 0) {
      frameworkFilter = new Set(ARRAY_OF_FRONTEND_INCLUDED_FRAMEWORKS);
    }

    const response = await companyDataControllerApi.getCompanies(
      searchString,
      frameworkFilter,
      countryCodeFilter,
      sectorFilter,
      onlyCompanyNames
    );
    const responseData: Array<StoredCompany> = response.data;
    mappedResponse = mapStoredCompanyToFrameworkDataSearchPage(
      filterCompaniesForAcceptedDataset(responseData)
    );
  } catch (error) {
    console.error(error);
  }
  return mappedResponse;
}

/**
 * Filters an array of companies for companies which have at least one data set which may be displayed
 * i.e. a dataset that has quality status "Accepted"
 *
 * @param companies the companies to filter
 * @returns the filtered companies
 */
function filterCompaniesForAcceptedDataset(companies: StoredCompany[]): StoredCompany[] {
  return companies.filter((company) =>
    company.dataRegisteredByDataland.some((dataMetaInfo) => dataMetaInfo.qualityStatus == DatasetQualityStatus.Accepted)
  );
}

/**
 * Generates a router link for the view button on the framework search page.
 * Links to the first framework that is included in the data in the company object
 *
 * @param companyData the company to generate a link for
 * @returns a vue router link to the first framework associated with the given company object
 */
export function getRouterLinkTargetFramework(companyData: DataSearchStoredCompany): string {
  const dataTypesForWhichCompanyHasData = companyData.dataRegisteredByDataland.map(
    (dataMetaInformation) => dataMetaInformation.dataType
  );
  const defaultRoute = `/companies/${companyData.companyId}/frameworks/${dataTypesForWhichCompanyHasData[0]}`;
  const filtersStore = useFiltersStore();
  if (filtersStore.selectedFiltersForFrameworks.length === 0) {
    return defaultRoute;
  } else {
    const dataTypesOfCompanyThatMatchTheFiltersStore = dataTypesForWhichCompanyHasData.filter((dataType) =>
      filtersStore.selectedFiltersForFrameworks.includes(dataType)
    );
    if (dataTypesOfCompanyThatMatchTheFiltersStore.length === 0) {
      return defaultRoute;
    } else {
      return `/companies/${companyData.companyId}/frameworks/${dataTypesOfCompanyThatMatchTheFiltersStore[0]}`;
    }
  }
}
