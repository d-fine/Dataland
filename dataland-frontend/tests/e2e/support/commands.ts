// add new command to the existing Cypress interface
declare global {
    namespace Cypress {
        interface Chainable {
            retrieveDataIdsList: typeof retrieveDataIdsList
            retrieveCompanyIdsList: typeof retrieveCompanyIdsList
        }
    }
}

export function retrieveDataIdsList(): any {
    cy.request('GET', `${Cypress.env("API")}/metadata`).then((response) => {
        const dataIdsList = []
        for (const data of response.body) {
            dataIdsList.push(data.dataId)
        }
        return dataIdsList
    })
}

export function retrieveCompanyIdsList(): any {
    cy.request('GET', `${Cypress.env("API")}/companies`).then((response) => {
        const companyIdsList = []
        for (const company of response.body) {
            companyIdsList.push(company.companyId)
        }
        return companyIdsList
    })
}

Cypress.Commands.add('retrieveDataIdsList', retrieveDataIdsList)
Cypress.Commands.add('retrieveCompanyIdsList', retrieveCompanyIdsList)
