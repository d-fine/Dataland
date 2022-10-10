import { login } from "@e2e/utils/Auth";

describe("As a user I expect Dataland to use PKCE flow to prevent auth loss", (): void => {
  it(`Test that code_verifier is sent along with token request`, (): void => {
    cy.intercept("/keycloak/realms/datalandsecurity/protocol/openid-connect/token").as("tokenRequest");
    login();
    cy.wait("@tokenRequest", { timeout: 5 * 1000 })
      .its("request.body")
      .should("contain", "code_verifier");
  });
});
