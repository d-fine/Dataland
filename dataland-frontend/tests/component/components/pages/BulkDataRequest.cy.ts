import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import BulkDataRequest from '@/components/pages/BulkDataRequest.vue';

describe('Component tests for the BulkDataRequest page', () => {
  beforeEach(() => {
    cy.mountWithPlugins(BulkDataRequest, {
      keycloak: minimalKeycloakMock({}),
    });
  });

  it('Check email notification toggle', () => {
    cy.get('[data-test="notifyMeImmediately"]').within(() => {
      cy.get('[data-test="notifyMeImmediatelyInput"]').scrollIntoView();
      cy.get('[data-test="notifyMeImmediatelyInput"]').should('be.visible');
      cy.get('label').should('contain.text', 'summary');
      cy.get('[data-test="notifyMeImmediatelyInput"]').click();
      cy.get('label').should('contain.text', 'immediate');
      cy.get('[data-test="notifyMeImmediatelyInput"]').click();
      cy.get('label').should('contain.text', 'summary');
    });
  });
});
