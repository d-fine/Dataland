import UploadDocumentDialog from '@/components/resources/companyCockpit/UploadDocumentDialog.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak.ts';

/** Helper to upload a file */
function uploadFile(fileName: string): void {
  const blob = new Blob([fileName], { type: 'application/pdf' });
  cy.get('[data-test="file-upload"]').find('input[type=file]').selectFile(
    {
      contents: blob,
      fileName: fileName,
      mimeType: 'application/pdf',
    },
    { force: true }
  );
}

describe('Check the Upload Document modal', function (): void {
  beforeEach(function () {
    // @ts-ignore
    cy.mountWithPlugins(UploadDocumentDialog, {
      props: {
        visible: true,
        companyId: 'company-123',
      },
      keycloak: minimalKeycloakMock({}),
    });
  });

  it('Check error message visibility when nothing is selected', function (): void {
    cy.get('[data-test="upload-document-button"]').click();
    cy.get('[data-test="file-upload-error"]').should('be.visible');
    cy.get('[data-test="document-name-error"]').should('be.visible');
    cy.get('[data-test="document-category-error"]').should('be.visible');
    uploadFile('file1.pdf');
    cy.get('[data-test="file-upload-error"]').should('not.exist');
    cy.get('[data-test="document-name-error"]').should('be.visible');
    cy.get('[data-test="document-name"]').type('Test Document');
    cy.get('[data-test="document-name-error"]').should('not.exist');
    cy.get('[data-test="document-category"]').click();
    cy.get('.p-select-option').first().click();
    cy.get('[data-test="document-category-error"]').should('not.exist');
  });

  it('Check that no more than 1 document can be selected', function (): void {
    uploadFile('file1.pdf');
    cy.get('.p-fileupload-choose-button').should('be.disabled');
  });

  it('Check that all fields can be filled and the success modal opens', function (): void {
    cy.intercept('POST', '**/documents', {
      statusCode: 200,
    }).as('postDocumentData');
    uploadFile('file1.pdf');
    cy.get('[data-test="document-name"]').type('Test Document');
    cy.get('[data-test="document-category"]').click();
    cy.get('.p-select-option').first().click();
    cy.get('[data-test="publication-date"]').find('.p-datepicker-dropdown').click();
    cy.get('.p-datepicker-today').click();
    cy.get('[data-test="reporting-period"]').find('.p-datepicker-dropdown').click();
    cy.get('.p-datepicker-year').contains('2024').click();
    cy.get('[data-test="upload-document-button"]').click();
    cy.get('[data-test="success-modal"]').should('be.visible');
  });
});
