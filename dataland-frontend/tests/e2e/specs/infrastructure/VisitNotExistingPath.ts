describe("As a user, I want to get redirected to a useful error page when I visit a non-existent page", () => {
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
