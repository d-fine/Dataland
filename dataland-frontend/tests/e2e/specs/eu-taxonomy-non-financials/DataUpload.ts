import { assertDefined } from '@/utils/TypeScriptUtils';
import {
  type CompanyAssociatedDataEutaxonomyNonFinancialsData,
  type DataMetaInformation,
  DataTypeEnum,
} from '@clients/backend';
import { describeIf } from '@e2e/support/TestUtility';
import { getKeycloakToken } from '@e2e/utils/Auth';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { TEST_PDF_FILE_NAME, TEST_PDF_FILE_PATH } from '@sharedUtils/ConstantsForPdfs';
import { admin_name, admin_pw, getBaseUrl } from '@e2e/utils/Cypress';
import { uploadDocumentViaApi } from '@e2e/utils/DocumentUpload';
import { goToEditFormOfMostRecentDatasetForCompanyAndFramework } from '@e2e/utils/GeneralUtils';
import { assignCompanyOwnershipToDatalandAdmin } from '@e2e/utils/CompanyRolesUtils';
import { UploadReports } from '@sharedUtils/components/UploadReports';
import { selectItemFromDropdownByIndex, selectItemFromDropdownByValue } from '@sharedUtils/Dropdown';

describeIf(
  'As a user, I expect that the upload form works correctly when editing and uploading a new eu-taxonomy dataset for a non-financial company',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  function () {
    let frontendDocumentHash = '';
    const uploadReports = new UploadReports('referencedReports');
    before(() => {
      Cypress.env('excludeBypassQaIntercept', true);
    });

    /**
     * Fills all the required fields of the eu-taxonomy upload form for non-financial companies to enable submit button
     */
    function fillRequiredEutaxonomyNonFinancialsFields(): void {
      cy.get(
        'div[data-test="fiscalYearEnd"] div[data-test="toggleDataPointWrapper"] div[data-test="dataPointToggleButton"]'
      )
        .should('exist')
        .click();
      cy.get('[data-test="fiscalYearEnd"] button').should('have.class', 'p-datepicker-trigger').click();
      cy.get('div.p-datepicker').find('button[aria-label="Next Month"]').first().click();
      cy.get('div.p-datepicker').find('span:contains("11")').click();
      selectItemFromDropdownByIndex(cy.get('div[name="value"]'), 1);
      cy.get('input[name="provider"]').type('Some Assurance Provider Company');
    }

    /**
     * Visits the edit page for the eu taxonomy dataset for non financial companies via navigation and then checks
     * if already uploaded reports do exist in the form.
     * @param companyId the id of the company for which to edit a dataset
     * @param isPdfTestFileExpected specifies if the test file is expected to be in the server response
     */
    function goToEditFormAndValidateExistenceOfReports(companyId: string, isPdfTestFileExpected: boolean): void {
      goToEditFormOfMostRecentDatasetForCompanyAndFramework(companyId, DataTypeEnum.EutaxonomyNonFinancials).then(
        (interceptionOfGetDataRequestForEditMode) => {
          const referencedReportsInDataset = assertDefined(
            (
              interceptionOfGetDataRequestForEditMode?.response
                ?.body as CompanyAssociatedDataEutaxonomyNonFinancialsData
            ).data.general?.referencedReports
          );
          expect(TEST_PDF_FILE_NAME in referencedReportsInDataset).to.equal(isPdfTestFileExpected);
          expect(`${TEST_PDF_FILE_NAME}2` in referencedReportsInDataset).to.equal(true);
        }
      );
    }

    /**
     * Checks that the computed hash in the frontend is the same as the one returned by the document upload endpoint
     * @param keycloakToken token given by keycloak after logging in
     * @param frontendDocumentHash calculated hash of the document
     */
    function validateFrontendAndBackendDocumentHashesCoincide(
      keycloakToken: string,
      frontendDocumentHash: string
    ): void {
      cy.task<{ [type: string]: ArrayBuffer }>('readFile', `../${TEST_PDF_FILE_PATH}`).then(async (bufferObject) => {
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
        method: 'GET',
        url: '**/api/data/**',
        times: 1,
      }).as('getDataToPrefillForm');
      cy.visitAndCheckAppMount(
        `/companies/${companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}/upload?templateDataId=${templateDataId}`
      );
      cy.wait('@getDataToPrefillForm', { timeout: Cypress.env('short_timeout_in_ms') as number });
      cy.get('[data-test="pageWrapperTitle"]').should('contain', 'Edit');
      cy.get('input[type=file]').selectFile(
        { contents: `../${TEST_PDF_FILE_PATH}`, fileName: differentFileNameForSameFile + '.pdf' },
        { force: true }
      );
      uploadReports.fillAllFormsOfReportsSelectedForUpload();
      selectItemFromDropdownByValue(
        cy.get('div[name="capex"] div[name="fileName"]').eq(0),
        differentFileNameForSameFile
      );
      selectItemFromDropdownByValue(cy.get('div[name="opex"] div[name="fileName"]').eq(0), `${TEST_PDF_FILE_NAME}2`);
      cy.intercept({ url: `**/documents/*`, method: 'HEAD', times: 1 }).as('documentExists');
      cy.intercept(`**/documents/`, cy.spy().as('postDocument'));
      cy.intercept(`**/api/data/${DataTypeEnum.EutaxonomyNonFinancials}*`).as('postCompanyAssociatedData');
      cy.get('button[data-test="submitButton"]').click();

      cy.wait('@documentExists', { timeout: Cypress.env('short_timeout_in_ms') as number })
        .its('response.statusCode')
        .should('equal', 200);
      cy.wait('@postCompanyAssociatedData', { timeout: Cypress.env('short_timeout_in_ms') as number });
      cy.url().should('eq', getBaseUrl() + '/datasets');
      cy.get('[data-test="datasets-table"]').should('be.visible');
      cy.get('@postDocument').should('not.have.been.called');
    }

    it(
      'Check if the file upload info remove button works as expected, make sure the file content hashes ' +
        'generated by frontend and backend are the same and that the exact document does not get reuploaded a second time',
      () => {
        getKeycloakToken(admin_name, admin_pw).then((token: string) => {
          const dummyCompanyInformation = generateDummyCompanyInformation(`Company-For-DataUpload-test-${Date.now()}`);
          return uploadCompanyViaApi(token, dummyCompanyInformation).then((storedCompany) => {
            return assignCompanyOwnershipToDatalandAdmin(token, storedCompany.companyId).then(() => {
              cy.ensureLoggedIn(admin_name, admin_pw);

              cy.visitAndCheckAppMount(
                `/companies/${storedCompany.companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}/upload`
              );
              uploadReports.selectFile(TEST_PDF_FILE_NAME);
              uploadReports.selectFile(`${TEST_PDF_FILE_NAME}2`);
              uploadReports.fillAllFormsOfReportsSelectedForUpload(2);
              fillRequiredEutaxonomyNonFinancialsFields();
              const revenueSelectorPrefix = 'div[name="revenue"] div[data-test="totalAmount"]';

              cy.get(`${revenueSelectorPrefix} [data-test="dataPointToggleButton"]`).click();
              cy.get(`${revenueSelectorPrefix} input[name="value"]`).type('250700');
              selectItemFromDropdownByIndex(cy.get(`${revenueSelectorPrefix} div[name="currency"]`), 1);
              selectItemFromDropdownByIndex(cy.get(`${revenueSelectorPrefix} div[name="quality"]`), 1);
              selectItemFromDropdownByValue(
                cy.get(`${revenueSelectorPrefix} div[name="fileName"]`).eq(0),
                TEST_PDF_FILE_NAME
              );

              const capexSelectorPrefix = 'div[name="capex"] div[data-test="totalAmount"]';

              cy.get(`${capexSelectorPrefix} [data-test="dataPointToggleButton"]`).click();
              cy.get(`${capexSelectorPrefix} input[name="value"]`).type('450700');
              selectItemFromDropdownByIndex(cy.get(`${capexSelectorPrefix} div[name="currency"]`), 10);
              selectItemFromDropdownByIndex(cy.get(`${capexSelectorPrefix} div[name="quality"]`), 1);
              selectItemFromDropdownByValue(
                cy.get(`${capexSelectorPrefix} div[name="fileName"]`).eq(0),
                `${TEST_PDF_FILE_NAME}2`
              );

              cy.intercept({ method: 'POST', url: `**/api/data/**`, times: 1 }, (request) => {
                const submittedEutaxonomyNonFinancialsData = assertDefined(
                  request.body as CompanyAssociatedDataEutaxonomyNonFinancialsData
                ).data;
                const submittedReferencedReports = assertDefined(
                  submittedEutaxonomyNonFinancialsData.general?.referencedReports
                );
                expect(`${TEST_PDF_FILE_NAME}2` in submittedReferencedReports).to.equal(true);
                if (TEST_PDF_FILE_NAME in submittedReferencedReports) {
                  frontendDocumentHash = submittedReferencedReports[TEST_PDF_FILE_NAME].fileReference;
                }
              }).as('submitData');
              cy.get('button[data-test="submitButton"]').click();
              cy.wait(`@submitData`, { timeout: Cypress.env('long_timeout_in_ms') as number }).then(() => {
                validateFrontendAndBackendDocumentHashesCoincide(token, frontendDocumentHash);
              });
              cy.url().should('eq', getBaseUrl() + '/datasets');
              cy.get('[data-test="datasets-table"]').should('be.visible');

              goToEditFormAndValidateExistenceOfReports(storedCompany.companyId, true);
              uploadReports.removeAlreadyUploadedReport(TEST_PDF_FILE_NAME);
              cy.intercept({ method: 'POST', url: `**/api/data/**`, times: 1 }, (request) => {
                const submittedEutaxonomyNonFinancialsData = assertDefined(
                  request.body as CompanyAssociatedDataEutaxonomyNonFinancialsData
                ).data;
                const submittedReports = assertDefined(submittedEutaxonomyNonFinancialsData.general?.referencedReports);
                expect(TEST_PDF_FILE_NAME in submittedReports).to.equal(false);
                expect(`${TEST_PDF_FILE_NAME}2` in submittedReports).to.equal(true);
              }).as('submitEditData');
              cy.get('button[data-test="submitButton"]').click();
              cy.wait(`@submitEditData`, { timeout: Cypress.env('long_timeout_in_ms') as number }).then(
                (interception) => {
                  expect(interception.response?.statusCode).to.eq(200);
                  cy.url().should('eq', getBaseUrl() + '/datasets');
                  cy.get('[data-test="datasets-table"]').should('be.visible');

                  goToEditFormAndValidateExistenceOfReports(storedCompany.companyId, false);
                  const metaDataOfReuploadedDataset = assertDefined(interception.response?.body) as DataMetaInformation;
                  checkThatFilesWithSameContentDontGetReuploaded(
                    storedCompany.companyId,
                    metaDataOfReuploadedDataset.dataId
                  );
                }
              );
            });
          });
        });
      }
    );
  }
);
