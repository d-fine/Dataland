describe("As a user I expect the admin console only to be reachable from localhost and not from remote", () => {
  it(`Test Admin Console not reachable from remote`, () => {
    cy.visit("/keycloak/admin");
    cy.get("h2").should("exist").should("contain", "Sorry an error occurred!");
    cy.url().should("contain", "nocontent");
  });
});
