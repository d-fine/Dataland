import apiSpecs from "@/../build/clients/backend/backendOpenApi.json";
const numberOfStockIndices = apiSpecs.components.schemas.CompanyInformation.properties["indices"].items.enum.length
describe.only('Index Panel behavior', function () {
    it('Index panel should be present on first visit and disappear', () => {
        cy.visit("/searchtaxonomy")
        cy.get('.grid')
            .contains('Choose by stock market index')
        cy.get('.p-card > .p-card-body > .p-card-content')
            .should('have.length', numberOfStockIndices)
            .eq(1).click()
        cy.get('.p-card > .p-card-body > .p-card-content').should('not.exist')
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

    it('Index panel should not be present with params', () => {
        cy.visit("/searchtaxonomy/?input=ag")
        cy.get('.p-card > .p-card-body > .p-card-content').should('not.exist')
    })





});
