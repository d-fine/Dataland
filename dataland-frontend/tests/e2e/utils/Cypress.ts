import Bluebird from "cypress/types/bluebird";
import Chainable = Cypress.Chainable;

export const reader_name = "data_reader";
export const reader_pw = getStringCypressEnv("KEYCLOAK_READER_PASSWORD");
export const uploader_name = "data_uploader";
export const uploader_pw = getStringCypressEnv("KEYCLOAK_UPLOADER_PASSWORD");

export function doThingsInChunks<T>(
  dataArray: Array<T>,
  chunkSize: number,
  processor: (element: T) => Promise<void>
): Chainable<void> {
  let promise: Promise<void> = Promise.resolve();
  for (let i = 0; i < dataArray.length; i += chunkSize) {
    const chunk = dataArray.slice(i, i + chunkSize);
    promise = promise.then(
      (): Promise<void> => Promise.all(chunk.map((element): Promise<void> => processor(element))).then()
    );
  }
  return cy.then((): Bluebird<void> => {
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
  throw new Error(`Cypress env ${variableName} is not a string.`);
}
