// add new command to the existing Cypress interface
import Chainable = Cypress.Chainable;
import {getKeycloakToken} from "./utility";

declare global {
    namespace Cypress {
        interface Chainable {
            retrieveDataIdsList: typeof retrieveDataIdsList
            retrieveCompanyIdsList: typeof retrieveCompanyIdsList
        }
    }
}

function retrieveIdsList(idKey: string, endpoint: string): Chainable<Array<string>> {
    return cy.wrap(null).then(async () => {
            return await getKeycloakToken("admin_user", "test")
        }
    ).then((token) => {
        return cy.request({
            url: `${Cypress.env("API")}/${endpoint}`,
            method: 'GET',
            headers: {"Authorization": "Bearer " + token}
        })
    }).then((response) => {
        return response.body.map((e: any) => e[idKey])
    })
}

export function retrieveDataIdsList(): Chainable<Array<string>> {
    return retrieveIdsList("dataId", "metadata")
}

export function retrieveCompanyIdsList(): Chainable<Array<string>> {
    return retrieveIdsList("companyId", "companies")
}

Cypress.Commands.add('retrieveDataIdsList', retrieveDataIdsList)
Cypress.Commands.add('retrieveCompanyIdsList', retrieveCompanyIdsList)
