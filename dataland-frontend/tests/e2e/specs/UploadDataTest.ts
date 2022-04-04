describe('User interactive tests for Data Upload', () => {

    beforeEach(() => {
        cy.visit("/upload")

    })

    it('Create a Company with no input', () => {
        cy.get('button[name="postCompanyData"]').click()
        cy.get('body').should("contain", "Sorry")
    })

    it('Create a Company when everything is fine', () => {
        const companyName = "BMW"
        cy.get('input[name=companyName]').type(companyName, {force: true})
        cy.get('button[name="postCompanyData"]').click()
        cy.get('body').should("contain", "success")
        cy.get('span[title=companyId]').then(($companyID) => {
            const id = $companyID.text()
            cy.visit(`/companies/${id}`)
            cy.get('body').should("contain", companyName)
        })
    })

    it('Create EU Taxonomy Dataset with Reporting Obligation', () => {
        cy.get('input[name="companyId"]').type("1", {force: true})
        cy.get('input[name="Reporting Obligation"][value=Yes]').check({force: true})
        cy.get('select[name="Attestation"]').select('None')
        for (const argument of ["capex", "opex", "revenue"]) {
            cy.get(`div[title=${argument}] input`).each(($element, index) => {
                const inputNumber = 10 * index + 7.
                cy.wrap($element).type(inputNumber.toString(), {force: true})
            })
        }
        cy.get('button[name="postEUData"]').click({force: true})
        cy.get('body').should("contain", "success").should("contain", "EU Taxonomy Data")
        cy.get('span[title=dataId]').then(($dataID) => {
            const id = $dataID.text()
            cy.visit(`/data/eutaxonomies/${id}`).get('body').should("contain", "Eligible Revenue").should("not.contain", "NaN")
        })
    })

    it('Create EU Taxonomy Dataset without Reporting Obligation', () => {
        cy.get('input[name="companyId"]').type("1", {force: true})
        cy.get('input[name="Reporting Obligation"][value=No]').check({force: true})
        cy.get('select[name="Attestation"]').select('None')
        cy.wait(1000)
        cy.get('button[name="postEUData"]').click({force: true})
        cy.get('body').should("contain", "success").should("contain", "EU Taxonomy Data")
        cy.get('span[title=dataId]').then(($dataID) => {
            const id = $dataID.text()
            cy.visit(`/data/eutaxonomies/${id}`).get('body').should("contain", "Eligible Revenue").should("contain", "NaN")
        })
    });



})
