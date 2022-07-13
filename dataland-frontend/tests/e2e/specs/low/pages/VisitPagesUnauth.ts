describe("Test that if unauthenticated will be redirected to landing page", () => {
  it("test for each of given paths", () => {
    const pages = ["/upload", "/search", "/searchtaxonomy", "/companies/:companyID/eutaxonomies"];
    pages.forEach((page) => {
      cy.visitAndCheckAppMount(page);
      cy.url().should("eq", Cypress.config("baseUrl") + "/");
    });
  });
});
