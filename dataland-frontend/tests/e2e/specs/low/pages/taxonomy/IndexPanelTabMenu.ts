import apiSpecs from "@/../build/clients/backend/backendOpenApi.json";
import {login} from "../../../../support/utility";
const numberOfStockIndices = apiSpecs.components.schemas.CompanyInformation.properties["indices"].items.enum.length
describe('Index Panel behavior', function () {
    const indexTabMenu = '.p-tabmenu > .p-tabmenu-nav > .p-tabmenuitem > .p-menuitem-link'
    beforeEach(()=> {
        login()
    })

    it('Index tabmenu should be present on first visit', () => {
        cy.visit("/searchtaxonomy")
        cy.get('.p-tabmenuitem').should('have.length', numberOfStockIndices)
        cy.get(indexTabMenu)
            .should('exist')
            .eq(1).parent('.p-tabmenuitem')
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
        cy.get(indexTabMenu).should('not.exist')
    });



    it('Index tabmenu should be present', () => {
        cy.visit("/searchtaxonomy")
        cy.get(indexTabMenu).should('exist')
        cy.get('.grid')
            .should('not.contain','Choose by stock market index')
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
