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
    promise = promise.then(async (): Promise<void> => {
      await Promise.all(chunk.map((element): Promise<void> => processor(element)));
    });
  }
  return cy.then((): Bluebird<string> => {
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
  return new Cypress.Promise((resolve, reject): void => {
    promise
      .then(
        (): void => resolve("done"),
        (reason): void => reject(reason)
      )
      .catch((reason): void => reject(reason));
  });
}
