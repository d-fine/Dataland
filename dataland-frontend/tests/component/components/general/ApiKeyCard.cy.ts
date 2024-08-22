// @ts-nocheck
import ApiKeyCard from '@/components/resources/apiKey/ApiKeyCard.vue';
import { KEYCLOAK_ROLE_ADMIN, KEYCLOAK_ROLE_USER } from '@/utils/KeycloakUtils';

describe('Component test for ApiKeyCard', () => {
  it("Should contain text 'The API Key expired' when Api Key is expired", () => {
    cy.mountWithPlugins(ApiKeyCard, {
      data() {
        return {
          viewDeleteConfirmation: false,
          userRoles: [KEYCLOAK_ROLE_USER, KEYCLOAK_ROLE_ADMIN],
          expiryDateInMilliseconds: 1,
        };
      },
    });
    cy.get('div#existingApiKeyCard').should('exist').should('contain.text', 'The API Key expired');
    cy.get('div#existingApiKeyCard span').should('have.class', 'text-red-700');
  });
  it("Should contain text 'The API Key has no defined expiry date' when Api Key has no defined expiry date", () => {
    cy.mountWithPlugins(ApiKeyCard, {
      data() {
        return {
          viewDeleteConfirmation: false,
          userRoles: [KEYCLOAK_ROLE_USER, KEYCLOAK_ROLE_ADMIN],
          expiryDateInMilliseconds: null,
        };
      },
    });
    cy.get('div#existingApiKeyCard').should('exist').should('contain.text', 'The API Key has no defined expiry date');
  });
});
