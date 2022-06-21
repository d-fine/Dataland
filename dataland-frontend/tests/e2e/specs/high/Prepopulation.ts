import {doThingsInChunks, getKeycloakToken, uploadSingleElementWithRetries} from "../../support/utility";
import {CompanyInformation, EuTaxonomyData} from "../../../../build/clients/backend/api"

const chunkSize = 40

describe('Population Test',
    () => {
        Cypress.config({
            defaultCommandTimeout: Cypress.env("PREPOPULATE_TIMEOUT_S") * 1000
        })

        let companiesWithData: Array<{ companyInformation: CompanyInformation; euTaxonomyData: EuTaxonomyData }>
        const teaserCompanies: Array<{ companyIds: string }> = []
        let teaserCompaniesPermIds: Array<{ permId: string }> = []

        if (Cypress.env("REALDATA")) {
            teaserCompaniesPermIds = Cypress.env("TEASER_COMPANY_PERM_IDS").cut(',')
        }

        before(function () {
            cy.fixture('CompanyInformationWithEuTaxonomyData').then(function (companies) {
                companiesWithData = companies
            });
        });

        beforeEach(function () {
            cy.restoreLoginSession()
        });

        function addCompanyIdToTeaserCompanies(companyInformation: CompanyInformation, json: any) {
            if (Cypress.env("REALDATA")) {
                for (const identifier of companyInformation.identifiers) {
                    if (identifier.identifierType == "PermId" && teaserCompaniesPermIds.includes(identifier.identifierValue)) {
                        teaserCompanies.push(json.companyId)
                    }
                }
            } else {
                if (teaserCompanies.length == 0) {
                    teaserCompanies.push(json.companyId)
                }
            }
        }

        it('Populate Companies and Eu Taxonomy Data', () => {
            getKeycloakToken("admin_user", "test")
                .then((token) => {
                    doThingsInChunks(
                        companiesWithData,
                        chunkSize,
                        (element) => {
                            return uploadSingleElementWithRetries("companies", element.companyInformation, token).then(response => response.json()).then(
                                (json) => {
                                    uploadSingleElementWithRetries("data/eutaxonomies", {
                                            "companyId": json.companyId,
                                            "data": element.euTaxonomyData
                                        }
                                        , token)
                                    addCompanyIdToTeaserCompanies(element.companyInformation, json);
                                }
                            )
                        }
                    )
                }).should("eq", "done")
        });

        it('Check if the teaser company can be set', () => {
            getKeycloakToken("admin_user", "test")
                .then(token => {
                    // TODO: Hier vielleicht lieber cy.request benutzen!
                    return new Cypress.Promise((resolve, reject) => {
                        try {
                            uploadSingleElementWithRetries("companies/teaser", teaserCompanies, token)
                                .then(() => resolve("done"), (reason) => reject(reason))
                        } catch (e) {
                            reject(e)
                        }

                    });
                });
        });

        it('Check if all the company ids can be retrieved', () => {
            cy.retrieveCompanyIdsList().then((companyIdList: Array<string>) => {
                assert(companyIdList.length >= companiesWithData.length, // >= to avoid problem with several runs in a row
                    `Uploaded ${companyIdList.length} out of ${companiesWithData.length} companies`)
                for (const companyIdIndex in companyIdList) {
                    const companyId = companyIdList[companyIdIndex]
                    assert(typeof companyId !== 'undefined',
                        `Validation of company number ${companyIdIndex}`)
                }
            })
        });

        it('Check if all the data ids can be retrieved', () => {
            cy.retrieveDataIdsList().then((dataIdList: any) => {
                assert(dataIdList.length >= companiesWithData.length, // >= to avoid problem with several runs in a row
                    `Uploaded ${dataIdList.length} out of ${companiesWithData.length} data`)
                for (const dataIdIndex in dataIdList) {
                    assert(typeof dataIdList[dataIdIndex] !== 'undefined',
                        `Validation of data number ${dataIdIndex}`)
                }
            })
        });

        it('Company Name Input field exists and works', () => {
            const inputValue = companiesWithData[0].companyInformation.companyName
            cy.visit("/search")
            cy.get('input[name=companyName]')
                .should('not.be.disabled')
                .type(inputValue, {force: true})
                .should('have.value', inputValue)
            cy.intercept('**/api/companies*').as('retrieveCompany')
            cy.get('button[name=getCompanies]').click()
            cy.wait('@retrieveCompany', {timeout: 60 * 1000}).then(() => {
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
    })
;

describe('EU Taxonomy Data', () => {
    it('Check Eu Taxonomy Data Presence and Link route', () => {
        cy.retrieveDataIdsList().then((dataIdList: Array<string>) => {
            cy.intercept('**/api/data/eutaxonomies/*').as('retrieveTaxonomyData')
            cy.visit("/data/eutaxonomies/" + dataIdList[0])
            cy.wait('@retrieveTaxonomyData', {timeout: 60 * 1000}).then(() => {
                cy.get('h3').should('be.visible')
                cy.get('h3').contains("Revenue")
                cy.get('h3').contains("CapEx")
                cy.get('h3').contains("OpEx")
                cy.get('.d-card').should('contain', 'Eligible')
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
            cy.wait('@retrieveCompany', {timeout: 60 * 1000})
                .wait('@retrieveTaxonomyData', {timeout: 60 * 1000}).then(() => {
                cy.get('h3').should('be.visible')
                cy.get('h3').contains("Revenue")
                cy.get('h3').contains("CapEx")
                cy.get('h3').contains("OpEx")
                cy.get('body').contains("Market Cap:")
                cy.get('body').contains("Headquarter:")
                cy.get('body').contains("Sector:")
                cy.get('input[name=eu_taxonomy_search_input]').should('exist')
            });
        });
    });
});