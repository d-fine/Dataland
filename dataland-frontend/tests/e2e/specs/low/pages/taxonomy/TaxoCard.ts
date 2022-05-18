describe('EU Taxonomy Data and Cards', function () {
    let companyIdList:Array<string> = []
    const companyNames:Array<string>  = ["eligible & total", "eligible"]
    it('Create a Company providing only valid data', () => {
        companyNames.forEach((companyName) => {
            cy.visit("/upload")
            cy.get('input[name=companyName]').type(companyName, {force: true})
            cy.get('input[name=headquarters]').type("applications", {force: true})
            cy.get('input[name=sector]').type("Handmade", {force: true})
            cy.get('input[name=marketCap]').type("123", {force: true})
            cy.get('input[name=reportingDateOfMarketCap]').type("2021-09-02", {force: true})
            cy.get('select[name=identifierType]').select('ISIN')
            cy.get('input[name=identifierValue]').type("IsinValueId", {force: true})
            cy.get('button[name="postCompanyData"]').click()
            cy.get('body').should("contain", "success")
            cy.get('span[title=companyId]').then(($companyID) => {
                const id = $companyID.text()
                companyIdList.push(id)
                cy.visit(`/companies/${id}`)
                cy.get('body').should("contain", companyName)
            })
        })
    });

    it('Create a EU Taxonomy Dataset via upload form with total(€) and eligible(%) numbers', () => {
        const eligible=0.67
        const total="15422154"
        cy.visit("/upload")
        cy.get('input[name="companyId"]').type(companyIdList[0], {force: true})
        cy.get('input[name="Reporting Obligation"][value=Yes]').check({force: true})
        cy.get('select[name="Attestation"]').select('None')
        for (const argument of ["capex", "opex", "revenue"]) {
            cy.get(`div[title=${argument}] input[name=eligiblePercentage]`).type(eligible.toString())
            cy.get(`div[title=${argument}] input[name=totalAmount]`).type(total)
        }
        cy.get('button[name="postEUData"]').click({force: true})
        cy.get('body').should("contain", "success").should("contain", "EU Taxonomy Data")
        cy.get('span[title=dataId]').then(() => {
            cy.get('span[title=companyId]').then(($companyID) => {
                const companyID = $companyID.text()
                cy.intercept('**/api/data/eutaxonomies/*').as('retrieveTaxonomyData')
                cy.visit(`/companies/${companyID}/eutaxonomies`)
                cy.wait('@retrieveTaxonomyData', {timeout: 120000}).then(() => {
                    cy.get('body').should('contain', 'Eligible Revenue').should("contain", `Out of total of`)
                    cy.get('body').should('contain', 'Eligible Revenue').should("contain", `${100 * eligible}%`)
                    cy.get('.font-semibold.text-lg').should('contain', '€')
                })
            })
        })
    });

    it('Create a EU Taxonomy Dataset via upload form with only eligible(%) numbers', () => {
        const eligible=0.67
        cy.visit("/upload")
        cy.get('input[name="companyId"]').type(companyIdList[1], {force: true})
        cy.get('input[name="Reporting Obligation"][value=Yes]').check({force: true})
        cy.get('select[name="Attestation"]').select('None')
        for (const argument of ["capex", "opex", "revenue"]) {
            cy.get(`div[title=${argument}] input[name=eligiblePercentage]`).type(eligible.toString())
        }
        cy.get('button[name="postEUData"]').click({force: true})
        cy.get('body').should("contain", "success").should("contain", "EU Taxonomy Data")
        cy.get('span[title=dataId]').then(() => {
            cy.get('span[title=companyId]').then(($companyID) => {
                const companyID = $companyID.text()
                cy.intercept('**/api/data/eutaxonomies/*').as('retrieveTaxonomyData')
                cy.visit(`/companies/${companyID}/eutaxonomies`)
                cy.wait('@retrieveTaxonomyData', {timeout: 120000}).then(() => {
                    cy.get('body').should('contain', 'Eligible OpEx').should("contain", `${100 * eligible}%`)
                    cy.get('body').should('contain', 'Eligible Revenue').should("not.contain", `Out of total of`)
                    cy.get('.font-semibold.text-lg').should('not.exist')
                })
            })
        })
    });
});

