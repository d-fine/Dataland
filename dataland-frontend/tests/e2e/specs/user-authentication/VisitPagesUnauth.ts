describe("As a user I expect to be redirected to the login page if I am unauthenticated", () => {
  const pages = [
    "/companies/upload",
    "/companies-only-search",
    "/companies",
    "/companies/:companyID/frameworks/eutaxonomy-financials",
    "/companies/:companyID/frameworks/eutaxonomy-non-financials",
    "/companies/:companyID/frameworks/eutaxonomy-non-financials/upload",
    "/companies/:companyID/frameworks/eutaxonomy-financials/upload",
  ];

  pages.forEach((page) => {
    it(`Test Login Redirect for ${page}`, () => {
      cy.visit(page);
      cy.get("input[name=login]").should("exist").url().should("contain", "keycloak");
    });
  });
});
