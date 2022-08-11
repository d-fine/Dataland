describe("Test that if unauthenticated will be redirected to login page", () => {
    it("test for each of given paths", () => {
        cy.retrieveCompanyIdsList()
            .then((allCompanyIdsList: Array<string>) => {
                const someCompanyId = allCompanyIdsList[0]
                const pages = [`/companies/${someCompanyId}/frameworks/eutaxonomy-non-financials/upload`, `/companies/${someCompanyId}/frameworks/eutaxonomy`, "/companies-only-search", "/search/eutaxonomy"];
                pages.forEach((page) => {
                    cy.visit(page);
                    cy.get("input[name=login]").should("exist").url().should("contain", "keycloak");
                })
            });
    });
});
