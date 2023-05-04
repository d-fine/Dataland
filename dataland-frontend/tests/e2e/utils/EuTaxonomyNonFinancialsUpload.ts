import {
  Configuration,
  DataMetaInformation,
  DataTypeEnum,
  EuTaxonomyDataForFinancials,
  EuTaxonomyDataForNonFinancials,
  EuTaxonomyDataForNonFinancialsControllerApi,
} from "@clients/backend";
import { FixtureData } from "@sharedUtils/Fixtures";
import Chainable = Cypress.Chainable;
import { uploadReports } from "@sharedUtils/components/UploadReports";
import { submitButton } from "@sharedUtils/components/SubmitButton";
import { TEST_PDF_FILE_NAME } from "@e2e/utils/Constants";

/**
 * Uploads a single eutaxonomy-non-financials data entry for a company via the Dataland upload form
 *
 * @param companyId The Id of the company to upload the dataset for
 * @param valueFieldNotFilled Value which, if true, disables the value field
 * @returns the id of the dataset that has been uploaded
 */
export function uploadEuTaxonomyDataForNonFinancialsViaForm(
  companyId: string,
  valueFieldNotFilled = false
) :any {
  cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}/upload`);
  submitButton.buttonIsAddDataButton();
  submitButton.buttonAppearsDisabled();
  uploadReports.uploadFile(TEST_PDF_FILE_NAME);
  uploadReports.validateSingleFileInUploadedList(TEST_PDF_FILE_NAME, "KB");
  uploadReports.validateFileInfo(TEST_PDF_FILE_NAME);

  fillAndValidateEuTaxonomyForNonFinancialsUploadForm(valueFieldNotFilled, TEST_PDF_FILE_NAME);
  submitButton.buttonAppearsEnabled();
  cy.intercept(`**/api/data/${DataTypeEnum.EutaxonomyNonFinancials}`).as("postCompanyAssociatedData");
  submitButton.clickButton();
  cy.wait("@postCompanyAssociatedData");
}

/**
 * Fills all the fields of the eu-taxonomy upload form for non-financial companies
 *
 * @param valueFieldNotFilled Value which, if true, disables the value field
 * @param assuranceReportName name of the assurance data source
 */
export function fillAndValidateEuTaxonomyForNonFinancialsUploadForm(
  valueFieldNotFilled: boolean,
  assuranceReportName: string
): void {
  cy.get('[data-test="fiscalYearEnd"] button').should("have.class", "p-datepicker-trigger").click();
  cy.get("div.p-datepicker").find('button[aria-label="Next Month"]').click();
  cy.get("div.p-datepicker").find('span:contains("11")').click();
  cy.get('input[name="fiscalYearEnd"]').invoke("val").should("contain", "11");
  cy.get('input[name="fiscalYearDeviation"][value="Deviation"]').check();
  cy.get('div[data-test="submitSideBar"] li:last a').click();
  cy.window().then((win) => {
    const scrollPosition = win.scrollY;
    expect(scrollPosition).to.be.greaterThan(0);
  });
  cy.get('[data-test="fiscalYearEnd"] button').should("have.class", "p-datepicker-trigger").should("exist");
  cy.get('input[name="fiscalYearEnd"]').should("not.be.visible");
  cy.get('input[name="scopeOfEntities"][value="Yes"]').check();

  cy.get('input[name="activityLevelReporting"][value="Yes"]').check();
  cy.get('input[name="numberOfEmployees"]').type("333");
  cy.get('input[name="reportingObligation"][value="Yes"]').check();

  cy.get('[data-test="assuranceSection"] select[name="assurance"]').select(1);
  cy.get('[data-test="assuranceSection"] input[name="provider"]').type("Assurance Provider");
  cy.get('[data-test="assuranceSection"] select[name="report"]').select(assuranceReportName);

  cy.get('[data-test="dataPointToggleTitle"]').should("exist");
  for (const argument of ["capexSection", "opexSection", "revenueSection"]) {
    if (!valueFieldNotFilled) {
      cy.get(`div[data-test=${argument}] input[name="value"]`).each(($element, index) => {
        const inputNumber = 10 * index + 7;
        cy.wrap($element).type(inputNumber.toString());
      });
    }
    cy.get(`div[data-test=${argument}] select[name="report"]`).each(($element) => {
      cy.wrap($element).select(1);
    });
    cy.get(`div[data-test=${argument}] input[name="page"]`).each(($element) => {
      cy.wrap($element).type("12");
    });
    cy.get(`div[data-test=${argument}] select[name="quality"]`).each(($element) => {
      cy.wrap($element).select(3);
    });
    cy.get(`div[data-test=${argument}] textarea[name="comment"]`).each(($element) => {
      cy.wrap($element).type("test");
    });
  }
}

/**
 * Extracts the first eutaxonomy-non-financials dataset from the fake fixtures
 *
 * @returns the first eutaxonomy-non-financials dataset from the fake fixtures
 */
export function getFirstEuTaxonomyNonFinancialsFixtureDataFromFixtures(): Chainable<
  FixtureData<EuTaxonomyDataForNonFinancials>
> {
  return cy.fixture("CompanyInformationWithEuTaxonomyDataForNonFinancials").then(function (jsonContent) {
    const companiesWithEuTaxonomyDataForNonFinancials = jsonContent as Array<
      FixtureData<EuTaxonomyDataForNonFinancials>
    >;
    return companiesWithEuTaxonomyDataForNonFinancials[0];
  });
}

/**
 * Uploads a single eutaxonomy-non-financials data entry for a company via the Dataland API
 *
 * @param token The API bearer token to use
 * @param companyId The Id of the company to upload the dataset for
 * @param reportingPeriod The reporting period to use for the upload
 * @param data The Dataset to upload
 * @returns a promise on the created data meta information
 */
export async function uploadOneEuTaxonomyNonFinancialsDatasetViaApi(
  token: string,
  companyId: string,
  reportingPeriod: string,
  data: EuTaxonomyDataForFinancials
): Promise<DataMetaInformation> {
  const dataMetaInformation = await new EuTaxonomyDataForNonFinancialsControllerApi(
    new Configuration({ accessToken: token })
  ).postCompanyAssociatedEuTaxonomyDataForNonFinancials({
    companyId,
    reportingPeriod,
    data,
  });
  return dataMetaInformation.data;
}
