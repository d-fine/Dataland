import {
  CompanyDataControllerApi,
  CompanyInformation,
  Configuration,
  DataTypeEnum,
  StoredCompany,
} from "@clients/backend";
import { CyHttpMessages, Interception, RouteHandler } from "cypress/types/net-stubbing";
import Chainable = Cypress.Chainable;
import { getKeycloakToken } from "./Auth";
import { admin_name, admin_pw, uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "./CompanyUpload";

export interface UploadIds {
  companyId: string;
  dataId: string;
}

/**
 * Gets stored companies that have at least one dataset with the provided data type
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
 * Intercepts all data upload requests to the backend and sets the bypassQa flag
 */
export function interceptAllDataPostsAndBypassQa(): void {
  const handler: RouteHandler = (incomingRequest) => {
    const authorizationHeader = incomingRequest.headers["Authorization"] as string;
    const base64EncodedAuthorizationPayload = authorizationHeader.split(".")[1];
    const authorization = JSON.parse(atob(base64EncodedAuthorizationPayload)) as { realm_access: { roles: string[] } };
    if(authorization.realm_access.roles.includes("ROLE_REVIEWER")) {
      incomingRequest.query["bypassQa"] = "true";
    }
  };
  cy.intercept("/api/data/*", handler);
}

/**
 * Visits the edit page for a framework via UI navigation.
 * @param companyId the id of the company for which to edit a dataset
 * @param dataType the framework type
 * @returns a cypress chainable to the interception of the data request on the edit page
 */
export function goToEditFormOfMostRecentDataset(companyId: string, dataType: DataTypeEnum): Chainable<Interception> {
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

/**
 * Uploads a company via POST-request, then an EU Taxonomy dataset for financial companies for the uploaded company
 * via the form in the frontend, and then visits the view page where that dataset is displayed
 * @param frameworkDataType The EU Taxanomy framework being tested
 * @param companyInformation Company information to be used for the company upload
 * @param testData EU Taxonomy dataset for financial companies to be uploaded
 * @param formFill Steps involved to fill data of the upload form
 * @param submissionDataIntercept performs checks on the request itself
 * @param afterDatasetSubmission is performed after the data has been submitted
 */
export function uploadCompanyViaApiAndEuTaxonomyDataViaForm<T>(
  frameworkDataType: DataTypeEnum,
  companyInformation: CompanyInformation,
  testData: T,
  formFill: (data: T) => void,
  submissionDataIntercept: (request: CyHttpMessages.IncomingHttpRequest) => void,
  afterDatasetSubmission: (companyId: string) => void
): void {
  getKeycloakToken(admin_name, admin_pw).then((token: string) => {
    return uploadCompanyViaApi(token, generateDummyCompanyInformation(companyInformation.companyName)).then(
      (storedCompany): void => {
        cy.ensureLoggedIn(uploader_name, uploader_pw);
        cy.visitAndCheckAppMount(`/companies/${storedCompany.companyId}/frameworks/${frameworkDataType}/upload`);
        formFill(testData);
        submitFilledInEuTaxonomyForm(submissionDataIntercept);
        afterDatasetSubmission(storedCompany.companyId);
      }
    );
  });
}

/**
 * After a Eu Taxonomy financial or non financial form has been filled in this function submits the form and checks
 * if a 200 response is returned by the backend
 * @param submissionDataIntercept function that asserts content of an intercepted request
 */
export function submitFilledInEuTaxonomyForm(
  submissionDataIntercept: (request: CyHttpMessages.IncomingHttpRequest) => void
): void {
  const postRequestAlias = "postDataAlias";
  cy.intercept(
    {
      method: "POST",
      url: `**/api/data/**`,
      times: 1,
    },
    submissionDataIntercept
  ).as(postRequestAlias);
  cy.get('button[data-test="submitButton"]').click();
  cy.wait(`@${postRequestAlias}`, { timeout: Cypress.env("long_timeout_in_ms") as number }).then((interception) => {
    expect(interception.response?.statusCode).to.eq(200);
  });
  cy.contains("td", "EU Taxonomy");
}
