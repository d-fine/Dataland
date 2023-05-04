import { describeIf } from "@e2e/support/TestUtility";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import {
  CompanyInformation,
  DataTypeEnum,
  EuTaxonomyDataForNonFinancials,
  CompanyAssociatedDataEuTaxonomyDataForNonFinancials,
  EuTaxonomyDataForFinancials,
} from "@clients/backend";
import { FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { uploadReports } from "@sharedUtils/components/UploadReports";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { CyHttpMessages } from "cypress/types/net-stubbing";
import { fillAndValidateEuTaxonomyForNonFinancialsUploadForm } from "@e2e/utils/EuTaxonomyNonFinancialsUpload";
import { gotoEditFormOfMostRecentDataset } from "@e2e/utils/GeneralApiUtils";
import { TEST_PDF_FILE_NAME } from "@e2e/utils/Constants";

describeIf(
  "As a user, I expect that the upload form works correctly when editing and uploading a new eu-taxonomy dataset for a non-financial company",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
    dataEnvironments: ["fakeFixtures"],
  },
  function () {
    beforeEach(() => {
      cy.ensureLoggedIn(uploader_name, uploader_pw);
    });

    let testData: FixtureData<EuTaxonomyDataForNonFinancials>;

    before(function () {
      cy.fixture("CompanyInformationWithEuTaxonomyDataForFinancialsPreparedFixtures").then(function (jsonContent) {
        const preparedFixtures = jsonContent as Array<FixtureData<EuTaxonomyDataForFinancials>>;
        testData = getPreparedFixture("company-for-all-types", preparedFixtures);
      });
    });

    /**
     * Uploads a company via POST-request, then an EU Taxonomy dataset for non financial companies for the uploaded company
     * via the form in the frontend, and then visits the view page where that dataset is displayed
     *
     * @param companyInformation Company information to be used for the company upload
     * @param beforeFormFill is performed before filling the fields of the upload form
     * @param afterFormFill is performed after filling the fields of the upload form
     * @param submissionDataIntercept performs checks on the request itself
     * @param afterDatasetSubmission is performed after the data has been submitted
     */
    function uploadCompanyViaApiAndEuTaxonomyDataForNonFinancialsViaForm(
      companyInformation: CompanyInformation,
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
              `/companies/${storedCompany.companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}/upload`
            );
            beforeFormFill();
            fillAndValidateEuTaxonomyForNonFinancialsUploadForm(false, TEST_PDF_FILE_NAME);
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
     * Visits the edit page for the eu taxonomy dataset for non financial companies via navigation.
     *
     * @param companyId the id of the company for which to edit a dataset
     * @param expectIncludedFile specifies if the test file is expected to be in the server response
     */
    function gotoEditForm(companyId: string, expectIncludedFile: boolean): void {
      gotoEditFormOfMostRecentDataset(companyId, DataTypeEnum.EutaxonomyNonFinancials).then((interception) => {
        const referencedReports = assertDefined(
          (interception?.response?.body as CompanyAssociatedDataEuTaxonomyDataForNonFinancials)?.data?.referencedReports
        );
        expect(TEST_PDF_FILE_NAME in referencedReports).to.equal(expectIncludedFile);
        expect(`${TEST_PDF_FILE_NAME}2` in referencedReports).to.equal(true);
      });
    }

    it(
      "Create an Eu Taxonomy Non Financial dataset via upload form with all non financial company types selected to assure " +
        "that the upload form works fine with all options",
      () => {
        testData.companyInformation.companyName = "non-financials-upload-form";
        uploadCompanyViaApiAndEuTaxonomyDataForNonFinancialsViaForm(
          testData.companyInformation,
          () => {
            uploadReports.uploadFile(TEST_PDF_FILE_NAME);
            uploadReports.validateSingleFileInUploadedList(TEST_PDF_FILE_NAME, "KB");
            uploadReports.fillReportCurrency(TEST_PDF_FILE_NAME);
            uploadReports.removeSingleUploadedFileFromUploadedList();
            uploadReports.checkNoReportIsListed();

            uploadReports.uploadFile(TEST_PDF_FILE_NAME);
            uploadReports.fillAllReportInfoForms();
          },
          () => undefined,
          () => undefined,
          () => undefined
        );
      }
    );

    it("Check if the file upload info remove button works as expected", () => {
      testData.companyInformation.companyName = "non-financials-upload-form-remove-document-button";
      let areBothDocumentsStillUploaded = true;
      uploadCompanyViaApiAndEuTaxonomyDataForNonFinancialsViaForm(
        testData.companyInformation,
        () => {
          uploadReports.uploadFile(TEST_PDF_FILE_NAME);
          uploadReports.uploadFile(`${TEST_PDF_FILE_NAME}2`);
          uploadReports.fillAllReportInfoForms();
        },
        () => {
          cy.get(`[data-test="capexSection"] [data-test="total"] select[name="report"]`).select(TEST_PDF_FILE_NAME);
          cy.get(`[data-test="opexSection"] [data-test="total"] select[name="report"]`).select(
            `${TEST_PDF_FILE_NAME}2`
          );
        },
        (request) => {
          const data = assertDefined((request.body as CompanyAssociatedDataEuTaxonomyDataForNonFinancials).data);
          expect(TEST_PDF_FILE_NAME in data.referencedReports!).to.equal(areBothDocumentsStillUploaded);
          expect(`${TEST_PDF_FILE_NAME}2` in data.referencedReports!).to.equal(true);
        },
        (companyId) => {
          gotoEditForm(companyId, true);
          uploadReports.removeUploadedReportFromReportInfos(TEST_PDF_FILE_NAME).then(() => {
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
