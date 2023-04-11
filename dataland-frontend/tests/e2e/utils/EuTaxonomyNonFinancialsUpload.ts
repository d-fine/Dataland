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
): Cypress.Chainable<string> {
  cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}/upload`);

  cy.get('button[data-test="upload-files-button"]').click();
  cy.get("input[type=file]").selectFile("tests/e2e/fixtures/pdfTest.pdf", { force: true });
  cy.get('div[data-test="uploaded-files"]').find('[data-test="uploaded-files-title"]').should("contain", "pdf");
  cy.get('div[data-test="uploaded-files"]').find('[data-test="uploaded-files-size"]').should("contain", "KB");
  cy.get('button[data-test="uploaded-files-remove"]').click();
  cy.get('div[data-test="uploaded-files"]').should("not.exist");

  cy.get('button[data-test="upload-files-button"]').click();
  cy.get("input[type=file]").selectFile("tests/e2e/fixtures/pdfTest.pdf", { force: true });
  cy.get('div[data-test="uploaded-files"]')
    .should("exist")
    .find('[data-test="uploaded-files-title"]')
    .should("contain", "pdf");
  cy.get('input[name="currency"]').type("qqq");

  cy.get('[data-test="fiscalYearEnd"] button').should("have.class", "p-datepicker-trigger").click();
  cy.get("div.p-datepicker").find('button[aria-label="Next Month"]').click();
  cy.get("div.p-datepicker").find('span:contains("11")').click();
  cy.get('input[name="fiscalYearEnd"]').invoke("val").should("contain", "11");
  cy.get('input[name="fiscalYearDeviation"][value="Deviation"]').check();
  cy.get('div[id="jumpLinks"] li:last a').click();
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
  cy.get('[data-test="assuranceSection"] select[name="report"]').select(1);

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
    cy.get(`div[data-test=${argument}] select[name="quality"]`).each(($element) => {
      cy.wrap($element).select(3);
    });
  }
  cy.intercept(`**/api/data/${DataTypeEnum.EutaxonomyNonFinancials}`).as("postCompanyAssociatedData");
  cy.get('button[data-test="submitButton"]').click();
  cy.wait("@postCompanyAssociatedData");
  return cy.contains("h4", "dataId").then<string>(($dataId): string => {
    return $dataId.text();
  });
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
