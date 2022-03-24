describe('Index testsuite', () => {
    it('Check if App is present', () => {
        cy.visit("/")
        cy.get('#app').should('exist')
    })
    it('Logo and welcome message are present', () => {
        cy.get('h1').should("have.text","Welcome to DataLand")
        cy.get('img[alt="Dataland logo"]')
            .should('be.visible')
            .should('have.attr', 'src')
            .should('include','dataland-logo')

    })

})


