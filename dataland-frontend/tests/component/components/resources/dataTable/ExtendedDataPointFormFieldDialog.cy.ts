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

  it('displays documents from API in the select dropdown', () => {
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
    cy.wait('@fetchDocuments');
    cy.get('[data-test="document-select"] .p-select-label').click();
    cy.get('.p-select-option-label').contains('AnnualReport2022.pdf').click();
    cy.get('.dataland-info-text').should('contain', 'Name: AnnualReport2022.pdf');
    cy.get('.dataland-info-text').should('contain', 'Category: Financial');
    cy.get('.dataland-info-text').should('contain', 'Publication Date: 2022-12-31');
    cy.get('.dataland-info-text').should('contain', 'Reporting Period: 2022');
    cy.get('[data-test="document-select"] .p-select-label').click();
    cy.get('.p-select-option-label').contains('SustainabilityReport2023.pdf').click();
    cy.get('.dataland-info-text').should('contain', 'Name: SustainabilityReport2023.pdf');
    cy.get('.dataland-info-text').should('contain', 'Category: Sustainability');
    cy.get('.dataland-info-text').should('contain', 'Publication Date: 2023-06-30');
    cy.get('.dataland-info-text').should('contain', 'Reporting Period: 2023');
  });
});
