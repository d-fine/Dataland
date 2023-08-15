import { DataMetaInformation, EuTaxonomyDataForFinancials, LksgData, StoredCompany } from "@clients/backend";
import { describeIf } from "@e2e/support/TestUtility";
import { getKeycloakToken, login } from "@e2e/utils/Auth";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { getBaseUrl, reviewer_name, reviewer_pw, uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { uploadOneEuTaxonomyFinancialsDatasetViaApi } from "@e2e/utils/EuTaxonomyFinancialsUpload";
import { FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { uploadFrameworkData } from "@e2e/utils/FrameworkUpload";

describeIf(
  "As a user, I expect to be able to add a new dataset and see it as pending",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
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

    beforeEach(() => {
      cy.ensureLoggedIn(uploader_name, uploader_pw);
    });

    it("Check whether newly added dataset has Pending status and can be approved by a reviewer", () => {
      const data = getPreparedFixture("company-for-all-types", preparedEuTaxonomyFixtures);
      getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
        return uploadOneEuTaxonomyFinancialsDatasetViaApi(token, storedCompany.companyId, "2022", data.t, false).then(
          () => testSubmittedDatasetIsInReviewListAndAcceptIt(storedCompany.companyInformation.companyName),
        );
      });
    });

    it("Check whether newly added dataset has Rejected status and can be edited", () => {
      const data = getPreparedFixture("lksg-all-fields", preparedLksgFixtures);
      getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
        return uploadFrameworkData("lksg", token, storedCompany.companyId, "2022", data.t, false).then((dataMetaInfo) =>
          testSubmittedDatasetIsInReviewListAndRejectIt(storedCompany, dataMetaInfo),
        );
      });
    });
  },
);

/**
 * Tests that the item was added and is visible on the QA list
 * @param companyName The name of the company
 */
function testSubmittedDatasetIsInReviewListAndAcceptIt(companyName: string): void {
  testDatasetPresentWithCorrectStatus(companyName, "PENDING");

  safeLogout();
  login(reviewer_name, reviewer_pw);

  viewRecentlyUploadedDatasetsInQaTable();

  cy.get('[data-test="qa-review-section"] .p-datatable-tbody')
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

  testDatasetPresentWithCorrectStatus(companyName, "APPROVED");
}

/**
 * Tests that the dataset is visible on the QA list and reject it and if the edit button is present on the view page
 * @param storedCompany the stored company owning the dataset
 * @param dataMetaInfo the data meta information of the dataset that that was uploaded before
 */
function testSubmittedDatasetIsInReviewListAndRejectIt(
  storedCompany: StoredCompany,
  dataMetaInfo: DataMetaInformation,
): void {
  login(reviewer_name, reviewer_pw);

  cy.intercept(`**/api/metadata/${dataMetaInfo.dataId}`).as("getMetadata");
  cy.intercept(`**/api/companies/${storedCompany.companyId}`).as("getCompanyInformation");

  viewRecentlyUploadedDatasetsInQaTable();

  cy.wait("@getMetadata").wait("@getCompanyInformation");

  cy.contains("td", dataMetaInfo.dataId).click();
  cy.get('button[id="reject-button"]').should("exist").click();

  safeLogout();
  login(uploader_name, uploader_pw);

  testDatasetPresentWithCorrectStatus(storedCompany.companyInformation.companyName, "REJECTED");

  cy.intercept(`**/api/data/lksg/${dataMetaInfo.dataId}`).as("getLksgDataset");
  cy.visitAndCheckAppMount(`/companies/${storedCompany.companyId}/frameworks/lksg/${dataMetaInfo.dataId}`);
  cy.wait("@getLksgDataset");
  cy.get('[data-test="datasetDisplayStatusContainer"]').should("exist");
  cy.get('button[data-test="editDatasetButton"]').should("exist").click();

  cy.url().should(
    "eq",
    getBaseUrl() + `/companies/${storedCompany.companyId}/frameworks/lksg/upload?templateDataId=${dataMetaInfo.dataId}`,
  );
}

/**
 * Visits the quality assurance page and switches to the last table page
 */
function viewRecentlyUploadedDatasetsInQaTable(): void {
  cy.intercept("**/qa/datasets").as("getQaQueue");
  cy.visitAndCheckAppMount("/qualityassurance");
  cy.wait("@getQaQueue");
  cy.get(".p-paginator-last", { timeout: Cypress.env("medium_timeout_in_ms") as number }).then((element) => {
    if (element.prop("disabled")) {
      return;
    }
    element.trigger("click");
  });
}

/**
 * Visits the datasets page and verifies that the last dataset matches the company name and expected status
 * @param companyName The name of the company that just uploaded
 * @param status The current expected status of the dataset
 */
function testDatasetPresentWithCorrectStatus(companyName: string, status: string): void {
  cy.intercept("**/api/companies*").as("getMyDatasets");
  cy.visitAndCheckAppMount("/datasets");
  cy.wait("@getMyDatasets");

  cy.get('[data-test="datasets-table"] .p-datatable-tbody tr', {
    timeout: Cypress.env("medium_timeout_in_ms") as number,
  })
    .first()
    .find(".data-test-company-name")
    .should("contain", companyName);

  cy.get('[data-test="datasets-table"]').get('span[data-test="qa-status"]').should("contain", status);
}

/**
 * Logs the user out without testing the url
 */
function safeLogout(): void {
  cy.intercept("**/api/companies*").as("searchRequest");
  cy.visitAndCheckAppMount("/")
    .wait("@searchRequest")
    .get("div[id='profile-picture-dropdown-toggle']")
    .click()
    .get("a[id='profile-picture-dropdown-logout-anchor']")
    .click();
}
