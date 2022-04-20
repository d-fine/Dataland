describe.only('User interactive tests for Data Search', () => {

    beforeEach(() => {
        cy.visit("/search")
    })

    it('Skyminder Data Search with no input', () => {
        cy.get('button[name="getSkyminderData"]').click()
        cy.get('body').should("contain", "Sorry")
    })

    it('Skyminder Data Search when everything is fine', () => {
        cy.get('input[name=countryCode]').type("DEU", {force: true})
        cy.get('input[name=companyName]').type("BMW", {force: true})
        cy.get('button[name="getSkyminderData"]').click()
        cy.get('table').should('exist')
    })

})