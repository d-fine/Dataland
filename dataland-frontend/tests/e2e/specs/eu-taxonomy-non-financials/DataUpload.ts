import { assertDefined } from '@/utils/TypeScriptUtils';
import { type CompanyAssociatedDataEutaxonomyNonFinancialsData, DataTypeEnum } from '@clients/backend';
import { describeIf } from '@e2e/support/TestUtility';
import { getAdminToken } from '@e2e/utils/Auth';
import { generateDummyCompanyInformation, getOrUploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { TEST_PDF_FILE_NAME, TEST_PDF_FILE_PATH } from '@sharedUtils/ConstantsForPdfs';
import { getBaseUrl } from '@e2e/utils/Cypress';
import { uploadDocumentViaApi } from '@e2e/utils/DocumentUploadUtils.ts';
import { assignCompanyOwnershipToDatalandAdmin } from '@e2e/utils/CompanyRolesUtils';
import { UploadReports } from '@sharedUtils/components/UploadReports';
import { selectItemFromDropdownByIndex, selectItemFromDropdownByValue } from '@sharedUtils/Dropdown';

const shortTimeoutInMs = Number(Cypress.expose('short_timeout_in_ms') ?? 10000);
const longTimeoutInMs = Number(Cypress.expose('long_timeout_in_ms') ?? 100000);

/**
 * Fills all the required fields of the eu-taxonomy upload form for non-financial companies to enable submit button
 */
function fillRequiredEutaxonomyNonFinancialsFields(): void {
  cy.get(
    'div[data-test="fiscalYearEnd"] div[data-test="toggleDataPointWrapper"] div[data-test="dataPointToggleButton"]'
  ).within(() => {
    cy.get('#dataPointIsAvailableSwitch').click();
  });
  cy.get('[data-test="fiscalYearEnd"] button').should('have.class', 'p-datepicker-dropdown').click();
  cy.get('.p-datepicker-header').find('button[aria-label="Next Month"]').first().click();
  cy.get('.p-datepicker-day-view').find('span:contains("11")').click();
  selectItemFromDropdownByIndex(cy.get('[data-test="assurance-form-field"]'), 1);
  cy.get('input[name="provider"]').type('Some Assurance Provider Company');
}

/**
 * Checks that the computed hash in the frontend is the same as the one returned by the document upload endpoint
 *
 * @param keycloakToken token given by keycloak after logging in
 * @param frontendDocumentHash calculated hash of the document
 */
function validateFrontendAndBackendDocumentHashesCoincide(keycloakToken: string, frontendDocumentHash: string): void {
  cy.task<{ [type: string]: ArrayBuffer }>('readFile', `../${TEST_PDF_FILE_PATH}`).then(async (bufferObject) => {
    await uploadDocumentViaApi(keycloakToken, bufferObject.data, TEST_PDF_FILE_PATH).then((response) => {
      expect(frontendDocumentHash).to.equal(response.documentId);
    });
  });
}

/**
 * Creates a company and assigns ownership to the dataland admin user.
 *
 * @param token keycloak access token
 * @returns token and created company id
 */
function createOwnedCompany(token: string): Promise<{ token: string; companyId: string }> {
  const dummyCompanyInformation = generateDummyCompanyInformation(`Company-For-DataUpload-test-${Date.now()}`);
  return getOrUploadCompanyViaApi(token, dummyCompanyInformation).then((storedCompany) => {
    return assignCompanyOwnershipToDatalandAdmin(token, storedCompany.companyId).then(() => {
      return { token: token, companyId: storedCompany.companyId };
    });
  });
}

describeIf(
  'As a user, I expect that the upload form works correctly when uploading a new eu-taxonomy dataset for a non-financial company',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  function () {
    let frontendDocumentHash = '';
    const uploadReports = new UploadReports('referencedReports');
    before(() => {
      Cypress.expose('excludeBypassQaIntercept', true);
    });

    /**
     * Submits an initial dataset with uploaded reports and validates the frontend and backend hash consistency.
     *
     * @param token keycloak access token
     * @param companyId id of the company to upload data for
     */
    function submitInitialDatasetAndValidateHash(token: string, companyId: string): void {
      cy.ensureLoggedInAsAdmin();
      cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}/upload`);
      uploadReports.selectFile(TEST_PDF_FILE_NAME);
      uploadReports.selectFile(`${TEST_PDF_FILE_NAME}2`);
      uploadReports.fillAllFormsOfReportsSelectedForUpload(2);
      fillRequiredEutaxonomyNonFinancialsFields();
      const revenueSelectorPrefix = 'div[name="revenue"] div[data-test="totalAmount"]';

      cy.get(`${revenueSelectorPrefix} [data-test="dataPointToggleButton"]`).within(() => {
        cy.get('#dataPointIsAvailableSwitch').click();
      });
      cy.get(`${revenueSelectorPrefix} input[name="value"]`).type('250700');
      selectItemFromDropdownByIndex(cy.get(`${revenueSelectorPrefix} div[data-test="currency"]`), 1);
      selectItemFromDropdownByIndex(cy.get(`${revenueSelectorPrefix} div[data-test="dataQuality"]`), 1);
      selectItemFromDropdownByValue(
        cy.get(`${revenueSelectorPrefix} div[data-test="dataReport"]`).eq(0),
        TEST_PDF_FILE_NAME
      );

      const capexSelectorPrefix = 'div[name="capex"] div[data-test="totalAmount"]';

      cy.get(`${capexSelectorPrefix} [data-test="dataPointToggleButton"]`).within(() => {
        cy.get('#dataPointIsAvailableSwitch').click();
      });
      cy.get(`${capexSelectorPrefix} input[name="value"]`).type('450700');
      selectItemFromDropdownByIndex(cy.get(`${capexSelectorPrefix} div[data-test="currency"]`), 10);
      selectItemFromDropdownByIndex(cy.get(`${capexSelectorPrefix} div[data-test="dataQuality"]`), 1);
      selectItemFromDropdownByValue(
        cy.get(`${capexSelectorPrefix} div[data-test="dataReport"]`).eq(0),
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
      cy.wait(`@submitData`, { timeout: longTimeoutInMs }).then(() => {
        validateFrontendAndBackendDocumentHashesCoincide(token, frontendDocumentHash);
      });
      cy.url().should('eq', getBaseUrl() + '/datasets');
      cy.get('[data-test="datasets-table"]').should('be.visible');
    }

    /**
     * This method verifies that a file is not uploaded a second time if its content hash already exists on the
     * backend, even when the duplicate content is submitted as part of an entirely independent dataset creation.
     *
     * @param companyId the ID of the company for which a new dataset is created
     */
    function checkThatFilesWithSameContentDontGetReuploaded(companyId: string): void {
      const differentFileNameForSameFile = `${TEST_PDF_FILE_NAME}FileCopy`;
      cy.ensureLoggedInAsAdmin();
      cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}/upload`);
      cy.get(`button[data-test='upload-files-button-referencedReports']`).click();
      cy.get(`div[data-test='upload-documents-referencedReports']`)
        .find('input[type=file]')
        .selectFile(
          { contents: `../${TEST_PDF_FILE_PATH}`, fileName: differentFileNameForSameFile + '.pdf' },
          { force: true }
        );
      uploadReports.fillAllFormsOfReportsSelectedForUpload();
      fillRequiredEutaxonomyNonFinancialsFields();

      const revenueSelectorPrefix = 'div[name="revenue"] div[data-test="totalAmount"]';
      cy.get(`${revenueSelectorPrefix} [data-test="dataPointToggleButton"]`).within(() => {
        cy.get('#dataPointIsAvailableSwitch').click();
      });
      cy.get(`${revenueSelectorPrefix} input[name="value"]`).type('250700');
      selectItemFromDropdownByIndex(cy.get(`${revenueSelectorPrefix} div[data-test="currency"]`), 1);
      selectItemFromDropdownByIndex(cy.get(`${revenueSelectorPrefix} div[data-test="dataQuality"]`), 1);
      selectItemFromDropdownByValue(
        cy.get(`${revenueSelectorPrefix} div[data-test="dataReport"]`).eq(0),
        differentFileNameForSameFile
      );

      cy.intercept({ url: `**/documents/*`, method: 'HEAD', times: 1 }).as('documentExists');
      cy.intercept(`**/documents/`, cy.spy().as('postDocument'));
      cy.intercept(`**/api/data/${DataTypeEnum.EutaxonomyNonFinancials}*`).as('postCompanyAssociatedData');
      cy.get('button[data-test="submitButton"]').click();

      cy.wait('@documentExists', { timeout: shortTimeoutInMs }).its('response.statusCode').should('equal', 200);
      cy.wait('@postCompanyAssociatedData', { timeout: shortTimeoutInMs });
      cy.url().should('eq', getBaseUrl() + '/datasets');
      cy.get('[data-test="datasets-table"]').should('be.visible');
      cy.get('@postDocument').should('not.have.been.called');
    }

    it(
      'Check that the file content hashes generated by frontend and backend are the same and that a file is not ' +
        'reuploaded when the exact same document content is later submitted as part of an independent dataset',
      () => {
        getAdminToken()
          .then((token: string) => {
            return createOwnedCompany(token).then(({ companyId }) => {
              submitInitialDatasetAndValidateHash(token, companyId);
              return createOwnedCompany(token);
            });
          })
          .then(({ companyId }) => {
            checkThatFilesWithSameContentDontGetReuploaded(companyId);
          });
      }
    );
  }
);
