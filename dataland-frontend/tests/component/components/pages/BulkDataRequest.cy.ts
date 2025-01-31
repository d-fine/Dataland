import BulkDataRequest from '@/components/pages/BulkDataRequest.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';

describe('Component tests for the BulkDataRequest page', () => {
  it('Should display the BulkRequestSummary with internal data', () => {
    cy.mountWithPlugins(BulkDataRequest, {
      keycloak: minimalKeycloakMock({}),
      data() {
        return {
          bulkDataRequestModel: {},
          availableFrameworks: ['sfdr', 'p2p'],
          submittingSucceeded: true,
          submittingInProgress: false,
          postBulkDataRequestObjectProcessed: true,
          message: '',
          rejectedCompanyIdentifiers: ['REJECT123'],
          existingDataSets: [
            {
              userProvidedCompanyId: '123',
              companyName: 'Company A',
              reportingPeriod: '2023',
              framework: 'sfdr',
              datasetId: 'dataset-1',
              datasetUrl: 'https://dataland.com/dataset-1',
            },
          ],
          createdRequests: [
            {
              userProvidedCompanyId: '456',
              companyName: 'Company B',
              requestId: 'request-1',
              reportingPeriod: '2022',
              framework: 'p2p',
              requestUrl: 'https://dataland.com/request-1',
            },
          ],
          existingRequests: [
            {
              userProvidedCompanyId: '789',
              companyName: 'Company C',
              requestId: 'request-2',
              reportingPeriod: '2023',
              framework: 'vsme',
              requestUrl: 'https://dataland.com/request-2',
            },
          ],
        };
      },
    });

    cy.get('.summary-section-heading').contains('CREATED REQUESTS').should('exist');
    cy.get('.summary-section-heading').contains('SKIPPED REQUESTS - DATA ALREADY EXISTS').should('exist');
    cy.get('.summary-section-heading').contains('SKIPPED REQUESTS - REQUESTS ALREADY EXIST').should('exist');
    cy.get('.summary-section-heading').contains('REJECTED IDENTIFIERS').should('exist');

    cy.get('.grid-container')
      .first()
      .within(() => {
        cy.get('.bold-text').contains('Company B').should('exist');
        cy.get('.bold-text').contains('2022').should('exist');
        cy.get('.text-primary').contains('VIEW REQUEST').should('exist');
      });

    cy.get('.grid-container')
      .eq(1)
      .within(() => {
        cy.get('.bold-text').contains('Company A').should('exist');
        cy.get('.text-primary').contains('VIEW DATA').should('exist');
      });

    cy.get('.grid-container')
      .eq(2)
      .within(() => {
        cy.get('.bold-text').contains('Company C').should('exist');
        cy.get('.text-primary').contains('VIEW REQUEST').should('exist');
      });

    cy.get('.grid-container')
      .eq(3)
      .within(() => {
        cy.get('.bold-text').contains('REJECT123').should('exist');
      });
  });
});
