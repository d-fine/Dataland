export function doThingsInChunks<T>(dataArray: Array<T>, chunkSize: number, processor: (element: T) => void): Promise<any> {
    let promise: Promise<any> = Promise.resolve()
    for (let i = 0; i < dataArray.length; i += chunkSize) {
        const chunk = dataArray.slice(i, i + chunkSize);
        promise = promise.then(
            () => Promise.all(chunk.map(element =>
                processor(element)
            ))
        )
    }
    return promise
}

export function uploadSingleElementOnce(endpoint: string, element: object, token: string): Promise<any> {
    return fetch(`${Cypress.env("API")}/${endpoint}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + token
        },
        body: JSON.stringify(element)
    }).then(response => {
        assert(response.status.toString() === "200",
            `Got status code ${response.status.toString()} during upload of single ` +
            `Element to ${endpoint}. Expected: 200.`)
        return response.json()
    })
}

export function uploadSingleElementWithRetries(endpoint: string, element: object, token: string): Promise<any> {
    return uploadSingleElementOnce(endpoint, element, token)
        .catch(_ =>
            uploadSingleElementOnce(endpoint, element, token))
        .catch(_ =>
            uploadSingleElementOnce(endpoint, element, token))
}

export async function getKeycloakToken(username: string, password: string, client_id: string = "dataland-public") {
    return await fetch(
        "http://localhost/keycloak/realms/datalandsecurity/protocol/openid-connect/token",
        {
            method: "POST",
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: "username=" + encodeURIComponent(username) +
                "&password=" + encodeURIComponent(password) +
                "&grant_type=password&client_id=" + encodeURIComponent(client_id) + ""
        }
    ).then(
        (response) => response.json()
    ).then(
        response_json => response_json.access_token
    )
}

export function login(username: string = "some_user", password: string = "test"): void {
    cy.visit("/")
    cy.get("button[name='login_dataland_button']").click()
    cy.get("iframe[name='keycloak-iframe']")
        .its("0.contentWindow.location.href")
        .should(
            "contain",
            "keycloak/realms/datalandsecurity/protocol/openid-connect/auth?client_id=dataland-public"
        )

    cy.get("iframe[name='keycloak-iframe']")
        .its('0.contentDocument.body')
        .should('not.be.empty')
        .then(cy.wrap)
        .find("#username")
        .should('exist')
        .type(username, {force: true})

    cy.get("iframe[name='keycloak-iframe']")
        .its('0.contentDocument.body')
        .should('not.be.empty')
        .then(cy.wrap)
        .find("#password")
        .should('exist')
        .type(password, {force: true})
    cy.get("iframe[name='keycloak-iframe']")
        .its('0.contentDocument.body')
        .should('not.be.empty')
        .then(cy.wrap)
        .find("#kc-login")
        .should('exist')
        .click()

    cy.get("iframe[name='keycloak-iframe']")
        .should("not.exist")
}