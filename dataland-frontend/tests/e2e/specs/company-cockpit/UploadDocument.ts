import { fetchTestCompanies, setupCommonInterceptions } from '@e2e/utils/CompanyCockpitPage/CompanyCockpitUtils';
import { uploadDocumentViaApi } from '@e2e/utils/DocumentUploadUtils.ts';
import { getKeycloakToken } from '@e2e/utils/Auth';
import { describeIf } from '@e2e/support/TestUtility';
import { admin_name, admin_pw } from '@e2e/utils/Cypress.ts';
import type { CompanyIdAndName } from '@clients/backend';

describeIf(
  'As an admin, I want to be able to upload documents via the UI',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  () => {
    let alphaCompanyIdAndName: CompanyIdAndName;
    let betaCompanyIdAndName: CompanyIdAndName;
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
      fetchTestCompanies().then(([alpha, beta]) => {
        alphaCompanyIdAndName = alpha;
        betaCompanyIdAndName = beta;
      });
      getKeycloakToken(admin_name, admin_pw).then((t) => {
        Cypress.env('token', t);
      });
    });

    beforeEach(() => {
      setupCommonInterceptions();
      cy.ensureLoggedIn(admin_name, admin_pw);
      cy.task('createUniquePdfFixture').then((filename) => {
        testDocFileName = filename as string;
        testDocFilePath = testDocFilePathBase + testDocFileName;
      });
    });

    afterEach(() => {
      if (testDocFileName) {
        cy.task('deleteFile', testDocFilePath);
        testDocFileName = '';
      }
    });

    it('Uploads a new document and verifies success', () => {
      visitDocumentPageAndUploadDocument(alphaCompanyIdAndName.companyId);
      cy.get('[data-test="success-modal"]').should('be.visible');
      cy.contains('Document uploaded successfully.').should('be.visible');
      cy.get('[data-test="close-success-modal-button"]').click();
      cy.contains(testDocFileName).should('exist');
    });

    it('Shows conflict modal for already associated document', () => {
      const token = Cypress.env('token');
      cy.readFile(testDocFilePath, null).then((buffer) => {
        void uploadDocumentViaApi(token, buffer, testDocFileName, {
          documentName: testDocFileName,
          documentCategory: 'Other',
          companyIds: [alphaCompanyIdAndName.companyId] as unknown as Set<string>,
        });
      });
      visitDocumentPageAndUploadDocument(alphaCompanyIdAndName.companyId);
      cy.contains('Document already exists').should('be.visible');
      cy.get('[data-test="ok-button"]').should('be.visible').click();
      cy.contains(testDocFileName).should('exist');
    });

    it('Associate document with new company after conflict', () => {
      const token = Cypress.env('token');
      cy.readFile(testDocFilePath, null).then((buffer) => {
        void uploadDocumentViaApi(token, buffer, testDocFileName, {
          documentName: testDocFileName,
          documentCategory: 'Other',
          companyIds: [betaCompanyIdAndName.companyId] as unknown as Set<string>,
        });
      });
      visitDocumentPageAndUploadDocument(alphaCompanyIdAndName.companyId);
      cy.contains('Document already exists').should('be.visible');
      cy.contains(betaCompanyIdAndName.companyName).should('be.visible');
      cy.get('[data-test="associate-document-button"]').should('be.visible').click();
      cy.get('[data-test="success-modal"]').should('be.visible');
      cy.contains('Document associated successfully.').should('be.visible');
      cy.get('[data-test="close-success-modal-button"]').click();
      cy.contains(testDocFileName).should('exist');
    });
  }
);
