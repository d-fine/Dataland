import { loginAsReader } from '@e2e/utils/Auth';

const shortTimeoutInMs = Number(Cypress.expose('short_timeout_in_ms') ?? 10000);

describe('As a user I expect Dataland to use PKCE flow to prevent auth loss', () => {
  it(`Test that code_verifier is sent along with token request`, () => {
    cy.intercept('/keycloak/realms/datalandsecurity/protocol/openid-connect/token').as('tokenRequest');
    loginAsReader();
    cy.wait('@tokenRequest', { timeout: shortTimeoutInMs }).its('request.body').should('contain', 'code_verifier');
  });
});
