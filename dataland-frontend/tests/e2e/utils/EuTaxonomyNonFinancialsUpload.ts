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
  cy.get('[data-test="reportingPeriod"] button').should("have.class", "p-datepicker-trigger").should("exist");
  cy.get('input[name="fiscalYearDeviation"][value="Deviation"]').check();
  cy.get('div[id="jumpLinks"] li:last a').click();
  cy.window().then((win) => {
    const scrollPosition = win.scrollY;
    expect(scrollPosition).to.be.greaterThan(0);
  });
  cy.get('[data-test="fiscalYearEnd"] button').should("have.class", "p-datepicker-trigger").should("exist");
  cy.get('input[name="fiscalYearEnd"]').should("not.be.visible");
  cy.get('input[name="reference"]').should("not.exist");
  cy.get('input[name="fiscalYearEnd"]').type("2022-03-03", { force: true });
  cy.get('input[name="scopeOfEntities"][value="Yes"]').check();
  cy.get('input[name="activityLevelReporting"][value="Yes"]').check();
  cy.get('[data-test="dataPointToggleTitle"]').should("not.exist");
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
