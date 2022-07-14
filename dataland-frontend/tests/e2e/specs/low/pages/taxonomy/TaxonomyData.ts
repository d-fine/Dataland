describe("EU Taxonomy Page", function () {
    it("Page should be present", function () {
        cy.restoreLoginSession();
        cy.retrieveDataIdsList().then((dataIdList: any) => {
            cy.visitAndCheckAppMount("/companies/" + dataIdList[2] + "/eutaxonomies");
        });
        cy.get("h2").should("contain", "EU Taxonomy Data");
        const placeholder = "Search company by name or PermID";
        cy.get("input[name=eu_taxonomy_search_bar_standard]")
            .should("not.be.disabled")
            .invoke("attr", "placeholder")
            .should("contain", placeholder);
    });

    it("Type smth into search bar, wait 1 sec, type enter, and expect to see search results on new page", function () {
        cy.restoreLoginSession();
        cy.retrieveDataIdsList().then((dataIdList: any) => {
            cy.visitAndCheckAppMount("/companies/" + dataIdList[2] + "/eutaxonomies");
        });
        cy.get("h2").should("contain", "EU Taxonomy Data");
        const inputValue = "A"
        cy.get("input[name=eu_taxonomy_search_bar_standard]")
            .should("not.be.disabled")
            .click({force: true})
            .type(inputValue)
            .should("have.value", inputValue)
            .wait(1000)
            .type("{enter}")
      cy.url().should("include", "/searchtaxonomy?input=" + inputValue)
        cy.get("h2").should("contain", "Results");
        cy.get("table.p-datatable-table").should("exist");
    });

});
