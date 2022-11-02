import { getStringCypressEnv } from "@e2e/utils/Cypress";

describe("As a user I expect the admin console only to be reachable using admin-proxy and not from remote", (): void => {
  function checkThatUrlResolvesToErrorPage(url: string): void {
    cy.visit(url);
    cy.get("h2").should("exist").should("contain", "Sorry an error occurred!");
    cy.url().should("contain", "nocontent");
  }

  it(`Test Admin Console not reachable from remote`, () => {
    checkThatUrlResolvesToErrorPage("/keycloak/admin");
  });

  it(`Master Realm not reachable from remote`, () => {
    checkThatUrlResolvesToErrorPage("/keycloak/realms/master");
  });

  it(`Test Admin Console is reachable via dataland-admin`, () => {
    cy.visit("http://dataland-admin:6789/keycloak/admin");
    cy.get("h1").should("exist").should("contain", "Sign in to your account");
    cy.url().should("contain", "realms/master");
    cy.get("#username")
      .should("exist")
      .type(getStringCypressEnv("KEYCLOAK_ADMIN"), { force: true })
      .get("#password")
      .should("exist")
      .type(getStringCypressEnv("KEYCLOAK_ADMIN_PASSWORD"), { force: true })
      .get("#kc-login")
      .should("exist")
      .click();
    cy.get("h1").should("exist").should("contain", "master realm");
  });
});
