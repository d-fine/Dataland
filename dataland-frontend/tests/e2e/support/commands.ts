// add new command to the existing Cypress interface
import Chainable = Cypress.Chainable;

declare global {
    namespace Cypress {
        interface Chainable {
            retrieveDataIdsList: typeof retrieveDataIdsList
            retrieveCompanyIdsList: typeof retrieveCompanyIdsList
        }
    }
}

function retrieveIdsList(idKey: string, endpoint: string): Chainable<Array<String>> {
    return cy.request('GET', `${Cypress.env("API")}/${endpoint}`).then((response) => {
        return response.body.map((e:any) => e[idKey])
    })
}

export function retrieveDataIdsList(): Chainable<Array<String>> {
    return retrieveIdsList("dataId", "metadata")
}

export function retrieveCompanyIdsList(): Chainable<Array<String>> {
    return retrieveIdsList("companyId", "companies")
}

Cypress.Commands.add('retrieveDataIdsList', retrieveDataIdsList)
Cypress.Commands.add('retrieveCompanyIdsList', retrieveCompanyIdsList)
