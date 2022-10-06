import Bluebird from "cypress/types/bluebird";
import Chainable = Cypress.Chainable;

export function doThingsInChunks<T>(
  dataArray: Array<T>,
  chunkSize: number,
  processor: (element: T) => Promise<void>
): Chainable<string> {
  let promise: Promise<void> = Promise.resolve();
  for (let i = 0; i < dataArray.length; i += chunkSize) {
    const chunk = dataArray.slice(i, i + chunkSize);
    promise = promise.then(async () => {
      await Promise.all(chunk.map((element) => processor(element)));
    });
  }
  return cy.then(() => {
    return wrapPromiseToCypressPromise(promise);
  });
}

export function getBaseUrl(): string {
  const cypressBaseUrl = Cypress.config("baseUrl");
  if (cypressBaseUrl) {
    return cypressBaseUrl;
  }
  throw new Error("Cypress baseUrl is unexpectedly null");
}

export function getStringCypressEnv(variableName: string): string {
  const cypressEnv: unknown = Cypress.env(variableName);
  if (typeof cypressEnv === "string") {
    return cypressEnv;
  }
  throw new Error(`Expected cypress env ${variableName} to be a string. It's not`);
}

export function wrapPromiseToCypressPromise<T>(promise: Promise<T>): Bluebird<string> {
  return new Cypress.Promise((resolve, reject) => {
    promise
      .then(
        () => resolve("done"),
        (reason) => reject(reason)
      )
      .catch((reason) => reject(reason));
  });
}
