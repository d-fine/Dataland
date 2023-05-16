import { EuTaxonomyDataForFinancials } from "@clients/backend";
import { describeIf } from "@e2e/support/TestUtility";
import { uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import {
  companyViaApiAndEuTaxonomyDataForFinancialsViaForm,
  fillEligibilityKpis,
} from "@e2e/utils/EuTaxonomyFinancialsUpload";
import { FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { dateFormElement } from "@sharedUtils/components/DateFormElement";

describeIf(
  "As a user, I expect to be able to add and remove Eligible KPIs and send the form successfully",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
    dataEnvironments: ["fakeFixtures"],
  },
  function () {
    beforeEach(() => {
      cy.ensureLoggedIn(uploader_name, uploader_pw);
    });

    let testData: FixtureData<EuTaxonomyDataForFinancials>;

    before(function () {
      cy.fixture("CompanyInformationWithEuTaxonomyDataForFinancialsPreparedFixtures").then(function (jsonContent) {
        const preparedFixtures = jsonContent as Array<FixtureData<EuTaxonomyDataForFinancials>>;
        testData = getPreparedFixture("company-for-all-types", preparedFixtures);
      });
    });

    it.only("Check", () => {
      testData.companyInformation.companyName = "company-for-all-types";
      companyViaApiAndEuTaxonomyDataForFinancialsViaForm(
        testData.companyInformation,
        testData.t,
        () => undefined,
        fillAndValidateEuTaxonomyForFinancialsUploadForm,
        () => undefined,
        () => undefined,
        () => undefined
      );
    });
  }
);

/**
 * Fills the eutaxonomy-financials upload form with the given dataset
 * @param data the data to fill the form with
 */
function fillAndValidateEuTaxonomyForFinancialsUploadForm(data: EuTaxonomyDataForFinancials): void {
  dateFormElement.selectDayOfNextMonth("fiscalYearEnd", 12);
  dateFormElement.validateDay("fiscalYearEnd", 12);

  // Required data entry
  if (data.reportingObligation !== undefined) {
    cy.get(`input[name="reportingObligation"][value=${data.reportingObligation.toString()}]`).check();
  }

  cy.get(
    `input[name="fiscalYearDeviation"][value=${
      data.fiscalYearDeviation ? data.fiscalYearDeviation.toString() : "Deviation"
    }]`
  ).check();

  cy.get('input[name="numberOfEmployees"]').type(
    `${data.numberOfEmployees ? data.numberOfEmployees.toString() : "13"}`
  );

  cy.get('[data-test="assuranceSection"] select[name="assurance"]').select(2);
  cy.get('[data-test="assuranceSection"] input[name="provider"]').type("Assurance Provider", { force: true });
  cy.get('[data-test="assuranceSection"] select[name="report"]').select(1);

  // Adding and deleting KPIs
  // cy.get('[data-test="MultiSelectfinancialServicesTypes"]')
  //   .click()
  //   .get("div.p-multiselect-panel")
  //   .find("li.p-multiselect-item")
  //   .first()
  //   .click();

  // cy.get('[data-test="MultiSelectfinancialServicesTypes"]')
  //   .click()
  //   .get("div.p-multiselect-panel")
  //   .find("li.p-multiselect-item").not('li.p-highlight')
  //   .first()
  //   .click();

  cy.get('[data-test="MultiSelectfinancialServicesTypes"]')
    .click()
    .get("div.p-multiselect-panel")
    .find("li.p-multiselect-item")
    .each(($el) => {
      cy.wrap($el).click({ force: true });
    });

  cy.get('[data-test="addKpisButton"]').click({ force: true });

  cy.get('[data-test="removeSectionButton"]').each(($el, index) => {
    if (index > 0) {
      cy.wrap($el).click({ force: true });
    }
  });

  // cy.get('[data-test="addKpisButton"]').click({ force: true });
  // cy.get('button[data-test="removeSectionButton"]').should("exist").should("have.class", "ml-auto");
  // cy.get('[data-test="removeSectionButton"]').click({ force: true });

  // cy.get('[data-test="dataPointToggle"]')
  //   .eq(1)
  //   .should("exist")
  //   .should("contain.text", "Data point is available")
  //   .find('[data-test="dataPointToggleButton"]')
  //   .click();

  // cy.get('[data-test="dataPointToggle"]').eq(1).find('[data-test="dataPointToggleButton"]').click();

  // cy.get('button[data-test="removeSectionButton"]').should("exist").should("have.class", "ml-auto");
  // cy.get('[data-test="removeSectionButton"]').click({ force: true });

  // fillEligibilityKpis("creditInstitutionKpis", data.eligibilityKpis?.CreditInstitution);
  fillEligibilityKpis("investmentFirmKpis", data.eligibilityKpis?.InvestmentFirm);
  // fillField("investmentFirmKpis", "greenAssetRatio", data.investmentFirmKpis?.greenAssetRatio);
}
