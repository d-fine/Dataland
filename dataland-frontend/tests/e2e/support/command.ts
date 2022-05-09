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
        return response.body.map(function (e: any) {
            e.dataId
        })
    })
}

export function retrieveCompanyIdsList(): any {
    cy.request('GET', `${Cypress.env("API")}/companies`).then((response) => {
        return response.body.map((e: any) => {
            e.companyId
        })
    })
}

Cypress.Commands.add('retrieveDataIdsList', retrieveDataIdsList)
Cypress.Commands.add('retrieveCompanyIdsList', retrieveCompanyIdsList)
