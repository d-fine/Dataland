describe("As a developer, I expect the PGAdmin console to be available to me", () => {
  it("Checks if the PGAdmin console is available and the login page is shown", () => {
    cy.visit("dataland-admin:6789/pgadmin")
      .get("input[name=email]")
      .should("exist")
      .type("admin@dataland.com")
      .get("input[name=password]")
      .should("exist")
      .type(Cypress.env("PGADMIN_PASSWORD"))
      .get("button[name=internal_button]")
      .should("contain.text", "Login")
      .click();
    cy.get("span[class=file-name]").should("contain.text", "BackendDb");
  });
});
