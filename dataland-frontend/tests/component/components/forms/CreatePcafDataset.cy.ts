import CreatePcafDataset from '@/components/forms/CreatePcafDataset.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { getMountingFunction } from '@ct/testUtils/Mount';
import { type CompanyAssociatedDataPcafData } from '@clients/backend';
import { selectItemFromDropdownByValue } from '@sharedUtils/Dropdown.ts';
const mockDocuments = [
  {
    documentId: 'doc1-id',
    documentName: 'MarketCap_2024.pdf',
    documentCategory: 'MarketCapitalization',
    companyId: 'company-id',
    uploaderId: 'uploader-1',
    publicationDate: '2024-02-13',
    reportingPeriod: '2024',
  },
  {
    documentId: 'doc2-id',
    documentName: 'Scope1Emissions_2024.pdf',
    documentCategory: 'Scope1Emissions',
    companyId: 'company-id',
    uploaderId: 'uploader-2',
    publicationDate: '2024-01-10',
    reportingPeriod: '2024',
  },
];

const mockData: CompanyAssociatedDataPcafData = {
  companyId: 'company-id',
  reportingPeriod: '2024',
  data: {
    companyValue: {
      listedCompany: {
        marketCapitalizationInEUR: { value: 1000 },
      },
      unlistedCompany: {},
    },
    environmental: {
      greenhouseGasEmissions: {
        scope1GhgEmissionsInTonnes: { value: 500 },
      },
    },
    general: {
      general: { fiscalYearDeviation: null },
      company: { mainPcafSector: null, companyExchangeStatus: {} },
    },
  },
};

describe('As a user i want to upload a PCAF Dataset with documents', () => {
  beforeEach(() => {
    cy.intercept('GET', '**/documents/**', {
      statusCode: 200,
      body: mockDocuments.map((doc) => ({
        documentId: doc.documentId,
        documentName: doc.documentName,
        documentCategory: doc.documentCategory,
        companyId: doc.companyId,
        publicationDate: doc.publicationDate,
        reportingPeriod: doc.reportingPeriod,
      })),
    }).as('fetchDocumentMetadata');

    cy.intercept('POST', '**/api/data/pcaf*', {
      statusCode: 200,
      body: {
        companyId: mockData.companyId,
        reportingPeriod: mockData.reportingPeriod,
        data: mockData.data,
        dataId: 'test-123',
        success: true,
        message: 'Data uploaded successfully',
      },
    }).as('postPcafData');
  });

  it('Uploads documents and associates them with fields', () => {
    getMountingFunction({ keycloak: minimalKeycloakMock() })(CreatePcafDataset, {
      props: { companyID: 'company-id' },
      data() {
        return { companyAssociatedDataPcafData: mockData };
      },
    }).then(() => {
      cy.get('[data-test="reporting-period-picker"]').should('be.visible');
      cy.get('[data-test="marketCapitalizationInEUR"]').should('exist');
      cy.wait('@fetchDocumentMetadata');

      cy.get('[data-test="reporting-period-picker"]').click();

      cy.get('.p-datepicker-year').contains('2024').click();
      cy.get('.p-select-label').first().click();
      cy.get('.p-select-option').contains('Energy').should('contain.text', 'Energy').click();
      cy.get('[data-test="marketCapitalizationInEUR"]').within(() => {
        cy.get('[data-test="dataPointToggleButton"]').click();
        cy.get('.formkit-outer.col-4 input[type="text"]').clear().type('1000');
      });
      selectItemFromDropdownByValue(
        cy.get('[data-test="marketCapitalizationInEUR"] [data-test="dataReport"]').eq(0),
        'MarketCap_2024.pdf'
      );
      cy.get('button[data-test="submitButton"]').click();
      cy.wait('@postPcafData');
    });
  });

  it('shows error message when POST fails', () => {
    cy.intercept('POST', '**/api/data/pcaf*', {
      statusCode: 500,
      body: { errors: [{ summary: 'Server error', message: 'Something went wrong' }] },
    }).as('postPcafDataError');

    getMountingFunction({ keycloak: minimalKeycloakMock() })(CreatePcafDataset, {
      props: { companyID: 'company-id' },
    }).then(() => {
      cy.get('button[data-test="submitButton"]').click();
      cy.wait('@postPcafDataError');

      cy.get('.p-message-error').should('contain.text', 'Server error');
    });
  });
  it('prevents double submit by disabling the button briefly', () => {
    getMountingFunction({ keycloak: minimalKeycloakMock() })(CreatePcafDataset, {
      props: { companyID: 'company-id' },
    }).then(() => {
      cy.get('button[data-test="submitButton"]').click();
      cy.get('button[data-test="submitButton"]').should('be.disabled');
    });
  });
});
