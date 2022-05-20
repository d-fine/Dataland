import {doThingsInChunks, uploadSingleElementWithRetries} from "../../support/utility";

const chunkSize = 40
describe('Population Test',
    () => {
        Cypress.config({
            defaultCommandTimeout: 900 * 1000
        })

        let eutaxonomiesData: any
        let companiesData: any
        const companyAssociatedEuTaxonomyData: any = []

        before(function () {
            cy.fixture('EuTaxonomyData').then(function (eutaxonomies) {
                eutaxonomiesData = eutaxonomies
            });
            cy.fixture('CompanyInformation').then(function (companies) {
                companiesData = companies
            });
        });

        it('Populate Companies', async () => {
            await doThingsInChunks(
                companiesData,
                chunkSize,
                (element: object) => uploadSingleElementWithRetries("companies", element)
            )
        });

        it('Check if all the company ids can be retrieved', () => {
            cy.retrieveCompanyIdsList().then((companyIdList: any) => {
                assert(companyIdList.length >= companiesData.length, // >= to avoid problem with several runs in a row
                    `Uploaded ${companyIdList.length} out of ${companiesData.length} companies`)
                for (const companyIdIndex in companyIdList) {
                    const companyId = companyIdList[companyIdIndex]
                    assert(typeof companyId !== 'undefined',
                        `Validation of company number ${companyIdIndex}`)
                    if (typeof eutaxonomiesData[companyIdIndex] == "object") {
                        companyAssociatedEuTaxonomyData.push({
                            "companyId": companyId,
                            "data": eutaxonomiesData[companyIdIndex]
                        })
                    }
                }
            })
        });

        it('Populate EU Taxonomy Data', async () => {
            await doThingsInChunks(
                companyAssociatedEuTaxonomyData,
                chunkSize,
                (element: object) => uploadSingleElementWithRetries("data/eutaxonomies", element)
            )
        });

        it('Check if all the data ids can be retrieved', () => {
            cy.retrieveDataIdsList().then((dataIdList: any) => {
                assert(dataIdList.length >= eutaxonomiesData.length, // >= to avoid problem with several runs in a row
                    `Uploaded ${dataIdList.length} out of ${eutaxonomiesData.length} data`)
                for (const dataIdIndex in dataIdList) {
                    assert(typeof dataIdList[dataIdIndex] !== 'undefined',
                        `Validation of data number ${dataIdIndex}`)
                }
            })
        });
    });

describe('EU Taxonomy Data', () => {
    it('Check Eu Taxonomy Data Presence and Link route', () => {
        cy.retrieveDataIdsList().then((dataIdList: Array<string>) => {
            cy.intercept('**/api/data/eutaxonomies/*').as('retrieveTaxonomyData')
            cy.visit("/data/eutaxonomies/" + dataIdList[0])
            cy.wait('@retrieveTaxonomyData', {timeout: 60000}).then(() => {
                cy.get('h3', {timeout: 90 * 1000}).should('be.visible')
                cy.get('h3').contains("Revenue")
                cy.get('h3').contains("CapEx")
                cy.get('h3').contains("OpEx")
                cy.get('.d-card').should('contain', 'Eligible')
                cy.get('.d-card .p-progressbar').should('exist')
            });
        });
    })
});

describe('Company EU Taxonomy Data', () => {
    it('Check Company associated EU Taxonomy Data Presence and Link route', () => {
        cy.retrieveCompanyIdsList().then((companyIdList: Array<string>) => {
            cy.intercept('**/api/companies/*').as('retrieveCompany')
            cy.intercept('**/api/data/eutaxonomies/*').as('retrieveTaxonomyData')
            cy.visit(`/companies/${companyIdList[0]}/eutaxonomies`)
            cy.wait('@retrieveCompany', {timeout: 60000})
                .wait('@retrieveTaxonomyData', {timeout: 60000}).then(() => {
                cy.get('h3').should('be.visible')
                cy.get('h3').contains("Revenue")
                cy.get('h3').contains("CapEx")
                cy.get('h3').contains("OpEx")
                cy.get('body').contains("Market Cap:")
                cy.get('body').contains("Headquarter:")
                cy.get('body').contains("Sector:")
                cy.get('.grid.align-items-end.text-left').contains('Financial Data 2021')
                cy.get('.grid.align-items-end.text-left').contains('Sustainability Data 2021')
                cy.get('input[name=eu_taxonomy_search_input]').should('exist')
            });
        });
    });
});

describe('Company Data', () => {
    let companiesData: any
    before(function () {
        cy.fixture('CompanyInformation').then(function (companies) {
            companiesData = companies
        });

    });
    it('Company Name Input field exists and works', () => {
        const inputValue = companiesData[0].companyName
        cy.visit("/search")
        cy.get('input[name=companyName]')
            .should('not.be.disabled')
            .type(inputValue, {force: true})
            .should('have.value', inputValue)
        cy.intercept('**/api/companies*').as('retrieveCompany')
        cy.get('button[name=getCompanies]').click()
        cy.wait('@retrieveCompany', {timeout: 60000}).then(() => {
            cy.get('td').contains("VIEW")
                .contains('a', 'VIEW')
                .click().url().should('include', '/companies/')
        })
    });

    it('Show all companies button exists', () => {
        cy.visit("/search")
        cy.get('button.p-button').contains('Show all companies')
            .should('not.be.disabled')
            .click()
    });
});