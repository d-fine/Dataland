import { describeIf } from "@e2e/support/TestUtility";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import {
  CompanyInformation,
  DataTypeEnum,
  EuTaxonomyDataForNonFinancials,
  CompanyAssociatedDataEuTaxonomyDataForNonFinancials,
  EuTaxonomyDataForFinancials,
  DataMetaInformation,
} from "@clients/backend";
import { FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { getBaseUrl, uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { uploadReports } from "@sharedUtils/components/UploadReports";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { CyHttpMessages } from "cypress/types/net-stubbing";
import {
  fillAndValidateEuTaxonomyForNonFinancialsUploadForm,
  submitFilledInEuTaxonomyForm,
  uploadEuTaxonomyDataForNonFinancialsViaForm,
} from "@e2e/utils/EuTaxonomyNonFinancialsUpload";
import { gotoEditFormOfMostRecentDataset } from "@e2e/utils/GeneralApiUtils";
import { TEST_PDF_FILE_NAME, TEST_PDF_FILE_PATH } from "@e2e/utils/Constants";
import { uploadDocumentViaApi } from "@e2e/utils/DocumentUpload";
import Chainable = Cypress.Chainable;

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

    let keycloakToken = "";
    let frontendDocumentHash = "";

    /**
     * Uploads a company via POST-request, then an EU Taxonomy dataset for non financial companies for the uploaded company
     * via the form in the frontend, and then visits the view page where that dataset is displayed
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
        keycloakToken = token;
        return uploadCompanyViaApi(token, generateDummyCompanyInformation(companyInformation.companyName)).then(
          (storedCompany): void => {
            cy.visitAndCheckAppMount(
              `/companies/${storedCompany.companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}/upload`
            );
            beforeFormFill();
            fillAndValidateEuTaxonomyForNonFinancialsUploadForm(false, TEST_PDF_FILE_NAME);
            afterFormFill();
            submitFilledInEuTaxonomyForm(submissionDataIntercept);
            afterDatasetSubmission(storedCompany.companyId);
          }
        );
      });
    }

    /**
     * Visits the edit page for the eu taxonomy dataset for non financial companies via navigation.
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
            uploadReports.selectFile(TEST_PDF_FILE_NAME);
            uploadReports.validateSingleFileInUploadList(TEST_PDF_FILE_NAME, "KB");
            uploadReports.fillReportCurrency(TEST_PDF_FILE_NAME);
            uploadReports.removeSingleFileFromUploadList();
            uploadReports.checkNoReportIsListed();

            uploadReports.selectFile(TEST_PDF_FILE_NAME);
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
      uploadCompanyViaApiAndEuTaxonomyDataForNonFinancialsViaForm(
        testData.companyInformation,
        () => {
          uploadReports.selectFile(TEST_PDF_FILE_NAME);
          uploadReports.selectFile(`${TEST_PDF_FILE_NAME}2`);
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
          frontendDocumentHash = data.referencedReports![TEST_PDF_FILE_NAME].reference;
          expect(TEST_PDF_FILE_NAME in data.referencedReports!).to.equal(true);
          expect(`${TEST_PDF_FILE_NAME}2` in data.referencedReports!).to.equal(true);
        },
        (companyId) => {
          validateFrontendAndBackendDocumentHashesCoincede();
          gotoEditForm(companyId, true);
          uploadReports.removeUploadedReportFromReportInfos(TEST_PDF_FILE_NAME);
          const postRequestAlias = "postData";
          cy.intercept(
            {
              method: "POST",
              url: `**/api/data/**`,
              times: 1,
            },
            (request) => {
              const data = assertDefined((request.body as CompanyAssociatedDataEuTaxonomyDataForNonFinancials).data);
              expect(TEST_PDF_FILE_NAME in data.referencedReports!).to.equal(false);
              expect(`${TEST_PDF_FILE_NAME}2` in data.referencedReports!).to.equal(true);
            }
          ).as(postRequestAlias);
          cy.get('button[data-test="submitButton"]').click();
          cy.wait(`@${postRequestAlias}`, { timeout: Cypress.env("long_timeout_in_ms") as number }).then(
            (interception) => {
              expect(interception.response?.statusCode).to.eq(200);
            }
          );
          gotoEditForm(companyId, false);
        }
      );
    });

    /**
     * Checks that the computed hash in the frontend is the same as the one returned by the documen upload endpoint
     */
    function validateFrontendAndBackendDocumentHashesCoincede(): void {
      cy.task<{ [type: string]: ArrayBuffer }>("readFile", `../${TEST_PDF_FILE_PATH}`).then(async (bufferObject) => {
        await uploadDocumentViaApi(keycloakToken, bufferObject.data, TEST_PDF_FILE_PATH).then((response) => {
          expect(frontendDocumentHash).to.equal(response.documentId);
        });
      });
    }

    it(
      "Upload EU Taxonomy Dataset via form, check that redirect to MyDatasets works and assure that it can be " +
        "viewed and edited, and that file selection, upload and download works properly",
      () => {
        getKeycloakToken(uploader_name, uploader_pw).then((token) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation("All fields filled")).then(
            (storedCompany) => {
              cy.intercept(`**/companies**`).as("getDataForMyDatasetsPage");
              uploadEuTaxonomyDataForNonFinancialsViaForm(storedCompany.companyId);
              cy.url().should("eq", getBaseUrl() + "/datasets");
              cy.wait("@getDataForMyDatasetsPage");

              cy.visitAndCheckAppMount(
                `/companies/${storedCompany.companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}`
              );

              cy.get("[data-test='companyNameTitle']").contains("All fields filled");
              checkAllDataProvided();
              clickEditButtonAndEditAndValidateChange(storedCompany.companyId).then((templateDataId) => {
                checkFileWithExistingFilenameCanNotBeResubmitted();
                checkExistingFilenameDialogDidNotBreakSubsequentUploads();
                checkThatFilesMustBeReferenced();
                checkThatFilesWithSameContentDontGetReuploaded(storedCompany.companyId, templateDataId);
                checkIfLinkedReportsAreDownloadable(storedCompany.companyId);
              });
            }
          );
        });
      }
    );

    /**
     * On the eu taxonomy for non-financial services view page, this method verifies that all data was provided
     */
    function checkAllDataProvided(): void {
      cy.contains("[data-test='taxocard']", "Eligible Revenue").should("contain", "%");
      cy.contains("[data-test='taxocard']", "Aligned Revenue").should("contain", "%");
      cy.contains("[data-test='taxocard']", "Eligible CapEx").should("contain", "%");
      cy.contains("[data-test='taxocard']", "Aligned CapEx").should("contain", "%");
      cy.contains("[data-test='taxocard']", "Eligible OpEx").should("contain", "%");
      cy.contains("[data-test='taxocard']", "Aligned OpEx").should("contain", "%");
    }

    /**
     * On the eu taxonomy for non-financial services view page, this method edits some data and validates the changes
     * @param companyId the ID of the company on whose view page this method starts on
     * @returns a chainable on the data ID of the created dataset
     */
    function clickEditButtonAndEditAndValidateChange(companyId: string): Chainable<string> {
      const newValueForEligibleRevenueAfterEdit = "30";
      cy.intercept(`**/api/data/${DataTypeEnum.EutaxonomyNonFinancials}/*`).as("getDataToPrefillForm");
      cy.get('button[data-test="editDatasetButton"]').click();
      cy.wait("@getDataToPrefillForm");
      cy.get('[data-test="pageWrapperTitle"]').should("contain", "Edit");
      cy.get(`div[data-test=revenueSection] div[data-test=eligible] input[name="value"]`)
        .clear()
        .type(newValueForEligibleRevenueAfterEdit);
      cy.get('button[data-test="submitButton"]').click();
      cy.wait("@getDataForMyDatasetsPage");
      cy.intercept(`**/api/metadata?companyId=${companyId}`).as("getMetaDataForViewPage");
      cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}`);
      cy.contains("[data-test='taxocard']", "Eligible Revenue").should(
        "contain",
        newValueForEligibleRevenueAfterEdit + "%"
      );
      return cy.wait("@getMetaDataForViewPage").then((interception) => {
        return (interception.response!.body as DataMetaInformation[]).find(
          (dataMetaInfo) => dataMetaInfo.currentlyActive
        )!.dataId;
      });
    }

    /**
     * On the eu taxonomy for non-financial services edit page, this method checks that there can not be a file uploaded
     * whose name equals the one of a file selected before
     */
    function checkFileWithExistingFilenameCanNotBeResubmitted(): void {
      cy.get('button[data-test="editDatasetButton"]').click();
      cy.wait("@getDataToPrefillForm");
      cy.get(`[data-test="${TEST_PDF_FILE_NAME}AlreadyUploadedContainer`).should("exist");
      cy.get("input[type=file]").selectFile(`../${TEST_PDF_FILE_PATH}`, { force: true });
      cy.get(".p-dialog-content").should("contain.text", "already uploaded");
      cy.get(".p-dialog-header-close").click();
      cy.get(`[data-test="${TEST_PDF_FILE_NAME}ToUploadContainer"]`).should("not.exist");
    }

    /**
     * Adds a report to upload and removes it again afterwards checking that no dialog regarding a duplicate file name
     * is wrongly triggered and that the file is correclty removed.
     */
    function checkExistingFilenameDialogDidNotBreakSubsequentUploads(): void {
      uploadReports.selectFile(`${TEST_PDF_FILE_NAME}2`);
      cy.get(".p-dialog-content").should("not.exist");
      uploadReports.removeAllFilesFromUploadList();
      uploadReports.specificReportInfoIsNotListed(`${TEST_PDF_FILE_NAME}2`);
    }

    /**
     * On the eu taxonomy for non-financial services edit page, this method checks that submission is denied
     * if a report is not referenced
     */
    function checkThatFilesMustBeReferenced(): void {
      cy.get(`button[data-test="remove-${TEST_PDF_FILE_NAME}"]`).click();
      cy.get(".p-dialog-content").should("not.exist");
      cy.get("input[type=file]").selectFile(
        {
          contents: `../${TEST_PDF_FILE_PATH}`,
          fileName: "someOtherFileName" + ".pdf",
        },
        { force: true }
      );
      uploadReports.fillAllReportInfoForms();
      cy.get('button[data-test="submitButton"]').click();
      cy.get('[data-test="failedUploadMessage"]').should("exist").should("contain.text", "someOtherFileName");
    }

    const differentFileNameForSameFile = `${TEST_PDF_FILE_NAME}FileCopy`;

    /**
     * This method verifies that there are no files with the same content uploaded twice
     * @param companyId the ID of the company whose data is to be edited
     * @param templateDataId the ID of the dataset to edit
     */
    function checkThatFilesWithSameContentDontGetReuploaded(companyId: string, templateDataId: string): void {
      cy.visitAndCheckAppMount(
        `/companies/${companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}/upload?templateDataId=${templateDataId}`
      );
      cy.wait("@getDataToPrefillForm");
      cy.get('[data-test="pageWrapperTitle"]').should("contain", "Edit");
      cy.get("input[type=file]").selectFile(
        {
          contents: `../${TEST_PDF_FILE_PATH}`,
          fileName: differentFileNameForSameFile + ".pdf",
        },
        { force: true }
      );
      uploadReports.fillAllReportInfoForms();
      cy.get(`div[data-test=capexSection] div[data-test=total] select[name="report"]`).select(
        differentFileNameForSameFile
      );
      cy.intercept(`**/documents/*/exists`).as("documentExists");
      cy.intercept(`**/documents/`, cy.spy().as("postDocument"));
      cy.intercept(`**/api/data/${DataTypeEnum.EutaxonomyNonFinancials}`).as("postCompanyAssociatedData");
      cy.get('button[data-test="submitButton"]').click();
      cy.wait("@documentExists", { timeout: Cypress.env("short_timeout_in_ms") as number })
        .its("response.body")
        .should("deep.equal", { documentExists: true });
      cy.wait("@postCompanyAssociatedData", { timeout: Cypress.env("short_timeout_in_ms") as number }).then((req) => {
        cy.log(req.response!.body as string);
      });
      cy.wait("@getDataForMyDatasetsPage");
      cy.get("@postDocument").should("not.have.been.called");
    }

    /**
     * This method verifies that uploaded reports are downloadable
     * @param companyId the ID of the company whose data to view
     */
    function checkIfLinkedReportsAreDownloadable(companyId: string): void {
      cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}`);
      const expectedPathToDownloadedReport = Cypress.config("downloadsFolder") + `/${TEST_PDF_FILE_NAME}.pdf`;
      const downloadLinkSelector = `span[data-test="Report-Download-${differentFileNameForSameFile}"]`;
      cy.readFile(expectedPathToDownloadedReport).should("not.exist");
      cy.get(downloadLinkSelector)
        .click()
        .then(() => {
          cy.readFile(`../${TEST_PDF_FILE_PATH}`, "binary", {
            timeout: Cypress.env("medium_timeout_in_ms") as number,
          }).then((expectedPdfBinary) => {
            cy.task("calculateHash", expectedPdfBinary).then((expectedPdfHash) => {
              cy.readFile(expectedPathToDownloadedReport, "binary", {
                timeout: Cypress.env("medium_timeout_in_ms") as number,
              }).then((receivedPdfHash) => {
                cy.task("calculateHash", receivedPdfHash).should("eq", expectedPdfHash);
              });
              cy.task("deleteFolder", Cypress.config("downloadsFolder"));
            });
          });
        });
    }
  }
);
