import { CompanyDataControllerApi, Configuration, DataTypeEnum, StoredCompany } from "@clients/backend";
import { Interception, RouteHandler } from "cypress/types/net-stubbing";
import Chainable = Cypress.Chainable;

export interface UploadIds {
  companyId: string;
  dataId: string;
}

/**
 * Gets stored companies that have at least one dataset with the provided data type
 *
 * @param token The API bearer token to use
 * @param dataType Data type for which the returned companies should have at least one dataset
 * @returns an array of stored companies
 */
export async function getStoredCompaniesForDataType(token: string, dataType: DataTypeEnum): Promise<StoredCompany[]> {
  const response = await new CompanyDataControllerApi(new Configuration({ accessToken: token })).getCompanies(
    undefined,
    new Set([dataType])
  );
  return response.data;
}

/**
 * Counts the number of stored companies which contain at least one dataset with the provided data type and the
 * total number of datasets for that datatype
 *
 * @param token The API bearer token to use
 * @param dataType The data type to use while counting companies and number of datasets for that data type
 * @returns an object which contains the resulting number of companies and number of datasets
 */
export async function countCompaniesAndDataSetsForDataType(
  token: string,
  dataType: DataTypeEnum
): Promise<{ numberOfCompaniesForDataType: number; numberOfDataSetsForDataType: number }> {
  const storedCompaniesForDataType = await getStoredCompaniesForDataType(token, dataType);
  let numberOfDataSetsForDataType = 0;
  storedCompaniesForDataType.forEach((storedCompany) => {
    numberOfDataSetsForDataType += storedCompany.dataRegisteredByDataland.length;
  });

  return {
    numberOfDataSetsForDataType,
    numberOfCompaniesForDataType: storedCompaniesForDataType.length,
  };
}

/**
 * Intercepts all requests to the backend, checks if a certain allow-flag is set in the headers, then checks if the
 * response has a status code greater or equal 500, and throws an error depending on the allow-flag
 */
export function interceptAllAndCheckFor500Errors(): void {
  const handler: RouteHandler = (incomingRequest) => {
    const is500ResponseAllowed = incomingRequest.headers["DATALAND-ALLOW-5XX"] === "true";
    delete incomingRequest.headers["DATALAND-ALLOW-5XX"];
    incomingRequest.continue((response) => {
      if (response.statusCode >= 500 && !is500ResponseAllowed) {
        assert(
          false,
          `Received a ${response.statusCode} Response from the Dataland backend (request to ${incomingRequest.url})`
        );
      }
    });
  };
  cy.intercept("/api/**", handler);
  cy.intercept("/api-keys/**", handler);
}

/**
 * Visits the edit page for a framework via UI navigation.
 *
 * @param companyId the id of the company for which to edit a dataset
 * @param dataType the framework type
 * @returns a cypress chainable to the interception of the data request on the edit page
 */
export function gotoEditFormOfMostRecentDataset(companyId: string, dataType: DataTypeEnum): Chainable<Interception> {
  const getRequestAlias = "getData";
  cy.intercept({
    method: "GET",
    url: "**/api/data/**",
    times: 2,
  }).as(getRequestAlias);
  cy.visit(`/companies/${companyId}/frameworks/${dataType}`);
  cy.wait(`@${getRequestAlias}`, { timeout: Cypress.env("medium_timeout_in_ms") as number });
  cy.get('[data-test="editDatasetButton"]').click();
  return cy.wait(`@${getRequestAlias}`, { timeout: Cypress.env("medium_timeout_in_ms") as number });
}
