import apiSpecs from "@/../build/clients/backend/backendOpenApi.json";
const numberOfStockIndices = apiSpecs.components.schemas.CompanyInformation.properties["indices"].items.enum.length
describe('Index Panel behavior', function () {
    const indexPanel = '.p-card > .p-card-body > .p-card-content'
    const indexTabMenu = '.p-tabmenu > .p-tabmenu-nav > .p-tabmenuitem > .p-menuitem-link'
    it('Index panel should be present on first visit and disappear', () => {
        cy.visit("/searchtaxonomy")
        cy.get('.grid')
            .contains('Choose by stock market index')
        cy.get(indexPanel)
            .should('have.length', numberOfStockIndices)
            .eq(1)
            .click()
        cy.get(indexPanel).should('not.exist')
        cy.get(indexTabMenu)
            .should('exist')
            .eq(1)
            .should('have.css', 'color', 'rgb(27, 27, 27)')
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
        cy.get(indexPanel).should('not.exist')
        cy.get(indexTabMenu).should('not.exist')
    });

    it('Index panel should not exist coming from singleton view', () => {
        cy.get('input[name=eu_taxonomy_search_input]')
            .should('not.be.disabled')
            .click()
            .type("ag{enter}")
        cy.get(indexPanel).should('not.exist')
        cy.get(indexTabMenu).should('exist')
    });
    //TODO
    it('Index panel should not be present with params', () => {
        cy.visit("/searchtaxonomy/?input=ag")
        cy.get(indexTabMenu)
            .eq(1)
            .click()
            .parent()
            .should('have.css', 'color', 'rgb(27, 27, 27)')

        cy.get(indexPanel).should('not.exist')
    });


    it('Index tabmenu should be present', () => {
        cy.visit("/searchtaxonomy")
        cy.get(indexTabMenu).should('not.exist')
        cy.get(indexPanel)
            .eq(0).click()
        cy.get('.grid')
            .should('not.contain','Choose by stock market index')
        cy.get(indexTabMenu)
            .should('have.length', numberOfStockIndices)
            .eq(2)
            .click()
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
