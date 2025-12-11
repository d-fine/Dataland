import { minimalKeycloakMock } from '@ct/testUtils/Keycloak.ts';
import CreditsCard from '@/components/resources/companyCockpit/CreditsCard.vue';
import options from 'axios';

/**
 * Mounts the CreditsCard component with the provided dummyCompanyId.
 */
function mountComponent(dummyCompanyId: string): void {
  cy.mountWithPlugins(CreditsCard as any, {
    props: {
      companyId: dummyCompanyId,
    },
    global: {
      provide: {
        getKeycloakPromise: () => Promise.resolve(minimalKeycloakMock({})),
      },
    },
    ...options,
  });
}

/**
 * Intercepts the credit balance API call and returns the provided body.
 * @param dummyCompanyId
 * @param body
 */
function interceptCreditBalance(dummyCompanyId: string, body: number): void {
  cy.intercept('GET', `**/accounting/credits/${dummyCompanyId}/balance*`, {
    statusCode: 200,
    body,
  }).as('creditBalance');
}

describe('As a user I want to see available credits on Dataland of the company I am associated to.', () => {
  const dummyCompanyId = '550e8400-e29b-11d4-a716-446655440000';

  it('Credit Balance is visible and correctly displayed if a Member has credits', () => {
    interceptCreditBalance(dummyCompanyId, 100);
    mountComponent(dummyCompanyId);
    cy.wait('@creditBalance');

    cy.get('[data-test="credits-balance-chip"]').should('be.visible');
    cy.get('[data-test="credits-balance-chip"]').should('contain', '100');
  });
});
