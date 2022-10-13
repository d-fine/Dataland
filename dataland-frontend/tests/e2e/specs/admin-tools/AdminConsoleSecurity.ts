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
});
