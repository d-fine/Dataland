describe('User interactive tests for Data Upload', () => {

    beforeEach(() => {
        cy.visit("/upload")

    })

    it('Create a Company with no input', () => {
        cy.get('button[name="postCompanyData"]').click()
        cy.get('body').should("contain", "Sorry")
    })

    it('Create a Company when everything is fine', () => {
        cy.get('input[name=companyName]').type("BMW", {force: true})
        cy.get('button[name="postCompanyData"]').click()
        cy.get('body').should("contain", "success")
    })

    it('Create EU Taxonomy Dataset without Reporting Obligation',
        () => {
            cy.get('input[name="companyId"]').type("1", {force: true})
            cy.get('input[name="Reporting Obligation"][value=No]').check({force: true})
            cy.get('select[name="Attestation"]').select('None')
            cy.get('button[name="postEUData"]').click()
            cy.get('h4').contains('success').contains('EU Taxonomy Data')
            cy.visit("/eutaxonomies/1").get('body').should("contain", "Dataset: 1")
            cy.go('back')
        })
})
