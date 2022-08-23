describe("As a user, I expect to get redirected to the company search page when I am logged in and visit the landing page", () => {
  it("Checks that the redirect works", () => {
    cy.ensureLoggedIn();
    cy.visit("/")
      .url()
      .should("eq", Cypress.config("baseUrl") + "/companies");
  });
});
