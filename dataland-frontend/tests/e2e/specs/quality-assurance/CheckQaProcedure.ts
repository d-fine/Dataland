import { DataMetaInformation, EuTaxonomyDataForFinancials, LksgData, StoredCompany } from "@clients/backend";
import { describeIf } from "@e2e/support/TestUtility";
import { getKeycloakToken, login } from "@e2e/utils/Auth";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { getBaseUrl, reviewer_name, reviewer_pw, uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { uploadOneEuTaxonomyFinancialsDatasetViaApi } from "@e2e/utils/EuTaxonomyFinancialsUpload";
import { uploadOneLksgDatasetViaApi } from "@e2e/utils/LksgUpload";
import { FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";

describeIf(
  "As a user, I expect to be able to add a new dataset and see it as pending",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
    onlyExecuteOnReset: false,
  },
  function () {
    let storedCompany: StoredCompany;
    let preparedEuTaxonomyFixtures: Array<FixtureData<EuTaxonomyDataForFinancials>>;
    let preparedLksgFixtures: Array<FixtureData<LksgData>>;

    before(function () {
      cy.fixture("CompanyInformationWithEuTaxonomyDataForFinancialsPreparedFixtures").then(function (jsonContent) {
        preparedEuTaxonomyFixtures = jsonContent as Array<FixtureData<EuTaxonomyDataForFinancials>>;
      });

      cy.fixture("CompanyInformationWithLksgPreparedFixtures").then(function (jsonContent) {
        preparedLksgFixtures = jsonContent as Array<FixtureData<LksgData>>;
      });

      getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
        const testCompany = generateDummyCompanyInformation(`company-for-testing-qa-${new Date().getTime()}`);
        return uploadCompanyViaApi(token, testCompany).then((newCompany) => (storedCompany = newCompany));
      });
    });

    it("Check whether newly added dataset has Pending status and can be approved by a reviewer", () => {
      const data = getPreparedFixture("company-for-all-types", preparedEuTaxonomyFixtures);
      getKeycloakToken(uploader_name, uploader_pw).then(async (token: string) => {
        cy.ensureLoggedIn(uploader_name, uploader_pw);
        await uploadOneEuTaxonomyFinancialsDatasetViaApi(token, storedCompany.companyId, "2022", data.t, false);
        testSubmittedDatasetIsInReviewListAndPending(storedCompany.companyInformation.companyName);
      });
    });

    it("Check whether newly added dataset has Rejected status and can be edited", () => {
      const data = getPreparedFixture("lksg-all-fields", preparedLksgFixtures);
      getKeycloakToken(uploader_name, uploader_pw).then(async (token: string) => {
        cy.ensureLoggedIn(uploader_name, uploader_pw);
        const dataMetaInfo = await uploadOneLksgDatasetViaApi(token, storedCompany.companyId, "2022", data.t, false);
        testSubmittedDatasetIsInReviewListAndRejected(storedCompany, dataMetaInfo);
      });
    });
  }
);

/**
 * Tests that the item was added and is visible on the QA list
 * @param companyName The name of the company
 */
function testSubmittedDatasetIsInReviewListAndPending(companyName: string): void {
  testDatasetPresentWithCorrectStatus(companyName, "PENDING");

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
  login(uploader_name, uploader_pw);

  testDatasetPresentWithCorrectStatus(companyName, "APPROVED");
}

/**
 * Tests that the item was added and is visible on the QA list
 * @param storedCompany the stored company uploading the dataset
 * @param dataset the data meta information that wa suploaded
 */
function testSubmittedDatasetIsInReviewListAndRejected(
  storedCompany: StoredCompany,
  dataset: DataMetaInformation
): void {
  login(reviewer_name, reviewer_pw);
  cy.visitAndCheckAppMount("/qualityassurance");

  cy.intercept(`**/api/metadata/${dataset.dataId}`).as("getMetadata");
  cy.intercept(`**/api/companies/${storedCompany.companyId}`).as("getCompanyInformation");

  cy.wait("@getMetadata").wait("@getCompanyInformation");

  cy.get('[data-test="qa-review-section"] .p-datatable-tbody').last().click();
  cy.get(".p-dialog").get('button[id="reject-button"]').should("exist").click();

  safeLogout();
  login(uploader_name, uploader_pw);

  testDatasetPresentWithCorrectStatus(storedCompany.companyInformation.companyName, "REJECTED");

  cy.visitAndCheckAppMount(`/companies/${storedCompany.companyId}/frameworks/lksg/${dataset.dataId}`);
  cy.get('[data-test="datasetDisplayStatusContainer"]').should("exist");
  cy.get('button[data-test="editDatasetButton"]').should("exist").click();

  cy.url().should(
    "eq",
    getBaseUrl() + `/companies/${storedCompany.companyId}/frameworks/lksg/upload?templateDataId=${dataset.dataId}`
  );
}

/**
 * Visitst the datasets page and verifies that the last dataset matches the company name and expected status
 * @param companyName The name of the company that just uploaded
 * @param status The current expected status of the dataset
 */
function testDatasetPresentWithCorrectStatus(companyName: string, status: string): void {
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
  cy.visitAndCheckAppMount("/")
    .wait(1000)
    .get("div[id='profile-picture-dropdown-toggle']")
    .click()
    .get("a[id='profile-picture-dropdown-logout-anchor']")
    .click();
}
