describe("As a user I expect to be redirected to the login page if I am unauthenticated", () => {
  const pages = ["/companies/upload", "/companies-only-search", "/companies", "/companies/:companyID/frameworks/eutaxonomy"];

  pages.forEach((page) => {
    it(`Test Login Redirect for ${page}`, () => {
      cy.visit(page);
      cy.get("input[name=login]").should("exist").url().should("contain", "keycloak");
    });
  });
});
