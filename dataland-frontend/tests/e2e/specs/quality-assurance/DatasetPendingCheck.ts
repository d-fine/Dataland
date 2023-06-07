import { DataTypeEnum, EuTaxonomyDataForFinancials } from "@clients/backend";
import { describeIf } from "@e2e/support/TestUtility";
import { login } from "@e2e/utils/Auth";
import { generateDummyCompanyInformation } from "@e2e/utils/CompanyUpload";
import { admin_name, admin_pw, reviewer_name, reviewer_pw } from "@e2e/utils/Cypress";
import {
  fillEligibilityKpis,
  fillEuTaxonomyForFinancialsRequiredFields,
  fillField,
} from "@e2e/utils/EuTaxonomyFinancialsUpload";
import { uploadCompanyViaApiAndEuTaxonomyDataViaForm } from "@e2e/utils/GeneralApiUtils";
import { FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";

describeIf(
  "As a user, I expect to be able to add a new dataset and see it as pending",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
    dataEnvironments: ["fakeFixtures"],
  },
  function () {
    let testData: FixtureData<EuTaxonomyDataForFinancials>;
    const uuid = new Date().getTime();
    const companyName = `company-for-testing-qa-${uuid}`;
    const testCompany = generateDummyCompanyInformation(companyName);

    before(function () {
      cy.fixture("CompanyInformationWithEuTaxonomyDataForFinancialsPreparedFixtures").then(function (jsonContent) {
        const preparedFixtures = jsonContent as Array<FixtureData<EuTaxonomyDataForFinancials>>;
        testData = getPreparedFixture("company-for-all-types", preparedFixtures);
      });
    });

    it("Check wether newly added dataset has Pending status", () => {
      login(admin_name, admin_pw);

      uploadCompanyViaApiAndEuTaxonomyDataViaForm<EuTaxonomyDataForFinancials>(
        DataTypeEnum.EutaxonomyFinancials,
        testCompany,
        testData.t,
        fillEuTaxonomyForm,
        () => undefined,
        testSubmittedDatasetIsInReviewList
      );
    });
  }
);

/**
 * Fills the eutaxonomy-financials upload form with the given dataset
 * @param data the data to fill the form with
 */
function fillEuTaxonomyForm(data: EuTaxonomyDataForFinancials): void {
  fillEuTaxonomyForFinancialsRequiredFields(data);

  cy.get('[data-test="MultiSelectfinancialServicesTypes"]')
    .click()
    .get("div.p-multiselect-panel")
    .find("li.p-multiselect-item")
    .first()
    .click({ force: true });

  cy.get('[data-test="addKpisButton"]').click({ force: true });

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

/**
 * Tests that the item was added and is visible on the QA list
 * @param companyName
 */
function testSubmittedDatasetIsInReviewList(companyName: string): void {
  cy.get("div[id='profile-picture-dropdown-toggle']")
    .click()
    .wait(1000)
    .get("a[id='profile-picture-dropdown-logout-anchor']")
    .click();

  login(reviewer_name, reviewer_pw);

  cy.visit("/qualityassurance").wait(1000);

  cy.get('[data-test="qa-review-section"] .p-datatable-tbody').first().should("exist");
  cy.get('[data-test="qa-review-section"] .p-datatable-tbody')
    .get(".qa-review-company-name")
    .should("contain", companyName);
}
