import { DataTypeEnum, EuTaxonomyDataForFinancials } from "@clients/backend";
import { describeIf } from "@e2e/support/TestUtility";
import { generateDummyCompanyInformation } from "@e2e/utils/CompanyUpload";
import { admin_name, admin_pw } from "@e2e/utils/Cypress";
import { fillEligibilityKpis, fillField } from "@e2e/utils/EuTaxonomyFinancialsUpload";
import { uploadCompanyViaApiAndEuTaxonomyDataViaForm } from "@e2e/utils/GeneralApiUtils";
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
      cy.ensureLoggedIn(admin_name, admin_pw);
    });

    let testData: FixtureData<EuTaxonomyDataForFinancials>;
    const testCompany = generateDummyCompanyInformation("company-for-testing-kpi-sections");

    before(function () {
      cy.fixture("CompanyInformationWithEuTaxonomyDataForFinancialsPreparedFixtures").then(function (jsonContent) {
        const preparedFixtures = jsonContent as Array<FixtureData<EuTaxonomyDataForFinancials>>;
        testData = getPreparedFixture("company-for-all-types", preparedFixtures);
      });
    });

    it("Check wether it is possible to add and delete KPIs and send the form successfully", () => {
      uploadCompanyViaApiAndEuTaxonomyDataViaForm<EuTaxonomyDataForFinancials>(
        DataTypeEnum.EutaxonomyFinancials,
        testCompany,
        testData.t,
        fillAndValidateEuTaxonomyForFinancialsUploadForm,
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

  cy.get('button[data-test="removeSectionButton"]').should("exist").should("have.class", "ml-auto");

  fillEligibilityKpis("creditInstitutionKpis", data.eligibilityKpis?.CreditInstitution);
  fillField(
    "creditInstitutionKpis",
    "tradingPortfolioAndInterbankLoans",
    data.creditInstitutionKpis?.tradingPortfolioAndInterbankLoans
  );
  fillField("creditInstitutionKpis", "tradingPortfolio", data.creditInstitutionKpis?.tradingPortfolio);
  fillField("creditInstitutionKpis", "interbankLoans", data.creditInstitutionKpis?.interbankLoans);
  fillField("creditInstitutionKpis", "greenAssetRatio", data.creditInstitutionKpis?.greenAssetRatio);
}
