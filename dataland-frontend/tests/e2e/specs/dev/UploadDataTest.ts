import {login} from "../../support/utility";

describe('User interactive tests for Data Upload', () => {
    let companyId:string
    beforeEach(() => {
        login()
    })

    it('Create a Company with no input', () => {
        cy.visit("/upload")
        cy.get('button[name="postCompanyData"]').click()
        cy.get('body').should("contain", "Sorry")
    })

    it('Create a Company when everything is fine', () => {
        cy.visit("/upload")
        const companyName = "Test company"
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
            companyId = $companyID.text()
            cy.visit(`/companies/${companyId}`)
            cy.get('body').should("contain", companyName)
        })
    })

    it('Create EU Taxonomy Dataset with Reporting Obligation and Check the Link', () => {
        cy.visit("/upload")
        cy.get('button[name="postEUData"]', { timeout: 2000 }).should('be.visible')
        cy.get('input[name="companyId"]').type(companyId, {force: true})
        cy.get('input[name="Reporting Obligation"][value=Yes]').check({force: true})
        cy.get('select[name="Attestation"]').select('None')
        for (const argument of ["capex", "opex"]) {
            cy.get(`div[title=${argument}] input`).each(($element, index) => {
                const inputNumber = 10 * index + 7.
                cy.wrap($element).type(inputNumber.toString(), {force: true})
            })
        }
        cy.get('div[title=revenue] input').eq(0).type("0")
        cy.get('div[title=revenue] input').eq(1).type("0")
        cy.get('button[name="postEUData"]', { timeout: 2000 }).should('not.be.disabled')
        cy.get('button[name="postEUData"]').click({force: true})
        cy.get('body').should("contain", "success").should("contain", "EU Taxonomy Data")
        cy.get('span[title=dataId]').then(() => {
            cy.get('span[title=companyId]').then(($companyID) => {
                const companyID = $companyID.text()
                cy.intercept('**/api/data/eutaxonomies/*').as('retrieveTaxonomyData')
                cy.visit(`/companies/${companyID}/eutaxonomies`)
                cy.wait('@retrieveTaxonomyData', {timeout: 120000}).then(() => {
                    cy.get('body').should('contain', 'Eligible Revenue').should("not.contain", "No data has been reported")
                });
            });
        });
    });

    it('Create EU Taxonomy Dataset without Reporting Obligation', () => {
        cy.visit("/upload")
        cy.get('button[name="postEUData"]', { timeout: 2000 }).should('be.visible')
        cy.get('input[name="companyId"]').type(companyId, {force: true})
        cy.get('input[name="Reporting Obligation"][value=No]').check({force: true})
        cy.get('select[name="Attestation"]').select('None')
        cy.get('button[name="postEUData"]', { timeout: 2000 }).should('be.enabled')
        cy.wait(1000)
        cy.get('button[name="postEUData"]').click()
        cy.get('body').should("contain", "success").should("contain", "EU Taxonomy Data")
        cy.get('span[title=dataId]').then(($dataID) => {
            const dataId = $dataID.text()
            cy.intercept('**/api/data/eutaxonomies/*').as('retrieveTaxonomyData')
            cy.visit(`/data/eutaxonomies/${dataId}`)
            cy.wait('@retrieveTaxonomyData', {timeout: 120000}).then(() => {
                cy.get('body')
                    .should("contain", "Eligible Revenue")
                    .should("contain", "No data has been reported")
            });
        });
    });

})
