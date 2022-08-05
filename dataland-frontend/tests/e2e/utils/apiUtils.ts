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

function retrieveIdsList(idKey: string, endpoint: string): Chainable<Array<string>> {
  return cy.getKeycloakToken("data_uploader", Cypress.env("KEYCLOAK_UPLOADER_PASSWORD"))
    .then((token) => {
      return cy.request({
        url: `${Cypress.env("API")}/${endpoint}`,
        method: "GET",
        headers: { Authorization: "Bearer " + token },
      });
    })
    .then((response) => {
      return response.body.map((e: any) => e[idKey]);
    });
}

export function retrieveDataIdsList(): Chainable<Array<string>> {
  return retrieveIdsList("dataId", "metadata");
}

export function retrieveCompanyIdsList(): Chainable<Array<string>> {
  return retrieveIdsList("companyId", "companies");
}
