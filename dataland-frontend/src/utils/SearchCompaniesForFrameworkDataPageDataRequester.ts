/**
 * Module for getting stored companies by sending out an API-call and mapping the received stored companies to a
 * specific schema which is needed on the search page
 */

import { ApiClientProvider } from "@/services/ApiClients";
import { StoredCompany, CompanyInformation, DataMetaInformation, DataTypeEnum, QAStatus } from "@clients/backend";
import Keycloak from "keycloak-js";
import { ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE } from "@/utils/Constants";
import { useFrameworkFiltersStore } from "@/stores/stores";

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
 * @returns the search result companies
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
      frameworkFilter = new Set(ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE);
    }

    const response = await companyDataControllerApi.getCompanies(
      searchString,
      frameworkFilter,
      countryCodeFilter,
      sectorFilter,
      onlyCompanyNames
    );
    const responseData: Array<StoredCompany> = response.data;
    mappedResponse = mapStoredCompanyToFrameworkDataSearchPage(filterCompaniesForAcceptedDataset(responseData));
  } catch (error) {
    console.error(error);
  }
  return mappedResponse;
}

/**
 * Filters an array of companies for companies which have at least one data set which may be displayed
 * i.e. a dataset that has quality status "Accepted"
 * @param companies the companies to filter
 * @returns the filtered companies
 */
function filterCompaniesForAcceptedDataset(companies: StoredCompany[]): StoredCompany[] {
  return companies.filter((company) =>
    company.dataRegisteredByDataland.some((dataMetaInfo) => dataMetaInfo.qaStatus == QAStatus.Accepted)
  );
}

/**
 * Generates a router link for the view button on the framework search page, or if an autocomplete suggestion is selected.
 * If no filter is set, or if the number of framework-filters equals the number of all viewable frameworks,
 * it links to the first framework that is included in the data in the company object.
 * Otherwise, it links to the first framework that is included in the currently stored framework filters.
 * @param companyData the company to generate a link for
 * @returns a vue router link to the view page for a specific framework
 */
export function getRouterLinkTargetFramework(companyData: DataSearchStoredCompany): string {
  const dataRegisteredByDataland = companyData.dataRegisteredByDataland;
  let routeToVisit = `/companies/${companyData.companyId}/frameworks/${dataRegisteredByDataland[0].dataType}`;
  const selectedFiltersForFrameworks = useFrameworkFiltersStore().selectedFiltersForFrameworks;
  if (
    selectedFiltersForFrameworks.length > 0 &&
    selectedFiltersForFrameworks.length < ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE.length
  ) {
    const frameworkToRouteTo = dataRegisteredByDataland.find((dataMetaInfo) => {
      return selectedFiltersForFrameworks.includes(dataMetaInfo.dataType);
    })?.dataType;
    if (frameworkToRouteTo) {
      routeToVisit = `/companies/${companyData.companyId}/frameworks/${frameworkToRouteTo}`;
    } else {
      throw new Error(
        "No data meta info for the frameworks set in the filters could be found in the data of the server response."
      );
    }
  }
  return routeToVisit;
}
