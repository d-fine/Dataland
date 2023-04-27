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
import { fillEuTaxonomyForNonFinancialsUploadForm } from "@e2e/utils/EuTaxonomyNonFinancialsUpload";

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
            fillEuTaxonomyForNonFinancialsUploadForm(false, "pdfTest");
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
     * @param expectIncludedFile specifies if the file pdfTest.pdf is expected to be in the server response
     */
    function gotoEditForm(companyId: string, expectIncludedFile: boolean): void {
      const getRequestAlias = "getData";
      cy.intercept("GET", "**/api/data/**").as(getRequestAlias);
      cy.visit(`/companies/${companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}`);
      cy.wait(`@${getRequestAlias}`, { timeout: 30000 });
      cy.get('[data-test="editDatasetButton"]').click();
      cy.wait(`@${getRequestAlias}`, { timeout: 30000 }).then((interception) => {
        console.log(interception);
        const data = assertDefined(
          (interception.response?.body as CompanyAssociatedDataEuTaxonomyDataForNonFinancials)?.data
        );
        expect("pdfTest" in data.referencedReports!).to.equal(expectIncludedFile);
        expect("pdfTest2" in data.referencedReports!).to.equal(true);
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
      testData.companyInformation.companyName = "non-financials-upload-form-remove-document-button";
      let areBothDocumentsStillUploaded = true;
      uploadCompanyViaApiAndEuTaxonomyDataForNonFinancialsViaForm(
        testData.companyInformation,
        () => {
          uploadReports.uploadFile("pdfTest");
          uploadReports.uploadFile("pdfTest2");
        },
        () => {
          uploadReports.fillAllReportInfoForms();
          cy.get(`[data-test="capexSection"] [data-test="total"] select[name="report"]`).select("pdfTest");
          cy.get(`[data-test="opexSection"] [data-test="total"] select[name="report"]`).select("pdfTest2");
        },
        (request) => {
          const data = assertDefined((request.body as CompanyAssociatedDataEuTaxonomyDataForNonFinancials).data);
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
