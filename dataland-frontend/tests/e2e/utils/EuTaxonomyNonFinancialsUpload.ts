import { DataTypeEnum } from "@clients/backend";
import { submitButton } from "@sharedUtils/components/SubmitButton";
import { TEST_PDF_FILE_NAME } from "@sharedUtils/ConstantsForPdfs";
import { selectSingleReportAndFillWithData } from "@e2e/utils/UploadUtils";

/**
 * Uploads a single eutaxonomy-non-financials data entry for a company via the Dataland upload form
 * @param companyId The Id of the company to upload the dataset for
 */
export function uploadEuTaxonomyDataForNonFinancialsViaForm(companyId: string): void {
  cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}/upload`);
  submitButton.buttonIsAddDataButton();
  submitButton.buttonAppearsDisabled();
  selectSingleReportAndFillWithData();

  fillAndValidateEuTaxonomyForNonFinancialsUploadForm(TEST_PDF_FILE_NAME);
  submitButton.buttonAppearsEnabled();
  cy.intercept({
    url: `**/api/data/${DataTypeEnum.EutaxonomyNonFinancials}`,
    times: 1,
  }).as("postCompanyAssociatedData");
  submitButton.clickButton();
  cy.wait("@postCompanyAssociatedData", { timeout: Cypress.env("medium_timeout_in_ms") as number });
}

/**
 * Fills all the fields of the eu-taxonomy upload form for non-financial companies
 * @param assuranceReportName name of the assurance data source
 */
export function fillAndValidateEuTaxonomyForNonFinancialsUploadForm(assuranceReportName: string): void {
  cy.get('[data-test="fiscalYearEnd"] button').should("have.class", "p-datepicker-trigger").click();
  cy.get('input[name="fiscalYearEnd"]').should("not.be.visible");
  cy.get("div.p-datepicker").find('button[aria-label="Next Month"]').click();
  cy.get("div.p-datepicker").find('span:contains("11")').click();
  cy.get('input[name="fiscalYearEnd"]').invoke("val").should("contain", "11");
  cy.get('div[data-test="fiscalYearDeviation"]').get('input[value="Deviation"]').check();
  cy.get('div[data-test="submitSideBar"] li:last a').click();
  cy.window().then((win) => {
    const scrollPosition = win.scrollY;
    expect(scrollPosition).to.be.greaterThan(0);
  });
  cy.get('div[data-test="scopeOfEntities"]').get('input[value="Yes"]').check();
  cy.get('div[data-test="euTaxonomyActivityLevelReporting"]').get('input[value="Yes"]').check();
  cy.get('input[name="numberOfEmployees"]').type("-13");
  cy.get('em[title="Number Of Employees"]').click();
  cy.get(`[data-message-type="validation"]`).should("contain", "at least 0").should("exist");
  cy.get('input[name="numberOfEmployees"]').clear().type("333");
  cy.get('div[data-test="nfrdMandatory"]').get('input[value="Yes"]').check();
  cy.get('select[name="value"]').select(1);
  cy.get('input[name="provider"]').type("Some Assurance Provider Company");
  cy.get('select[name="fileName"]').eq(0).select(assuranceReportName);
  cy.get('input[name="page"]').eq(0).type("-13");
  cy.get('em[title="Assurance"]').click();
  cy.get(`[data-message-type="validation"]`).should("exist").should("contain", "at least 0");
  cy.get('input[name="page"]').eq(0).clear().type("1");
  cy.get('div[name="revenue"]').within(() => {
    cy.get('input[name="value"]').type("250700");
    cy.get('select[data-test="datapoint-currency"]').select(1);
    cy.get('select[name="quality"]').select(1);
  });
  cy.get('div[name="capex"]').within(() => {
    cy.get('input[name="value"]').type("450700");
    cy.get('select[data-test="datapoint-currency"]').select(10);
    cy.get('select[name="quality"]').select(1);
  });
}
