// @ts-nocheck
import CreateApiKeyCard from '@/components/resources/apiKey/CreateApiKeyCard.vue';
import { KEYCLOAK_ROLE_ADMIN, KEYCLOAK_ROLE_USER } from '@/utils/KeycloakUtils';

describe('Component test for CreateApiKeyCard', () => {
  it('Should have class invalidExpiryTimeText when expire time is invalid', () => {
    cy.mountWithPlugins(CreateApiKeyCard, {
      data() {
        return {
          isExpiryDateValid: false,
          userRoles: [KEYCLOAK_ROLE_USER, KEYCLOAK_ROLE_ADMIN],
        };
      },
    });
    cy.get('label[for="expiryTime"]').should('have.class', 'invalidExpiryTimeText');
  });
  it('Should not have class invalidExpiryTimeText when expire time is valid', () => {
    cy.mountWithPlugins(CreateApiKeyCard, {
      data() {
        return {
          isExpiryDateValid: true,
          userRoles: [KEYCLOAK_ROLE_USER, KEYCLOAK_ROLE_ADMIN],
        };
      },
    });
    cy.get('label[for="expiryTime"]').should('not.have.class', 'invalidExpiryTimeText');
  });
});
