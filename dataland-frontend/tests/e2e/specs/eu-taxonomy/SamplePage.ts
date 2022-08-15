describe("As a user, I expect the sample page to be functional and reachable without logging in", () => {
  function visitSamplePage() {
    cy.visitAndCheckAppMount("/");
    cy.get("h2").should("contain.text", "Explore Dataland");
    cy.get("button[name=eu_taxonomy_sample_button]")
      .should("be.visible")
      .should("contain.text", "EU Taxonomy")
      .click({ force: true })
      .url()
      .should("include", "/eutaxonomy-non-financials");
    cy.get("h2").contains("EU Taxonomy Data");
    cy.get(".p-button.p-button-rounded").should("contain.text", "COMPANY DATA SAMPLE");
    cy.get("body").should("contain.text", "Join Dataland with other");
    cy.get("[title=back_button").should("be.visible").click({ force: true });
    cy.url().should("eq", Cypress.config("baseUrl") + "/");
  }

  it("Check that the sample section works properly with authentication", () => {
    cy.ensureLoggedIn();
    visitSamplePage();
  });
  it("Check that the sample section works properly without authentication", () => {
    visitSamplePage();
  });
});
