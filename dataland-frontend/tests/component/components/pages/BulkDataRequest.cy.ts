import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import BulkDataRequest from '@/components/pages/BulkDataRequest.vue';

describe('Component tests for the BulkDataRequest page', () => {
  beforeEach(() => {
    cy.mountWithPlugins(BulkDataRequest, {
      keycloak: minimalKeycloakMock({}),
    });
  });

  it('Check email notification toggle', () => {
    cy.get('[data-test="emailOnUpdate"]').within(() => {
      cy.get('[data-test="emailOnUpdateInput"]').scrollIntoView();
      cy.get('[data-test="emailOnUpdateInput"]').should('be.visible');
      cy.get('[data-test="emailOnUpdateText"]').should('contain.text', 'summary');
      cy.get('[data-test="emailOnUpdateInput"]').click();
      cy.get('[data-test="emailOnUpdateText"]').should('contain.text', 'immediate');
      cy.get('[data-test="emailOnUpdateInput"]').click();
      cy.get('[data-test="emailOnUpdateText"]').should('contain.text', 'summary');
    });
  });
});
