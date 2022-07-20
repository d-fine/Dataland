describe("Test that if unauthenticated will be redirected to login page", () => {
  it("test for each of given paths", () => {
    const pages = ["/upload", "/search", "/searchtaxonomy", "/companies/:companyID/eutaxonomies"];
    pages.forEach((page) => {
      cy.visitAndCheckAppMount(page);
      cy.get("input[name=login]")
          .should("exist")
          .url().should("contain", "keycloak");
    });
  });
});
