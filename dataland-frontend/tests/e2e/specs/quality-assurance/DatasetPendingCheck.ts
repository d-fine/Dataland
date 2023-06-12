import { DataTypeEnum, EuTaxonomyDataForFinancials } from "@clients/backend";
import { describeIf } from "@e2e/support/TestUtility";
import { login } from "@e2e/utils/Auth";
import { generateDummyCompanyInformation } from "@e2e/utils/CompanyUpload";
import { reviewer_name, reviewer_pw, uploader_name, uploader_pw } from "@e2e/utils/Cypress";
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
      login(uploader_name, uploader_pw);

      uploadCompanyViaApiAndEuTaxonomyDataViaForm<EuTaxonomyDataForFinancials>(
        DataTypeEnum.EutaxonomyFinancials,
        testCompany,
        testData.t,
        fillEuTaxonomyForm,
        (req) => (req.headers["REQUIRE-QA"] = "true"),
        () => testSubmittedDatasetIsInReviewList(companyName)
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
 * @param companyName The name of the company
 */
function testSubmittedDatasetIsInReviewList(companyName: string): void {
  testDatasetPresent(companyName, "PENDING");

  safeLogout();

  login(reviewer_name, reviewer_pw);

  cy.visit("/qualityassurance").wait(1000);

  cy.get('[data-test="qa-review-section"] .p-datatable-tbody tr')
    .last()
    .should("exist")
    .get(".qa-review-company-name")
    .should("contain", companyName);

  cy.get('[data-test="qa-review-section"] .p-datatable-tbody tr').last().click();

  cy.get(".p-dialog").should("exist").get(".p-dialog-header").should("contain", companyName);
  cy.get(".p-dialog").get('.p-dialog-content pre[id="dataset-container"]').should("not.be.empty");
  cy.get(".p-dialog").get('button[id="accept-button"]').should("exist").click();

  safeLogout();
  login(uploader_name, uploader_pw);

  testDatasetPresent(companyName, "APPROVED");
}

/**
 * Visitst the datasets page and verifies that the last dataset matches the company name and expected status
 * @param companyName The name of the company that just uploaded
 * @param status The current expected status of the dataset
 */
function testDatasetPresent(companyName: string, status: string): void {
  cy.visit("/datasets").wait(1000);

  cy.get('[data-test="datasets-table"] .p-datatable-tbody tr')
    .first()
    .should("exist")
    .get(".data-test-company-name")
    .should("contain", companyName);

  cy.get('[data-test="datasets-table"]').first().get('span[data-test="data-test-status"]').should("contain", status);
}

/**
 * Logs the user out without testing the url
 */
function safeLogout(): void {
  cy.get("div[id='profile-picture-dropdown-toggle']")
    .click()
    .wait(1000)
    .get("a[id='profile-picture-dropdown-logout-anchor']")
    .click();
}
