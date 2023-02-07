import Bluebird from "cypress/types/bluebird";
import Chainable = Cypress.Chainable;

export const reader_name = "data_reader";
export const reader_pw = getStringCypressEnv("KEYCLOAK_READER_PASSWORD");
export const uploader_name = "data_uploader";
export const uploader_pw = getStringCypressEnv("KEYCLOAK_UPLOADER_PASSWORD");

/**
 * A higher level function that operates on a list of elements (dataArray) and applys a
 * potentially time-intensive operation (processor) to each element. These operations are completed in chunks
 * of chunkSize. The resulting promise is entered into the cypress chain
 *
 * @param dataArray the list of data elements to operate on
 * @param chunkSize the maximum number of operations that should be queued at the same time
 * @param processor a function performing some operation on a single element from the data array
 * @returns the cypress chainable after queueing all operations
 */
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

/**
 * Wraps a browser promise to a cypress promise
 *
 * @param promise the browser promise
 * @returns the converted cypress (Bluebird) promise
 */
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

/**
 * Wraps a browser promise to a cypress promise and enters it into the cypress execution queue
 *
 * @param promise the browser promise to execute in the cypress chain
 * @returns a cypress chainable
 */
export function browserThen<T>(promise: Promise<T>): Chainable<T> {
  return cy.then((): Bluebird<T> => wrapPromiseToCypressPromise(promise));
}

/**
 * Returns the base url from the cypress configuration
 *
 * @returns the cypress baseUrl
 */
export function getBaseUrl(): string {
  const cypressBaseUrl = Cypress.config("baseUrl");
  if (cypressBaseUrl) {
    return cypressBaseUrl;
  }
  throw new Error("Cypress baseUrl is unexpectedly null");
}

/**
 * Checks the presence of a cypress env variable and ensures it is a string before returning it
 * throws an error if the environment variable not a string
 *
 * @param variableName the name of the env variable
 * @returns the string value of the environment variable
 */
export function getStringCypressEnv(variableName: string): string {
  const cypressEnv: unknown = Cypress.env(variableName);
  if (typeof cypressEnv === "string") {
    return cypressEnv;
  }
  throw new Error(`Cypress env ${variableName} is not a string.`);
}
