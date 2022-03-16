describe('User interactive tests for Data Upload', () => {

    beforeEach(() => {
        cy.visit("/upload")

    })

    it('Create a Company with no input', () => {
        cy.get('button[name="submit_2"]').click()
        cy.get('body').contains('Sorry').should('exist')
    })

    it('Create a Company when everything is fine', () => {
        cy.get('input[placeholder="Company Name"]').type("BMW", {force: true})
        cy.get('button[name="submit_2"]').click()
        cy.get('body').contains('Success').should('exist')
    })

    it('Create EU Taxonomy Dataset when everything is fine', () => {
        cy.get('input[name="companyID"]').type("1", {force: true})
        cy.get('button[name="submit_4"]').click()
    })
})