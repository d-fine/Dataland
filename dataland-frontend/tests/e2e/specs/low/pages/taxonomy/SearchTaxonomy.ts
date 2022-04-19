import apiSpecs from "@/../build/clients/backend/backendOpenApi.json";
const numberOfStockIndices = apiSpecs.components.schemas.CompanyInformation.properties["indices"].items.enum.length
describe.only('Search Taxonomy', function () {
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

    it('Index panel should be present', () => {
        cy.visit("/searchtaxonomy")
        cy.get('.grid')
            .contains('Choose by stock market index')
        cy.get('.p-card > .p-card-body > .p-card-content')
            .should('have.length', numberOfStockIndices)
            .eq(1).click()
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

    it('Index tabmenu should be present', () => {
        cy.visit("/searchtaxonomy")
        cy.get('.p-card > .p-card-body > .p-card-content')
            .eq(0).click()
        cy.get('.grid')
            .should('not.contain','Choose by stock market index')
        cy.get('.p-tabmenu > .p-tabmenu-nav > .p-tabmenuitem')
            .should('have.length', numberOfStockIndices)
            .eq(2).click()
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
    })

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
