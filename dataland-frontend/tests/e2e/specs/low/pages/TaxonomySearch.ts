describe.only('EU Taxonomy Page', function () {
    it('page should be present', function () {
        cy.visit("/searchtaxonomy")
        cy.get('#app').should("exist")
    });
    it('Heading is present', () => {
        cy.get('h1').should("contain", "Search EU Taxonomy data")
    });
    it('Search Input field should be present', () => {
        const placeholder = "Search by company name"
        cy.get('input[name=eu_taxonomy_search_input]')
            .should('be.disabled')
            .invoke('attr', 'placeholder').should('contain', placeholder)
    });
    it('Should have button to search all entries', () => {
        cy.get('.p-button.p-component.uppercase')
            .should('not.be.disabled')
            .should('contain', 'Search')
            .click({force: true})
        cy.get('h2')
            .should('contain', "Results")
        cy.get('table.p-datatable-table').should('exist')
        cy.get('table.p-datatable-table').contains('th','COMPANY')
        cy.get('table.p-datatable-table').contains('th','SECTOR')
        cy.get('table.p-datatable-table').contains('th','MARKET CAP')
        cy.get('table.p-datatable-table').contains('td','VIEW')
            .contains('a', 'VIEW')
            .click()
            .url().should('include', '/companies/')
            .url().should('include', '/eutaxonomies')
    });
});
