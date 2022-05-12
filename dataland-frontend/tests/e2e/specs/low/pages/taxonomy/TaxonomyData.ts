describe('EU Taxonomy Page', function () {
    it('page should be present', function () {
        cy.retrieveDataIdsList().then((dataIdList: any) => {
            cy.visit("/companies/" + dataIdList[2] + "/eutaxonomies")
            cy.get('#app').should("exist")
        });
    });
    it('Heading is present', () => {
        cy.get('h2').should("contain", "EU Taxonomy Data")
    });
    it('Search Input field should be present', () => {
        const placeholder = "Search a company by name, ISIN, PermID or LEI"
        cy.get('input[name=eu_taxonomy_search_input]')
            .should('not.be.disabled')
            .invoke('attr', 'placeholder').should('contain', placeholder)
    });
});
