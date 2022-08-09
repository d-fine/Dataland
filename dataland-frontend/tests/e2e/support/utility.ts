import Chainable = Cypress.Chainable;
import Bluebird from "cypress/types/bluebird";

export function doThingsInChunks<T>(
  dataArray: Array<T>,
  chunkSize: number,
  processor: (element: T) => Promise<any>
): Chainable<any> {
  let promise: Promise<any> = Promise.resolve();
  for (let i = 0; i < dataArray.length; i += chunkSize) {
    const chunk = dataArray.slice(i, i + chunkSize);
    promise = promise.then(() => Promise.all(chunk.map((element) => processor(element))));
  }
  return cy.then(() => {
    return wrapPromiseToCypressPromise(promise);
  });
}

export function browserPromiseUploadSingleElementOnce(
  endpoint: string,
  element: object,
  token: string
): Promise<Response> {
  return fetch(`/api/${endpoint}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: "Bearer " + token,
    },
    body: JSON.stringify(element),
  }).then((response) => {
    assert(
      response.status.toString() === "200",
      `Got status code ${response.status.toString()} during upload of single ` +
        `Element to ${endpoint}. Expected: 200.`
    );
    return response;
  });
}

export function uploadSingleElementWithRetries(endpoint: string, element: object, token: string): Promise<Response> {
  return browserPromiseUploadSingleElementOnce(endpoint, element, token)
    .catch((_) => browserPromiseUploadSingleElementOnce(endpoint, element, token))
    .catch((_) => browserPromiseUploadSingleElementOnce(endpoint, element, token));
}

export function getKeycloakToken(
  username: string,
  password: string,
  client_id: string = "dataland-public"
): Chainable<string> {
  return cy
    .request({
      url: "/keycloak/realms/datalandsecurity/protocol/openid-connect/token",
      method: "POST",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      body:
        "username=" +
        encodeURIComponent(username) +
        "&password=" +
        encodeURIComponent(password) +
        "&grant_type=password&client_id=" +
        encodeURIComponent(client_id) +
        "",
    })
    .should("have.a.property", "body")
    .should("have.a.property", "access_token")
    .then((token) => token.toString());
}

export function wrapPromiseToCypressPromise(promise: Promise<any>): Bluebird<any> {
  return new Cypress.Promise((resolve, reject) => {
    promise
      .then(
        () => resolve("done"),
        (reason) => reject(reason)
      )
      .catch((reason) => reject(reason));
  });
}
