export function doThingsInChunks<T>(dataArray: Array<T>, chunkSize: number, processor: (element: T)=>void): Promise<any> {
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

export function uploadSingleElementOnce(endpoint: string, element: object): Promise<any> {
    return fetch(`${Cypress.env("API")}/${endpoint}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(element)
    }).then(response => {
        assert(response.status.toString() === "200",
            `Got status code ${response.status.toString()} during upload of single ` +
            `Element to ${endpoint}. Expected: 200.`)
        return response.json()
    })
}

export function uploadSingleElementWithRetries(endpoint: string, element: object): Promise<any> {
    return uploadSingleElementOnce(endpoint, element)
        .catch(_ =>
            uploadSingleElementOnce(endpoint, element))
        .catch(_ =>
            uploadSingleElementOnce(endpoint, element))
}
