import { fetchTestCompanies, setupCommonInterceptions } from '@e2e/utils/CompanyCockpitPage/CompanyCockpitUtils';
import { uploadDocumentViaApi } from '@e2e/utils/DocumentUploadUtils.ts';
import { getKeycloakToken } from '@e2e/utils/Auth';
import { describeIf } from '@e2e/support/TestUtility';
import { admin_name, admin_pw } from '@e2e/utils/Cypress.ts';

describeIf(
  'As an admin, I want to be able to upload documents via the UI',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  () => {
    let testDocFilePath: string;
    let testDocFileName: string;
    const testDocFilePathBase = 'tests/e2e/fixtures/documents/';

    /** Visits the document page of a specific company and uploads a document */
    function visitDocumentPageAndUploadDocument(companyId: string): void {
      cy.visit(`/companies/${companyId}/documents`);
      cy.get('[data-test="document-upload-button"]').should('be.visible').click();
      cy.get('[data-test="upload-document-modal"]').should('be.visible');
      cy.get('[data-test="file-upload"]').find('input[type="file"]').selectFile(testDocFilePath, { force: true });
      cy.get('[data-test="document-name"]').type(testDocFileName);
      cy.get('[data-test="document-category"]').click();
      cy.get('.p-select-option').first().click();
      cy.get('[data-test="upload-document-button"]').click();
    }

    before(() => {
      setupCommonInterceptions();
      fetchTestCompanies().then(([a, b]) => {
        Cypress.env('companyA', a);
        Cypress.env('companyB', b);
      });
      getKeycloakToken(admin_name, admin_pw).then((t) => {
        Cypress.env('token', t);
      });
    });

    beforeEach(() => {
      setupCommonInterceptions();
      cy.ensureLoggedIn(admin_name, admin_pw);
      cy.task('createUniqueTxtFixture').then((filename) => {
        testDocFileName = filename as string;
        testDocFilePath = testDocFilePathBase + testDocFileName;
      });
    });

    afterEach(() => {
      if (testDocFileName) {
        cy.task('deleteFile', testDocFilePathBase + testDocFileName);
      }
    });

    it('Uploads a new document and verifies success', () => {
      const companyA = Cypress.env('companyA');
      visitDocumentPageAndUploadDocument(companyA.companyId);
      cy.get('[data-test="successModal"]').should('be.visible');
      cy.contains('Document uploaded successfully.').should('be.visible');
      cy.get('[data-test="close-success-modal-button"]').click();
      cy.contains(testDocFileName).should('exist');
    });

    it('Shows conflict modal for already associated document', () => {
      const companyA = Cypress.env('companyA');
      const token = Cypress.env('token');
      cy.readFile(testDocFilePath, null).then((buffer) => {
        void uploadDocumentViaApi(token, buffer, testDocFileName, {
          documentName: testDocFileName,
          documentCategory: 'Other',
          companyIds: [companyA.companyId] as unknown as Set<string>,
        });
      });
      visitDocumentPageAndUploadDocument(companyA.companyId);
      cy.contains('Document already exists').should('be.visible');
      cy.get('[data-test="ok-button"]').should('be.visible').click();
      cy.contains(testDocFileName).should('exist');
    });

    it('Associate document with new company after conflict', () => {
      const companyA = Cypress.env('companyA');
      const companyB = Cypress.env('companyB');
      const token = Cypress.env('token');
      cy.readFile(testDocFilePath, null).then((buffer) => {
        void uploadDocumentViaApi(token, buffer, testDocFileName, {
          documentName: testDocFileName,
          documentCategory: 'Other',
          companyIds: [companyB.companyId] as unknown as Set<string>,
        });
      });
      visitDocumentPageAndUploadDocument(companyA.companyId);
      cy.contains('Document already exists').should('be.visible');
      cy.get('[data-test="associate-document-button"]').should('be.visible').click();
      cy.get('[data-test="successModal"]').should('be.visible');
      cy.contains('Document associated successfully.').should('be.visible');
      cy.get('[data-test="close-success-modal-button"]').click();
      cy.contains(testDocFileName).should('exist');
    });
  }
);
