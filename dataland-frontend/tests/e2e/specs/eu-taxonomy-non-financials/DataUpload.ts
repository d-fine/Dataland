import { assertDefined } from "@/utils/TypeScriptUtils";
import {
  type CompanyAssociatedDataEuTaxonomyDataForNonFinancials,
  type DataMetaInformation,
  DataTypeEnum,
  type EuTaxonomyDataForNonFinancials,
} from "@clients/backend";
import { describeIf } from "@e2e/support/TestUtility";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { TEST_PDF_FILE_NAME, TEST_PDF_FILE_PATH } from "@sharedUtils/ConstantsForPdfs";
import { admin_name, admin_pw } from "@e2e/utils/Cypress";
import { uploadDocumentViaApi } from "@e2e/utils/DocumentUpload";
import { fillAndValidateEuTaxonomyForNonFinancialsUploadForm } from "@e2e/utils/EuTaxonomyNonFinancialsUpload";
import { goToEditFormOfMostRecentDatasetForCompanyAndFramework } from "@e2e/utils/GeneralUtils";
import { type FixtureData } from "@sharedUtils/Fixtures";
import { uploadDocuments } from "@sharedUtils/components/UploadDocuments";

describeIf(
  "As a user, I expect that the upload form works correctly when editing and uploading a new eu-taxonomy dataset for a non-financial company",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
  },
  function () {
    let frontendDocumentHash = "";
    let testData: FixtureData<EuTaxonomyDataForNonFinancials>;
    before(function () {
      cy.fixture("CompanyInformationWithEuTaxonomyDataForNonFinancials").then(function (
        jsonContent: FixtureData<EuTaxonomyDataForNonFinancials>[],
      ) {
        testData = jsonContent[0];
      });
    });

    /**
     * Visits the edit page for the eu taxonomy dataset for non financial companies via navigation and then checks
     * if already uploaded reports do exist in the form.
     * @param companyId the id of the company for which to edit a dataset
     * @param isPdfTestFileExpected specifies if the test file is expected to be in the server response
     */
    function goToEditFormAndValidateExistenceOfReports(companyId: string, isPdfTestFileExpected: boolean): void {
      goToEditFormOfMostRecentDatasetForCompanyAndFramework(companyId, DataTypeEnum.EutaxonomyNonFinancials).then(
        (interceptionOfGetDataRequestForEditMode) => {
          const referencedReports = assertDefined(
            (
              interceptionOfGetDataRequestForEditMode?.response
                ?.body as CompanyAssociatedDataEuTaxonomyDataForNonFinancials
            )?.data.general?.referencedReports,
          );
          expect(TEST_PDF_FILE_NAME in referencedReports).to.equal(isPdfTestFileExpected);
          expect(`${TEST_PDF_FILE_NAME}2` in referencedReports).to.equal(true);
        },
      );
    }

    /**
     * Checks that the computed hash in the frontend is the same as the one returned by the document upload endpoint
     * @param keycloakToken token given by keycloak after logging in
     * @param frontendDocumentHash calculated hash of the document
     */
    function validateFrontendAndBackendDocumentHashesCoincede(
      keycloakToken: string,
      frontendDocumentHash: string,
    ): void {
      cy.task<{ [type: string]: ArrayBuffer }>("readFile", `../${TEST_PDF_FILE_PATH}`).then(async (bufferObject) => {
        await uploadDocumentViaApi(keycloakToken, bufferObject.data, TEST_PDF_FILE_PATH).then((response) => {
          expect(frontendDocumentHash).to.equal(response.documentId);
        });
      });
    }

    /**
     * This method verifies that there are no files with the same content uploaded twice
     * @param companyId the ID of the company whose data is to be edited
     * @param templateDataId the ID of the dataset to edit
     */
    function checkThatFilesWithSameContentDontGetReuploaded(companyId: string, templateDataId: string): void {
      const differentFileNameForSameFile = `${TEST_PDF_FILE_NAME}FileCopy`;
      cy.intercept({
        method: "GET",
        url: "**/api/data/**",
        times: 1,
      }).as("getDataToPrefillForm");
      cy.visitAndCheckAppMount(
        `/companies/${companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}/upload?templateDataId=${templateDataId}`,
      );
      cy.wait("@getDataToPrefillForm");
      cy.get('[data-test="pageWrapperTitle"]').should("contain", "Edit");
      cy.get("input[type=file]").selectFile(
        { contents: `../${TEST_PDF_FILE_PATH}`, fileName: differentFileNameForSameFile + ".pdf" },
        { force: true },
      );
      uploadDocuments.fillAllFormsOfReportsSelectedForUpload();
      cy.get('div[name="capex"]').within(() => {
        cy.get('select[name="fileName"]').select(differentFileNameForSameFile);
      });
      cy.get('div[name="opex"]').within(() => {
        cy.get('select[name="fileName"]').select(`${TEST_PDF_FILE_NAME}2`);
      });
      cy.intercept({ url: `**/documents/*`, method: "HEAD", times: 1 }).as("documentExists");
      cy.intercept(`**/documents/`, cy.spy().as("postDocument"));
      cy.intercept(`**/api/data/${DataTypeEnum.EutaxonomyNonFinancials}`).as("postCompanyAssociatedData");
      cy.intercept(`**/companies**`).as("getDataForMyDatasetsPage");
      cy.get('button[data-test="submitButton"]').click();
      cy.wait("@documentExists", { timeout: Cypress.env("short_timeout_in_ms") as number })
        .its("response.statusCode")
        .should("equal", 200);
      cy.wait("@postCompanyAssociatedData", { timeout: Cypress.env("short_timeout_in_ms") as number });
      cy.wait("@getDataForMyDatasetsPage");
      cy.get("@postDocument").should("not.have.been.called");
    }

    it(
      "Check if the file upload info remove button works as expected, make sure the file content hashes" +
        "generated by frontend and backend are the same and that the exact document does not get reuploaded a second time",
      () => {
        getKeycloakToken(admin_name, admin_pw).then((token: string) => {
          const dummyCompanyInformation = generateDummyCompanyInformation(testData.companyInformation.companyName);
          return uploadCompanyViaApi(token, dummyCompanyInformation).then((storedCompany) => {
            cy.ensureLoggedIn(admin_name, admin_pw);
            cy.visitAndCheckAppMount(
              `/companies/${storedCompany.companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}/upload`,
            );
            uploadDocuments.selectFile(TEST_PDF_FILE_NAME, "referencedReports");
            uploadDocuments.selectFile(`${TEST_PDF_FILE_NAME}2`, "referencedReports");
            uploadDocuments.fillAllFormsOfReportsSelectedForUpload(2);
            fillAndValidateEuTaxonomyForNonFinancialsUploadForm(`${TEST_PDF_FILE_NAME}2`);
            cy.get('div[name="revenue"]').within(() => {
              cy.get('select[name="fileName"]').select(TEST_PDF_FILE_NAME);
            });
            cy.get('div[name="capex"]').within(() => {
              cy.get('select[name="fileName"]').select(`${TEST_PDF_FILE_NAME}2`);
            });
            cy.intercept({ method: "POST", url: `**/api/data/**`, times: 1 }, (request) => {
              const data = assertDefined(request.body as CompanyAssociatedDataEuTaxonomyDataForNonFinancials).data;
              expect(TEST_PDF_FILE_NAME in assertDefined(data.general?.referencedReports)).to.equal(true);
              expect(`${TEST_PDF_FILE_NAME}2` in assertDefined(data.general?.referencedReports)).to.equal(true);
              frontendDocumentHash = assertDefined(data.general?.referencedReports)[TEST_PDF_FILE_NAME].fileReference;
            }).as("submittedData");
            cy.get('button[data-test="submitButton"]').click();

            cy.wait(`@submittedData`, { timeout: Cypress.env("long_timeout_in_ms") as number }).then(() => {
              validateFrontendAndBackendDocumentHashesCoincede(token, frontendDocumentHash);
            });
            cy.contains("span", "MY DATASETS");
            goToEditFormAndValidateExistenceOfReports(storedCompany.companyId, true);
            uploadDocuments.removeAlreadyUploadedReport(TEST_PDF_FILE_NAME);
            cy.intercept({ method: "POST", url: `**/api/data/**`, times: 1 }, (request) => {
              const data = assertDefined(request.body as CompanyAssociatedDataEuTaxonomyDataForNonFinancials).data;
              expect(TEST_PDF_FILE_NAME in assertDefined(data.general?.referencedReports)).to.equal(false);
              expect(`${TEST_PDF_FILE_NAME}2` in assertDefined(data.general?.referencedReports)).to.equal(true);
            }).as("submitEditData");
            cy.get('button[data-test="submitButton"]').click();
            cy.wait(`@submitEditData`, { timeout: Cypress.env("long_timeout_in_ms") as number }).then(
              (interception) => {
                expect(interception.response?.statusCode).to.eq(200);
                goToEditFormAndValidateExistenceOfReports(storedCompany.companyId, false);
                const metaDataOfReuploadedDataset = assertDefined(interception.response?.body) as DataMetaInformation;
                checkThatFilesWithSameContentDontGetReuploaded(
                  storedCompany.companyId,
                  metaDataOfReuploadedDataset.dataId,
                );
              },
            );
          });
        });
      },
    );
  },
);
