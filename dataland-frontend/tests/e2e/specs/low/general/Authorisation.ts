describe("Authentication Buttons", () => {
  it("Checks that normal and logout work", () => {
    cy.visitAndCheckAppMount("/");
    cy.login();
    cy.logout();
  });

  it("Checks that registering works", () => {
    cy.register();
    cy.logout();
  });

  it("Checks that user dropdown menu logout works", () => {
    cy.login();
    cy.logoutDropdown();
  });

  it("Checks that the back button on the login page works as expected", () => {
    cy.visit("/searchtaxonomy")
      .get("#back_button")
      .should("exist")
      .click()
      .url()
      .should("eq", Cypress.config("baseUrl") + "/");
  });
});
