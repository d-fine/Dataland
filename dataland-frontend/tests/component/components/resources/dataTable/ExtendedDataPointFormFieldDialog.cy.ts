import { minimalKeycloakMock } from '@ct/testUtils/Keycloak.ts';
import ExtendedDataPointFormFieldDialog from '@/components/resources/dataTable/modals/ExtendedDataPointFormFieldDialog.vue';

describe('As a user I want to have displayed the associated documents for the company I am editing', () => {
  const dummyCompanyId = 'test-company-id';
  const mockDocuments = [
    {
      documentId: 'doc1',
      documentName: 'AnnualReport2022.pdf',
      documentCategory: 'Financial',
      publicationDate: '2022-12-31',
      reportingPeriod: '2022',
    },
    {
      documentId: 'doc2',
      documentName: 'SustainabilityReport2023.pdf',
      documentCategory: 'Sustainability',
      publicationDate: '2023-06-30',
      reportingPeriod: '2023',
    },
  ];

  beforeEach(() => {
    cy.intercept('GET', '**/documents/**', {
      statusCode: 200,
      body: mockDocuments,
    }).as('fetchDocuments');
  });

  /**
   * Mounts the ExtendedDataPointFormFieldDialog with the standard test setup used by all
   * test cases in this file.
   */
  function mountDialog(): void {
    cy.mountWithPlugins(ExtendedDataPointFormFieldDialog, {
      keycloak: minimalKeycloakMock({}),
      global: {
        provide: {
          companyId: dummyCompanyId,
          dialogRef: { value: { close: cy.stub() } },
          getKeycloakPromise: () => Promise.resolve(minimalKeycloakMock({})),
        },
      },
    });
  }

  it('displays documents from API in the select dropdown', () => {
    mountDialog();
    cy.wait('@fetchDocuments');
    cy.get('[data-test="page-number-input"]').should('be.disabled');
    cy.get('[data-test="document-select"] .p-select-label').click();
    cy.get('.p-select-option-label').should('contain', 'AnnualReport2022.pdf');
    cy.get('.p-select-option-label').should('contain', 'SustainabilityReport2023.pdf');
    cy.get('.p-select-option-label').contains('AnnualReport2022.pdf').click();
    cy.get('[data-test="page-number-input"]').should('not.be.disabled');
    cy.get('[data-test="page-number-input"]').type('123-456').should('have.value', '123-456');
    cy.get('[data-test="document-select"] .p-select-label').click();
  });

  it('updates meta information when different documents are selected', () => {
    mountDialog();
    cy.wait('@fetchDocuments');
    cy.get('[data-test="document-select"] .p-select-label').click();
    cy.get('.p-select-option-label').contains(mockDocuments[0].documentName).click();
    cy.get('.dataland-info-text').should('contain', `Name: ${mockDocuments[0].documentName}`);
    cy.get('.dataland-info-text').should('contain', `Category: ${mockDocuments[0].documentCategory}`);
    cy.get('.dataland-info-text').should('contain', `Publication Date: ${mockDocuments[0].publicationDate}`);
    cy.get('.dataland-info-text').should('contain', `Reporting Period: ${mockDocuments[0].reportingPeriod}`);
    cy.get('[data-test="document-select"] .p-select-label').click();
    cy.get('.p-select-option-label').contains(mockDocuments[1].documentName).click();
    cy.get('.dataland-info-text').should('contain', `Name: ${mockDocuments[1].documentName}`);
    cy.get('.dataland-info-text').should('contain', `Category: ${mockDocuments[1].documentCategory}`);
    cy.get('.dataland-info-text').should('contain', `Publication Date: ${mockDocuments[1].publicationDate}`);
    cy.get('.dataland-info-text').should('contain', `Reporting Period: ${mockDocuments[1].reportingPeriod}`);
  });

  it('shows a validation error for an invalid page reference and accepts a valid comma-separated page list', () => {
    mountDialog();
    cy.wait('@fetchDocuments');
    cy.get('[data-test="document-select"] .p-select-label').click();
    cy.get('.p-select-option-label').contains(mockDocuments[0].documentName).click();

    cy.get('[data-test="page-number-input"]').type('5-3');
    cy.get('[data-test="page-number-error"]').should('exist');

    cy.get('[data-test="page-number-input"]').clear().type('4, 112');
    cy.get('[data-test="page-number-error"]').should('not.exist');
  });
});
