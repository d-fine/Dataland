describe('User interactive tests', () => {

    beforeEach(() => {
        cy.visit("/")
        cy.get('input[id="countryCode"]').type("DEU", {force: true})
        cy.get('input[id="companyName"]').type("BMW", {force: true})
    })

    it('BMW address should be available upon request by clicking', () => {
        cy.get('button').contains('Get Skyminder by Name').click()
        cy.get('table').should('exist')
        cy.get('table').contains('td', 'Teststr. 1 123456 Teststadt')
    })

    it('BMW address should be available upon request by pressing enter for countryCode input', () => {
        cy.get('input[id="countryCode"]').type("{enter}", {force: true})
        cy.get('table').should('exist')
        cy.get('table').contains('td', 'Teststr. 1 123456 Teststadt')
    })

    it('BMW address should be available upon request by pressing enter for companyName input', () => {
        cy.get('input[id="companyName"]').type("{enter}", {force: true})
        cy.get('table').should('exist')
        cy.get('table').contains('td', 'Teststr. 1 123456 Teststadt')
    })

    it('checks Clear button', () => {
        cy.get('button').contains('Get Skyminder by Name').click()
        cy.get('table').should('exist')
        cy.get('button').contains('Clear').click()
        cy.get('table').should('not.exist')
        cy.get('input[id="countryCode"]').should('be.empty')
        cy.get('input[id="companyName"]').should('be.empty')
    })

})