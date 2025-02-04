//@ts-nocheck
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import BulkDataRequestSummary from '@/components/pages/BulkDataRequestSummary.vue';

describe('Component tests for the BulkDataRequestSummary page', () => {
  it('Should display the BulkRequestSummary with internal data', () => {
    cy.mountWithPlugins(BulkDataRequestSummary, {
      keycloak: minimalKeycloakMock({}),
      props: {
        humanizedReportingPeriods: '',
        summarySectionReportingPeriodsHeading: '',
        humanizedSelectedFrameworks: ['Test1', 'Test2'],
        summarySectionFrameworksHeading: '',
        bulkDataRequestResponse: {
          rejectedCompanyIdentifiers: ['REJECT123'],
          alreadyExistingDatasets: [
            {
              userProvidedIdentifier: '123',
              companyName: 'Company A',
              reportingPeriod: '2023',
              framework: 'sfdr',
              resourceId: 'dataset-1',
              resourceUrl: 'https://dataland.com/dataset-1',
            },
          ],
          acceptedDataRequests: [
            {
              userProvidedIdentifier: '456',
              companyName: 'Company B',
              resourceId: 'request-1',
              reportingPeriod: '2022',
              framework: 'p2p',
              resourceUrl: 'https://dataland.com/request-1',
            },
          ],
          alreadyExistingNonFinalRequests: [
            {
              userProvidedIdentifier: '789',
              companyName: 'Company C',
              resourceId: 'request-2',
              reportingPeriod: '2023',
              framework: 'vsme',
              resourceUrl: 'https://dataland.com/request-2',
            },
          ],
        } as BulkDataRequestSummary,
      },
    }).then(() => {
      cy.get('.summary-section-heading').contains('CREATED REQUESTS').should('exist').click();
      cy.get('.summary-section-heading').contains('SKIPPED REQUESTS - DATA ALREADY EXISTS').should('exist');
      cy.get('.summary-section-heading').contains('SKIPPED REQUESTS - REQUESTS ALREADY EXIST').should('exist').click();
      cy.get('.summary-section-heading').contains('REJECTED IDENTIFIERS').should('exist').click();

      cy.get('.grid-container').should('have.length', 3);

      cy.get('[data-test="acceptedDataRequestsContent"]').within(() => {
        cy.get('.bold-text').contains('Company B').should('exist');
        cy.get('.bold-text').contains('2022').should('exist');
        cy.get('.text-primary').contains('VIEW REQUEST').should('exist');
      });

      cy.get('[data-test="alreadyExistingDatasetsContent"]').within(() => {
        cy.get('.bold-text').contains('Company A').should('exist');
        cy.get('.text-primary').contains('VIEW DATA').should('exist');
      });

      cy.get('[data-test="alreadyExistingNonFinalRequestsContent"]').within(() => {
        cy.get('.bold-text').contains('Company C').should('exist');
        cy.get('.text-primary').contains('VIEW REQUEST').should('exist');
      });

      cy.get('[data-test="rejectedCompanyIdentifiersContent"]').within(($div) => {
        assert($div.text() == 'REJECT123');
      });
    });
  });
});
