describe('Data Upload testsuite', () => {
    it('Check if page is present', () => {
        cy.visit("/upload")
        cy.get('#app').should('exist')
    })

})