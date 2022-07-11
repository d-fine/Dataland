import Chainable = Cypress.Chainable;

describe('Paginator test suite', () => {
    beforeEach(() => {
        cy.restoreLoginSession()
    })
    it('Assure existence of paginator for Dax search', () => {
        cy.visit('/searchtaxonomy')
        cy.get('#app').should("exist")
        cy.get('h1').should("contain", "Search EU Taxonomy data")
        cy.get('div.p-paginator').should('exist')
    });


    it('Do a search with 0 matches, then assure that the paginator is gone', () => {
        cy.visit("/searchtaxonomy")
        const inputValueThatWillResultInZeroMatches = "ABCDEFGHIJKLMNOPQRSTUVWXYZ12345678987654321"
        cy.get('input[name=eu_taxonomy_search_input]')
            .should('exist')
            .type(inputValueThatWillResultInZeroMatches)
            .type('{enter}')
            .should('have.value', inputValueThatWillResultInZeroMatches)
        cy.get('div.p-paginator').should('not.exist')
    });
})
