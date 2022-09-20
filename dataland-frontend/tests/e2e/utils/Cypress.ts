import Bluebird from "cypress/types/bluebird";
import Chainable = Cypress.Chainable;

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
//
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
