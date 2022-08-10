describe("Test that if unauthenticated will be redirected to login page", () => {
  it("test for each of given paths", () => {
    const pages = ["/companies/:companyID/frameworks/eutaxonomy-non-financials/upload", "/companies-only-search", "/search/eutaxonomy", "/companies/:companyID/frameworks/eutaxonomy-non-financials"];
    pages.forEach((page) => {
      cy.visit(page);
      cy.get("input[name=login]").should("exist").url().should("contain", "keycloak");
    });
  });
});
