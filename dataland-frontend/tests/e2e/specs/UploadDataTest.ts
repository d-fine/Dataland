describe('User interactive tests for Data Upload', () => {

    beforeEach(() => {
        cy.visit("/upload")

    })

    it('Create a Company with no input', () => {
        cy.get('button[name="postCompanyData"]').click()
        cy.get('body').should("contain", "Sorry")
    })

    it('Create a Company when everything is fine', () => {
        cy.get('input[placeholder="Company Name"]').type("BMW", {force: true})
        cy.get('button[name="postCompanyData"]').click()
        cy.get('body').should("contain", "success")
    })

    it('Create EU Taxonomy Dataset when everything is fine', () => {
        cy.get('input[name="companyID"]').type("1", {force: true})
        cy.get('input[name="Reporting Obligation"][value=Yes]').check({force: true})
        cy.get('select[name="Attestation"]').select('None')
        cy.get('button[name="postEUData"]').click()
        cy.get('body').should("contain", "success")
    })
})
