// add new command to the existing Cypress interface
declare global {
    namespace Cypress {
        interface Chainable {
            retrieveDataIdsList: typeof retrieveDataIdsList
            retrieveCompanyIdsList: typeof retrieveCompanyIdsList
        }
    }
}

function retrieveIdsList(idKey: string, endpoint: string): any {
    cy.request('GET', `${Cypress.env("API")}/${endpoint}`).then((response) => {
        const idsList: Array<string> = []
        for (const item of response.body) {
            idsList.push(item[idKey])
        }
        return idsList
    })
}

export function retrieveDataIdsList(): any {
    return retrieveIdsList("dataId", "metadata")
}

export function retrieveCompanyIdsList(): any {
    return retrieveIdsList("companyId", "companies")
}

Cypress.Commands.add('retrieveDataIdsList', retrieveDataIdsList)
Cypress.Commands.add('retrieveCompanyIdsList', retrieveCompanyIdsList)
