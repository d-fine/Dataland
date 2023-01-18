import { login } from "@e2e/utils/Auth";

describe("As a user I expect Dataland to use PKCE flow to prevent auth loss", () => {
  it(`Test that code_verifier is sent along with token request`, () => {
    cy.intercept("/keycloak/realms/datalandsecurity/protocol/openid-connect/token").as("tokenRequest");
    login();
    cy.wait("@tokenRequest").its("request.body").should("contain", "code_verifier");
  });
});
