// add new command to the existing Cypress interface
import Chainable = Cypress.Chainable;
import {getKeycloakToken} from "./utility";

declare global {
    namespace Cypress {
        interface Chainable {
            retrieveDataIdsList: typeof retrieveDataIdsList
            retrieveCompanyIdsList: typeof retrieveCompanyIdsList
            login: typeof login
            restoreLoginSession: typeof restoreLoginSession
            register: typeof register
            logout: typeof logout
        }
    }
}

function retrieveIdsList(idKey: string, endpoint: string): Chainable<Array<string>> {
    return getKeycloakToken("admin_user", "test")
        .then((token) => {
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

export function login(username: string = "some_user", password: string = "test"): Chainable<JQuery> {
    return cy.visit("/")
        .get("button[name='login_dataland_button']").click()
        .get("iframe[name='keycloak-iframe']")
        .its("0.contentWindow.location.href")
        .should(
            "contain",
            "keycloak/realms/datalandsecurity/protocol/openid-connect/auth?client_id=dataland-public"
        )

        .get("iframe[name='keycloak-iframe']")
        .its('0.contentDocument.body')
        .should('not.be.empty')
        .then(cy.wrap)
        .find("#username")
        .should('exist')
        .type(username, {force: true})

        .get("iframe[name='keycloak-iframe']")
        .its('0.contentDocument.body')
        .should('not.be.empty')
        .then(cy.wrap)
        .find("#password")
        .should('exist')
        .type(password, {force: true})

        .get("iframe[name='keycloak-iframe']")
        .its('0.contentDocument.body')
        .should('not.be.empty')
        .then(cy.wrap)
        .find("#kc-login")
        .should('exist')
        .click()

        .get("iframe[name='keycloak-iframe']")
        .should("not.exist")
}
const currentTime=Date.now();
export function register(email: string = "some_user", password: string = "test"): Chainable<JQuery> {
    return cy.visit("/")
        .get("button[name='join_dataland_button']").click()
        .get("iframe[name='keycloak-iframe']")
        .its("0.contentWindow.location.href")
        .should(
            "contain",
            "keycloak/realms/datalandsecurity/protocol/openid-connect/registrations?client_id=dataland-public"
        )

        .get("iframe[name='keycloak-iframe']")
        .its('0.contentDocument.body')
        .should('not.be.empty')
        .then(cy.wrap)
        .find("#email")
        .should('exist')
        .type(email.concat(currentTime.toString()).concat('@dataland.com'), {force: true})

        .get("iframe[name='keycloak-iframe']")
        .its('0.contentDocument.body')
        .should('not.be.empty')
        .then(cy.wrap)
        .find("#password")
        .should('exist')
        .type(password, {force: true})
        .get("iframe[name='keycloak-iframe']")
        .its('0.contentDocument.body')
        .should('not.be.empty')
        .then(cy.wrap)
        .find("#password-confirm")
        .should('exist')
        .type(password, {force: true})

        .get("iframe[name='keycloak-iframe']")
        .its('0.contentDocument.body')
        .should('not.be.empty')
        .then(cy.wrap)
        .find("#kc-form-buttons")
        .should('exist')
        .click()

        .get("iframe[name='keycloak-iframe']")
        .should("not.exist")
}

export function logout(): Chainable<JQuery> {
    return cy.visit("/")
        .get("button[name='logout_dataland_button']").click()
}

export function restoreLoginSession(username?: string, password?: string): Chainable<null> {
    return cy.session([username, password], () => {
        login(username, password)
    })
}

Cypress.Commands.add('retrieveDataIdsList', retrieveDataIdsList)
Cypress.Commands.add('retrieveCompanyIdsList', retrieveCompanyIdsList)
Cypress.Commands.add('login', login)
Cypress.Commands.add('restoreLoginSession', restoreLoginSession)
Cypress.Commands.add('register', register)
Cypress.Commands.add('logout', logout)
