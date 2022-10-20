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
    checkThatUrlResolvesToErrorPage("/keycloak/realms/master");
  });

  it(`Test Admin Console is reachable via dataland-admin`, () => {
    cy.visit("dataland-admin:6789/keycloak/admin");
    cy.get('html:root').eq(0).invoke('prop', 'outerHTML').then((doc) => cy.log(doc))
    cy.get("h1").should("exist").should("contain", "Sign in to your account");
    cy.url().should("contain", "realms/master");
    cy.get("#username")
      .should("exist")
      .type(Cypress.env("KEYCLOAK_ADMIN"), { force: true })
      .get("#password")
      .should("exist")
      .type(Cypress.env("KEYCLOAK_ADMIN_PASSWORD"), { force: true })
      .get("#kc-login")
      .should("exist")
      .click();
    cy.get("h1").should("exist").should("contain", "Master realm");
  });
});
