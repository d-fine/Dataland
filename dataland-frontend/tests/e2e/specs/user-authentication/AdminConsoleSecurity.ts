describe("As a user I expect the admin console only to be reachable using admin-proxy and not from remote", () => {
  function checkThatUrlResolvesToErrorPage(url: string) {
    cy.visit(url);
    cy.get("h2").should("exist").should("contain", "Sorry an error occurred!");
    cy.url().should("contain", "nocontent");
  }

  it(`Test Admin Console not reachable from remote`, () => {
    checkThatUrlResolvesToErrorPage("/keycloak/admin");
  });

  it(`Master Realm not reachable from remote`, () => {
    checkThatUrlResolvesToErrorPage("/keycloak/realms/datalandsecurity");
  });

  it(`Datalandsecurity Realm is reachable from remote`, () => {
    cy.request("/keycloak/realms/master").its("body").should("contain", '{"realm":"datalandsecurity","public_key":');
  });

  it(`Test Admin Console is reachable via dataland-admin`, () => {
    cy.visit("http://dataland-admin:6789/keycloak/admin");
    cy.get("h1").should("exist").should("contain", "Sign in to your account");
    cy.url().should("contain", "realms/master");
    cy.get("#username")
      .should("exist")
      .type("admin", { force: true })
      .get("#password")
      .should("exist")
      .type(Cypress.env("KEYCLOAK_ADMIN_PASSWORD"), { force: true })
      .get("#kc-login")
      .should("exist")
      .click();
    cy.get("h1").should("exist").should("contain", "Master realm");
  });
});
