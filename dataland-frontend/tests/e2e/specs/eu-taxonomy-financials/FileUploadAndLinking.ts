import { describeIf } from "@e2e/support/TestUtility";
import {
  gotoEditForm,
  uploadCompanyViaApiAndEuTaxonomyDataForFinancialsViaForm,
} from "@e2e/utils/EuTaxonomyFinancialsUpload";
import { EuTaxonomyDataForFinancials, CompanyAssociatedDataEuTaxonomyDataForFinancials } from "@clients/backend";
import { FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { uploadDocuments } from "@sharedUtils/components/UploadDocuments";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { TEST_PDF_FILE_NAME } from "@e2e/utils/Constants";

describeIf(
  "As a user, I want to add and link documents to the EU Taxonomy form",

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

    it("Check if the file upload info remove button works as expected", () => {
      testData.companyInformation.companyName = "financials-upload-form-remove-document-button";
      let areBothDocumentsStillUploaded = true;
      uploadCompanyViaApiAndEuTaxonomyDataForFinancialsViaForm(
        testData.companyInformation,
        testData.t,
        () => undefined,
        () => {
          uploadDocuments.selectFile(TEST_PDF_FILE_NAME);
          uploadDocuments.validateReportToUploadIsListed(TEST_PDF_FILE_NAME);
          cy.get(`[data-test="${TEST_PDF_FILE_NAME}ToUploadContainer"]`).should("exist");
          uploadDocuments.removeReportToUpload(TEST_PDF_FILE_NAME);
          cy.get(`[data-test="${TEST_PDF_FILE_NAME}ToUploadContainer"]`).should("not.exist");
          uploadDocuments.checkNoReportIsListed();
          uploadDocuments.selectFile(TEST_PDF_FILE_NAME);
          cy.get(`[data-test="${TEST_PDF_FILE_NAME}ToUploadContainer"]`).should("exist");
          uploadDocuments.validateReportToUploadIsListed(TEST_PDF_FILE_NAME);
          uploadDocuments.selectFile(`${TEST_PDF_FILE_NAME}2`);
          cy.get(`[data-test="${TEST_PDF_FILE_NAME}ToUploadContainer"]`).should("exist");
          uploadDocuments.validateReportToUploadIsListed(`${TEST_PDF_FILE_NAME}2`);
          uploadDocuments.fillAllReportsToUploadForms(2);
          cy.get(`[data-test="assetManagementKpis"]`)
            .find(`[data-test="banksAndIssuers"]`)
            .find('select[name="report"]')
            .select(2);
          cy.get(`[data-test="assetManagementKpis"]`)
            .find(`[data-test="investmentNonNfrd"]`)
            .find('select[name="report"]')
            .select(3);
        },
        (request) => {
          const data = assertDefined((request.body as CompanyAssociatedDataEuTaxonomyDataForFinancials).data);
          expect(TEST_PDF_FILE_NAME in assertDefined(data.referencedReports)).to.equal(areBothDocumentsStillUploaded);
          expect(`${TEST_PDF_FILE_NAME}2` in assertDefined(data.referencedReports)).to.equal(true);
        },
        (companyId) => {
          gotoEditForm(companyId, true);
          uploadDocuments.removeUploadedReport(TEST_PDF_FILE_NAME).then(() => {
            areBothDocumentsStillUploaded = false;
          });
          const postRequestAlias = "postData";
          cy.intercept(
            {
              method: "POST",
              url: `**/api/data/**`,
              times: 1,
            },
            (request) => {
              const data = assertDefined((request.body as CompanyAssociatedDataEuTaxonomyDataForFinancials).data);
              expect(TEST_PDF_FILE_NAME in assertDefined(data.referencedReports)).to.equal(
                areBothDocumentsStillUploaded
              );
              expect(`${TEST_PDF_FILE_NAME}2` in assertDefined(data.referencedReports)).to.equal(true);
            }
          ).as(postRequestAlias);
          cy.get('button[data-test="submitButton"]').click();
          cy.wait(`@${postRequestAlias}`, { timeout: Cypress.env("short_timeout_in_ms") as number }).then(
            (interception) => {
              expect(interception.response?.statusCode).to.eq(200);
            }
          );
          gotoEditForm(companyId, false);
        }
      );
    });
  }
);
