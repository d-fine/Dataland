import apiSpecs from "@/../build/clients/backend/backendOpenApi.json";
import Chainable = Cypress.Chainable;

const numberOfStockIndices = apiSpecs.components.schemas.CompanyInformation.properties["indices"].items.enum.length

export function checkIfDaxTabIsHighlighted(): Chainable<JQuery> {
    return cy.get('li[class="p-tabmenuitem p-highlight"]')
        .children('.p-menuitem-link')
        .children('.p-menuitem-text')
        .should('contain', 'DAX')
}

describe('Index Panel behavior', function () {
    const indexTabMenu = '.p-tabmenu > .p-tabmenu-nav > .p-tabmenuitem > .p-menuitem-link'
    beforeEach(() => {
        cy.restoreLoginSession()
    })

    it('Index tabmenu should be present', () => {
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


    it('Visit searchtaxonomy page, scroll to the bottom, back to the top, and check if Dax still highlighted', () => {
        cy.restoreLoginSession()
        cy.visit('/searchtaxonomy')

        checkIfDaxTabIsHighlighted()

        cy.scrollTo('bottom', {duration: 500})
        cy.scrollTo('top', {duration: 500})

        checkIfDaxTabIsHighlighted()
    });
});
