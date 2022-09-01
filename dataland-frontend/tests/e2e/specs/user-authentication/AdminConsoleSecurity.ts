describe("As a user I expect the admin console only to be reachable from localhost and not from remote", () => {
  it(`Test Admin Console not reachable from remote`, () => {
    cy.visit("/keycloak/admin");
    cy.get("h2").should("exist").should("contain", "Sorry an error occurred!");
    cy.url().should("contain", "nocontent");
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
