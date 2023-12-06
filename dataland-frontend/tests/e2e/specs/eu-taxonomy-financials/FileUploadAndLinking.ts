import { describeIf } from "@e2e/support/TestUtility";
import { checkIfLinkedReportsAreDownloadable, gotoEditForm } from "@e2e/utils/EuTaxonomyFinancialsUpload";
import {
  type EuTaxonomyDataForFinancials,
  type CompanyAssociatedDataEuTaxonomyDataForFinancials,
  DataTypeEnum,
} from "@clients/backend";
import { type FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { admin_name, admin_pw } from "@e2e/utils/Cypress";
import { UploadDocuments } from "@sharedUtils/components/UploadDocuments";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { TEST_PDF_FILE_NAME } from "@sharedUtils/ConstantsForPdfs";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { uploadFrameworkData } from "@e2e/utils/FrameworkUpload";

describeIf(
  "As a user, I want to add and link documents to the EU Taxonomy form",

  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
  },
  function () {
    let euTaxoFinancialsFixture: FixtureData<EuTaxonomyDataForFinancials>;
    const uploadDocuments = new UploadDocuments();

    before(function () {
      cy.fixture("CompanyInformationWithEuTaxonomyDataForFinancialsPreparedFixtures").then(function (jsonContent) {
        const preparedFixtures = jsonContent as Array<FixtureData<EuTaxonomyDataForFinancials>>;
        euTaxoFinancialsFixture = getPreparedFixture("company-for-all-types", preparedFixtures);
      });
    });

    it("Check if the files upload works as expected", () => {
      euTaxoFinancialsFixture.companyInformation.companyName =
        "financials-upload-form-document-upload-test" + Date.now();
      let areBothDocumentsStillUploaded = true;
      let storedCompanyId: string;
      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        return uploadCompanyViaApi(
          token,
          generateDummyCompanyInformation(euTaxoFinancialsFixture.companyInformation.companyName),
        ).then((storedCompany) => {
          storedCompanyId = storedCompany.companyId;
          return uploadFrameworkData(
            DataTypeEnum.EutaxonomyFinancials,
            token,
            storedCompanyId,
            "2023",
            euTaxoFinancialsFixture.t,
            true,
          ).then((dataMetaInformation) => {
            cy.ensureLoggedIn(admin_name, admin_pw);
            cy.intercept(`**/api/data/${DataTypeEnum.EutaxonomyFinancials}/${dataMetaInformation.dataId}`).as(
              "fetchDataForPrefill",
            );
            cy.visitAndCheckAppMount(
              "/companies/" +
                storedCompanyId +
                "/frameworks/" +
                DataTypeEnum.EutaxonomyFinancials +
                "/upload?templateDataId=" +
                dataMetaInformation.dataId,
            );
            cy.wait("@fetchDataForPrefill", {
              timeout: Cypress.env("medium_timeout_in_ms") as number,
            });
            cy.get("h1").should("contain", euTaxoFinancialsFixture.companyInformation.companyName);

            uploadDocuments.selectFile(TEST_PDF_FILE_NAME);
            uploadDocuments.validateReportToUploadHasContainerInTheFileSelector(TEST_PDF_FILE_NAME);
            uploadDocuments.validateReportToUploadHasContainerWithInfoForm(TEST_PDF_FILE_NAME);

            uploadDocuments.selectFile(`${TEST_PDF_FILE_NAME}2`);
            uploadDocuments.validateReportToUploadHasContainerInTheFileSelector(`${TEST_PDF_FILE_NAME}2`);
            uploadDocuments.validateReportToUploadHasContainerWithInfoForm(`${TEST_PDF_FILE_NAME}2`);

            uploadDocuments.fillAllFormsOfReportsSelectedForUpload(2);
            cy.get(`[data-test="assetManagementKpis"]`)
              .find(`[data-test="banksAndIssuersInPercent"]`)
              .find('select[name="fileName"]')
              .select(2);

            cy.get(`[data-test="assetManagementKpis"]`)
              .find(`[data-test="investmentNonNfrdInPercent"]`)
              .find('select[name="fileName"]')
              .select(3);

            cy.intercept(
              {
                method: "POST",
                url: `**/api/data/**`,
                times: 1,
              },
              (request) => {
                const data = assertDefined((request.body as CompanyAssociatedDataEuTaxonomyDataForFinancials).data);
                expect(TEST_PDF_FILE_NAME in assertDefined(data.referencedReports)).to.equal(
                  areBothDocumentsStillUploaded,
                );
                expect(`${TEST_PDF_FILE_NAME}2` in assertDefined(data.referencedReports)).to.equal(true);
              },
            ).as("postDataWithTwoReports");
            cy.get('button[data-test="submitButton"]').click();
            cy.wait("@postDataWithTwoReports", { timeout: Cypress.env("short_timeout_in_ms") as number }).then(
              (interception) => {
                expect(interception.response?.statusCode).to.eq(200);
              },
            );
            cy.get('[data-test="datasets-table"]').should("be.visible");
            checkIfLinkedReportsAreDownloadable(storedCompanyId);
            gotoEditForm(storedCompanyId, true);
            uploadDocuments.selectMultipleFilesAtOnce([TEST_PDF_FILE_NAME, `${TEST_PDF_FILE_NAME}2`]);
            cy.get(".p-dialog.p-component").should("exist").get('[data-pc-section="closebutton"]').click();
            cy.get(".p-dialog.p-component").should("not.exist");

            uploadDocuments.removeAlreadyUploadedReport(TEST_PDF_FILE_NAME).then(() => {
              areBothDocumentsStillUploaded = false;
            });

            cy.intercept(
              {
                method: "POST",
                url: `**/api/data/**`,
                times: 1,
              },
              (request) => {
                const data = assertDefined((request.body as CompanyAssociatedDataEuTaxonomyDataForFinancials).data);
                expect(TEST_PDF_FILE_NAME in assertDefined(data.referencedReports)).to.equal(
                  areBothDocumentsStillUploaded,
                );
                expect(`${TEST_PDF_FILE_NAME}2` in assertDefined(data.referencedReports)).to.equal(true);
              },
            ).as("postDataWithOneReport");
            cy.get('button[data-test="submitButton"]').click();
            cy.wait("@postDataWithOneReport", { timeout: Cypress.env("short_timeout_in_ms") as number }).then(
              (interception) => {
                expect(interception.response?.statusCode).to.eq(200);
              },
            );
            cy.get('[data-test="datasets-table"]').should("be.visible");
            gotoEditForm(storedCompanyId, false);
          });
        });
      });
    });
  },
);
