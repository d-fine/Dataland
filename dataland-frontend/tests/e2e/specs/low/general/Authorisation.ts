describe('Authentication Buttons', () => {
    it('Checks that normal and logout work' , () => {
        cy.visit("/")
        cy.login()
        cy.logout()
    })

    it('Checks that registering works', () =>  {
        cy.register()
        cy.logout()
    })

})