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

export function getKeycloakToken(username: string, password: string, client_id: string = "dataland-public") {
    return cy.request(
        {
            url: "/keycloak/realms/datalandsecurity/protocol/openid-connect/token",
            method: "POST",
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: "username=" + encodeURIComponent(username) +
                "&password=" + encodeURIComponent(password) +
                "&grant_type=password&client_id=" + encodeURIComponent(client_id) + ""
        }
    ).then(
        (response) => {
            expect(response.status).to.eq(200)
            return response.body.access_token
        })
}
