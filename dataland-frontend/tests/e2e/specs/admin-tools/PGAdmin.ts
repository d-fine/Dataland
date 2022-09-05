describe("As a developer, I expect the PGAdmin console to be available to me", () => {
  it("Checks if the PGAdmin console is available and the login page is shown", () => {
    cy.visit("http://dataland-admin:6789/pgadmin");
    cy.get("button[name=internal_button]").should("contain.text", "Login");
  });
});
