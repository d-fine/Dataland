describe.only('EU Taxonomy Data and Cards', function () {
    let companyIdList:Array<string> = []
    const companyNames:Array<string>  = ["eligible & total", "eligible"]
    it('Create a Companies when everything is fine', () => {
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

    it('Create EU Taxonomy Datasets all data present', () => {
        cy.visit("/upload")
        cy.get('input[name="companyId"]').type(companyIdList[0], {force: true})
        cy.get('input[name="Reporting Obligation"][value=Yes]').check({force: true})
        cy.get('select[name="Attestation"]').select('None')
        for (const argument of ["capex", "opex", "revenue"]) {
            const eligible="0.67"
            const total="15422154"
            cy.get(`div[title=${argument}] input[name=eligible]`).type(eligible)
            cy.get(`div[title=${argument}] input[name=total]`).type(total)
        }
        cy.get('button[name="postEUData"]').click({force: true})
        cy.get('body').should("contain", "success").should("contain", "EU Taxonomy Data")
        cy.get('span[title=dataId]').then(() => {
            cy.get('span[title=companyId]').then(($companyID) => {
                const companyID = $companyID.text()
                cy.visit(`/companies/${companyID}/eutaxonomies`)
                cy.get('body').should('contain', 'Eligible Revenue').should("not.contain", "NaN")
            })
        })
    });

    it('Create EU Taxonomy Datasets only percentages', () => {
        cy.visit("/upload")
        cy.get('input[name="companyId"]').type(companyIdList[1], {force: true})
        cy.get('input[name="Reporting Obligation"][value=Yes]').check({force: true})
        cy.get('select[name="Attestation"]').select('None')
        for (const argument of ["capex", "opex", "revenue"]) {
            const eligible="0.67"
            cy.get(`div[title=${argument}] input[name=eligible]`).type(eligible)
        }
        cy.get('button[name="postEUData"]').click({force: true})
        cy.get('body').should("contain", "success").should("contain", "EU Taxonomy Data")
        cy.get('span[title=dataId]').then(() => {
            cy.get('span[title=companyId]').then(($companyID) => {
                const companyID = $companyID.text()
                cy.visit(`/companies/${companyID}/eutaxonomies`)
                cy.get('body').should('contain', 'Eligible Revenue').should("not.contain", "NaN")
            })
        })
    });
});

