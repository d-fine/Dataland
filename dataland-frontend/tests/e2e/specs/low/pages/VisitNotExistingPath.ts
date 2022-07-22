describe("Test that if a not existing page is called the redirect to error page works", () => {
  it("test for each of given paths", () => {
    cy.visitAndCheckAppMount("/ddsd");
    cy.get("body").should("contain.text", "Something went wrong");
    cy.get('a[title="back to landing page"]')
      .should("contain.text", "Dataland")
      .click({ force: true })
      .url()
      .should("eq", Cypress.config("baseUrl") + "/");
  });
});
