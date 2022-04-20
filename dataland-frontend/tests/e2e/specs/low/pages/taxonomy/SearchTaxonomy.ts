describe('Search Taxonomy', function () {
    it('page should be present', function () {
        cy.visit("/searchtaxonomy")
        cy.get('#app').should("exist")
    });
    it('Heading should be present', () => {
        cy.get('h1').should("contain", "Search EU Taxonomy data")
    });

    it('Search Input field should be present before index filter', () => {
        const placeholder = "Search a company by name"
        const inputValue = "A company name"
        cy.get('input[name=eu_taxonomy_search_input]')
            .should('not.be.disabled')
            .click({force:true})
            .type(inputValue)
            .should('have.value', inputValue)
            .invoke('attr', 'placeholder').should('contain', placeholder)
    });



    it('Search Input field should be present after index filter', () => {
        const placeholder = "Search a company by name"
        const inputValue = "A company name"
        cy.get('input[name=eu_taxonomy_search_input]')
            .should('not.be.disabled')
            .click()
            .type(inputValue)
            .should('have.value', inputValue)
            .invoke('attr', 'placeholder').should('contain', placeholder)
    });




});
