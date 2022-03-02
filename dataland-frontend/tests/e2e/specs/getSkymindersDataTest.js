describe('User interactive tests', () => {

    beforeEach(() => {
        cy.visit("/")
    })

    it('BMW address should be available upon request by clicking', () => {
        cy.get('input[id="countryCode"]').type("DEU", {force: true})
        cy.get('input[id="companyName"]').type("BMW", {force: true})
        cy.get('button').contains('Get Skyminder by Name').click()
        cy.get('table').should('exist')
        cy.get('table').contains('td', 'Regensburger Str. 420 90480 Nürnberg')
    })

    it('BMW address should be available upon request by pressing enter for countryCode input', () => {
        cy.get('input[id="companyName"]').type("BMW", {force: true})
        cy.get('input[id="countryCode"]').type("DEU{enter}", {force: true})
        cy.get('table').should('exist')
        cy.get('table').contains('td', 'Regensburger Str. 420 90480 Nürnberg')
    })

    it('BMW address should be available upon request by pressing enter for companyName input', () => {
        cy.get('input[id="countryCode"]').type("DEU", {force: true})
        cy.get('input[id="companyName"]').type("BMW{enter}", {force: true})
        cy.get('table').should('exist')
        cy.get('table').contains('td', 'Regensburger Str. 420 90480 Nürnberg')
    })

    it('table should not exist in case of invalid input data', () => {
        cy.get('input[id="countryCode"]').type("DUMMY", {force: true})
        cy.get('input[id="companyName"]').type("DUMMY", {force: true})
        cy.get('button').contains('Get Skyminder by Name').click()
        cy.get('table').should('not.exist')
    })

    it('checks Clear button', () => {
        cy.get('input[id="countryCode"]').type("DEU", {force: true})
        cy.get('input[id="companyName"]').type("BMW", {force: true})
        cy.get('button').contains('Get Skyminder by Name').click()
        cy.get('table').should('exist')
        cy.get('button').contains('Clear').click()
        cy.get('table').should('not.exist')
        cy.get('input[id="countryCode"]').should('be.empty')
        cy.get('input[id="companyName"]').should('be.empty')
    })

})