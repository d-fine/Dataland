import Bluebird from "cypress/types/bluebird";
import Chainable = Cypress.Chainable;

export function doThingsInChunks<T>(
  dataArray: Array<T>,
  chunkSize: number,
  processor: (element: T) => Promise<never>
): Chainable<void | Awaited<T>[]> {
  let promise: Promise<void | Awaited<T>[]> = Promise.resolve();
  for (let i = 0; i < dataArray.length; i += chunkSize) {
    const chunk = dataArray.slice(i, i + chunkSize);
    promise = promise.then(
      (): Promise<Awaited<T>[]> => Promise.all(chunk.map((element): Promise<never> => processor(element)))
    );
  }
  return cy.then((): Bluebird<void | Awaited<T>[]> => {
    return wrapPromiseToCypressPromise(promise);
  });
}

export function wrapPromiseToCypressPromise<T>(promise: Promise<T>): Bluebird<T> {
  return new Cypress.Promise((resolve, reject): void => {
    promise
      .then(
        (result): void => resolve(result),
        (reason): void => reject(reason)
      )
      .catch((reason): void => reject(reason));
  });
}

export function browserThen<T>(promise: Promise<T>): Chainable<T> {
  return cy.then((): Bluebird<T> => wrapPromiseToCypressPromise(promise));
}
