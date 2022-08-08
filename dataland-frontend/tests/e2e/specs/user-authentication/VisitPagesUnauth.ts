describe("As a user I expect to be redirected to the login page if I am unauthenticated", () => {
  const pages = ["/upload", "/search", "/searchtaxonomy", "/companies/:companyID/eutaxonomies"];

  pages.forEach((page) => {
    it(`Test Login Redirect for ${page}`, () => {
      cy.visit(page);
      cy.get("input[name=login]").should("exist").url().should("contain", "keycloak");
    });
  });
});
