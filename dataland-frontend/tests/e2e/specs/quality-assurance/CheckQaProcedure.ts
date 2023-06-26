import { DataTypeEnum, EuTaxonomyDataForFinancials } from "@clients/backend";
import { describeIf } from "@e2e/support/TestUtility";
import { login } from "@e2e/utils/Auth";
import { generateDummyCompanyInformation } from "@e2e/utils/CompanyUpload";
import { admin_name, admin_pw, reviewer_name, reviewer_pw } from "@e2e/utils/Cypress";
import { fillAndValidateEuTaxonomyCreditInstitutionForm, prepareFixture } from "@e2e/utils/EuTaxonomyFinancialsUpload";
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
    const testCompany = generateDummyCompanyInformation(`company-for-testing-qa-${new Date().getTime()}`);

    before(function () {
      const fixtureType = "CompanyInformationWithEuTaxonomyDataForFinancialsPreparedFixtures";
      prepareFixture<EuTaxonomyDataForFinancials>(fixtureType).then((preparedFixtures) => {
        testData = getPreparedFixture("company-for-all-types", preparedFixtures);
      });
    });

    it("Check whether newly added dataset has Pending status and can be approved by a reviewer", () => {
      uploadCompanyViaApiAndEuTaxonomyDataViaForm<EuTaxonomyDataForFinancials>(
        DataTypeEnum.EutaxonomyFinancials,
        testCompany,
        testData.t,
        (data) => fillAndValidateEuTaxonomyCreditInstitutionForm(data),
        (req) => (req.headers["REQUIRE-QA"] = "true"),
        () => testSubmittedDatasetIsInReviewList(testCompany.companyName)
      );
    });
  }
);

/**
 * Tests that the item was added and is visible on the QA list
 * @param companyName The name of the company
 */
function testSubmittedDatasetIsInReviewList(companyName: string): void {
  testDatasetPresent(companyName, "PENDING");

  safeLogout();

  login(reviewer_name, reviewer_pw);

  cy.visitAndCheckAppMount("/qualityassurance");

  cy.get('[data-test="qa-review-section"] .p-datatable-tbody')
    .last()
    .should("exist")
    .get(".qa-review-company-name")
    .should("contain", companyName);

  cy.get('[data-test="qa-review-section"] .p-datatable-tbody').last().click();

  cy.get(".p-dialog").should("exist").get(".p-dialog-header").should("contain", companyName);
  cy.get(".p-dialog").get('.p-dialog-content pre[id="dataset-container"]').should("not.be.empty");
  cy.get(".p-dialog").get('button[id="accept-button"]').should("exist").click();

  safeLogout();
  login(admin_name, admin_pw);

  testDatasetPresent(companyName, "APPROVED");
}

/**
 * Visitst the datasets page and verifies that the last dataset matches the company name and expected status
 * @param companyName The name of the company that just uploaded
 * @param status The current expected status of the dataset
 */
function testDatasetPresent(companyName: string, status: string): void {
  cy.visitAndCheckAppMount("/datasets");

  cy.get('[data-test="datasets-table"] .p-datatable-tbody')
    .first()
    .should("exist")
    .get(".data-test-company-name")
    .should("contain", companyName);

  cy.get('[data-test="datasets-table"]').first().get('span[data-test="qa-status"]').should("contain", status);
}

/**
 * Logs the user out without testing the url
 */
function safeLogout(): void {
  cy.get("div[id='profile-picture-dropdown-toggle']")
    .click()
    .get("a[id='profile-picture-dropdown-logout-anchor']")
    .click();
}
