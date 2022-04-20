describe('Search Taxonomy', function () {
    let companiesData:any
    before(function(){
        cy.fixture('companies').then(function(companies){
            companiesData=companies
        });

    });
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

    it('Company Search by Name', () => {
        cy.visit('/searchtaxonomy')
        const inputValue = companiesData[0].companyName
        cy.get('input[name=eu_taxonomy_search_input]')
            .should('not.be.disabled')
            .click({force:true})
            .type(inputValue)
            .type('{enter}')
            .should('have.value', inputValue)
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
        cy.get('h1').contains(inputValue)
    });

    it('Company Search by Identifier', () => {
        cy.visit('/searchtaxonomy')
        const inputValue = companiesData[1].identifiers[0].identifierValue
        cy.get('input[name=eu_taxonomy_search_input]')
            .should('not.be.disabled')
            .click({force:true})
            .type(inputValue)
            .type('{enter}')
            .should('have.value', inputValue)
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

    it('Search Input field should be always present', () => {
        const placeholder = "Search a company by name"
        const inputValue = "A company name"
        cy.get('input[name=eu_taxonomy_search_input]')
            .should('not.be.disabled')
            .click()
            .type(inputValue)
            .should('have.value', inputValue)
            .invoke('attr', 'placeholder').should('contain', placeholder)
    });

    it('Autocomplete functionality', () => {
        cy.visit('/searchtaxonomy')
        cy.get('input[name=eu_taxonomy_search_input]')
            .click()
            .type('b')
        cy.get('.p-autocomplete-items')
            .eq(0).click()
            .url().should('include', '/companies/')
            .url().should('include', '/eutaxonomies')

    })




});
