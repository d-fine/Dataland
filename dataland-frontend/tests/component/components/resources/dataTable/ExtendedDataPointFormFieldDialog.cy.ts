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
    cy.get('[data-test="document-select"] .p-select-label').click();
    cy.get('.p-select-option-label').should('contain', 'AnnualReport2022.pdf');
    cy.get('.p-select-option-label').should('contain', 'SustainabilityReport2023.pdf');
  });
});
