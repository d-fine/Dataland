import { type CompanyInformation, type DataTypeEnum } from "@clients/backend";
import { type CyHttpMessages, type Interception } from "cypress/types/net-stubbing";
import Chainable = Cypress.Chainable;
import { getKeycloakToken } from "./Auth";
import { admin_name, admin_pw, uploader_name, uploader_pw } from "./Cypress";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "./CompanyUpload";
import { submitFilledInEuTaxonomyForm } from "./EuTaxonomyFinancialsUpload";

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
  afterDatasetSubmission: (companyId: string) => void,
): void {
  getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
    return uploadCompanyViaApi(token, generateDummyCompanyInformation(companyInformation.companyName)).then(
      (storedCompany): void => {
        cy.ensureLoggedIn(admin_name, admin_pw);
        cy.visitAndCheckAppMount(`/companies/${storedCompany.companyId}/frameworks/${frameworkDataType}/upload`);
        formFill(testData);
        submitFilledInEuTaxonomyForm(submissionDataIntercept);
        afterDatasetSubmission(storedCompany.companyId);
      },
    );
  });
}
