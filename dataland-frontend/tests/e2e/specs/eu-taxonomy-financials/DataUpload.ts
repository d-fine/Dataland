import { describeIf } from "@e2e/support/TestUtility";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { fillEuTaxonomyForFinancialsUploadForm } from "@e2e/utils/EuTaxonomyFinancialsUpload";
import {
  CompanyInformation,
  EuTaxonomyDataForFinancials,
  DataTypeEnum,
  CompanyAssociatedDataEuTaxonomyDataForFinancials,
} from "@clients/backend";
import { FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { uploadReports } from "@sharedUtils/components/UploadReports";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { CyHttpMessages } from "cypress/types/net-stubbing";

describeIf(
  "As a user, I expect that the upload form works correctly when editing and uploading a new dataset",
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

    /**
     * Uploads a company via POST-request, then an EU Taxonomy dataset for financial companies for the uploaded company
     * via the form in the frontend, and then visits the view page where that dataset is displayed
     *
     * @param companyInformation Company information to be used for the company upload
     * @param testData EU Taxonomy dataset for financial companies to be uploaded
     * @param beforeFormFill is performed before filling the fields of the upload form
     * @param afterFormFill is performed after filling the fields of the upload form
     * @param submissionDataIntercept performs checks on the request itself
     * @param afterDatasetSubmission is performed after the data has been submitted
     */
    function uploadCompanyViaApiAndEuTaxonomyDataForFinancialsViaForm(
      companyInformation: CompanyInformation,
      testData: EuTaxonomyDataForFinancials,
      beforeFormFill: () => void,
      afterFormFill: () => void,
      submissionDataIntercept: (request: CyHttpMessages.IncomingHttpRequest) => void,
      afterDatasetSubmission: (companyId: string) => void
    ): void {
      getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
        return uploadCompanyViaApi(token, generateDummyCompanyInformation(companyInformation.companyName)).then(
          (storedCompany): void => {
            cy.ensureLoggedIn(uploader_name, uploader_pw);
            cy.visitAndCheckAppMount(
              `/companies/${storedCompany.companyId}/frameworks/${DataTypeEnum.EutaxonomyFinancials}/upload`
            );
            beforeFormFill();
            fillEuTaxonomyForFinancialsUploadForm(testData);
            afterFormFill();
            cy.intercept("POST", `**/api/data/**`, submissionDataIntercept).as(postRequestAlias);
            cy.get('button[data-test="submitButton"]').click();
            cy.wait(`@${postRequestAlias}`, { timeout: 100000 }).then((interception) => {
              expect(interception.response?.statusCode).to.eq(200);
            });
            afterDatasetSubmission(storedCompany.companyId);
          }
        );
      });
    }

    const postRequestAlias = "postData";

    /**
     * Visits the edit page for the eu taxonomy dataset for financial companies via navigation.
     *
     * @param companyId the id of the company for which to edit a dataset
     * @param expectIncludedFile specifies if the file pdfTest.pdf is expected to be in the server response
     */
    function gotoEditForm(companyId: string, expectIncludedFile: boolean): void {
      const getRequestAlias = "getData";
      cy.intercept("GET", "**/api/data/**").as(getRequestAlias);
      cy.visit(`/companies/${companyId}/frameworks/${DataTypeEnum.EutaxonomyFinancials}`);
      cy.wait(`@${getRequestAlias}`, { timeout: 30000 });
      cy.get('[data-test="editDatasetButton"]').click();
      cy.wait(`@${getRequestAlias}`, { timeout: 30000 })
        .its("response")
        .then((response) => {
          const data = assertDefined((response?.body as CompanyAssociatedDataEuTaxonomyDataForFinancials)?.data);
          expect("pdfTest" in data.referencedReports!).to.equal(expectIncludedFile);
          expect("pdfTest2" in data.referencedReports!).to.equal(true);
        });
    }

    it(
      "Create an Eu Taxonomy Financial dataset via upload form with all financial company types selected to assure " +
        "that the upload form works fine with all options",
      () => {
        testData.companyInformation.companyName = "financials-upload-form";
        uploadCompanyViaApiAndEuTaxonomyDataForFinancialsViaForm(
          testData.companyInformation,
          testData.t,
          () => {
            const filename = "pdfTest";
            uploadReports.uploadFile(filename);
            uploadReports.validateSingleFileInUploadedList(filename, "KB");
            uploadReports.validateFileInfo(filename);
            uploadReports.removeSingleUploadedFileFromUploadedList();
            uploadReports.checkNoReportIsListed();
          },
          () => undefined,
          () => undefined,
          () => undefined
        );
      }
    );

    it("Check if the file upload info remove button works as expected", () => {
      testData.companyInformation.companyName = "financials-upload-form-remove-document-button";
      let areBothDocumentsStillUploaded = true;
      uploadCompanyViaApiAndEuTaxonomyDataForFinancialsViaForm(
        testData.companyInformation,
        testData.t,
        () => undefined,
        () => {
          uploadReports.uploadFile("pdfTest");
          uploadReports.uploadFile("pdfTest2");
          uploadReports.fillAllReportInfoForms();
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
          expect("pdfTest" in data.referencedReports!).to.equal(areBothDocumentsStillUploaded);
          expect("pdfTest2" in data.referencedReports!).to.equal(true);
        },
        (companyId) => {
          gotoEditForm(companyId, true);
          uploadReports.removeUploadedReportFromReportInfos("pdfTest").then(() => {
            areBothDocumentsStillUploaded = false;
          });
          cy.get('button[data-test="submitButton"]').click();
          cy.wait(`@${postRequestAlias}`, { timeout: 100000 }).then((interception) => {
            expect(interception.response?.statusCode).to.eq(200);
          });
          gotoEditForm(companyId, false);
        }
      );
    });
  }
);
