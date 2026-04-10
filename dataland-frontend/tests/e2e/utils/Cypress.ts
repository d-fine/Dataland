// @ts-ignore: Cypress types are internal;
import type Bluebird from 'cypress/types/bluebird';

export const reader_name = 'data_reader';
export const reader_userId = '18b67ecc-1176-4506-8414-1e81661017ca';

export const uploader_name = 'data_uploader';
export const uploader_userId = 'c5ef10b1-de23-4a01-9005-e62ea226ee83';

export const reviewer_name = 'data_reviewer';
export const reviewer_userId = 'f7a02ff1-0dab-4e10-a908-7d775c1014ae';

export const judge_name = 'data_judge';
export const judge_userId = '375c4c42-fa50-4f7d-af69-450803fe0ca1';

export const premium_user_name = 'data_premium_user';
export const premium_user_userId = '68129cce-52e5-473e-bec9-90046eebc619';

export const admin_name = 'data_admin';
export const admin_userId = '136a9394-4873-4a61-a25b-65b1e8e7cc2f';

/**
 * Checks the presence of a Cypress env variable and ensures it is a string before returning it
 * throws an error if the environment variable not a string
 * @param variableName the name of the env variable
 * @returns Cypress.Chainable<string> the string value of the environment variable
 */
export function getStringCypressEnv(variableName: string): Cypress.Chainable<string> {
  return cy.env([variableName]).then((vars) => {
    const v = vars as Record<string, unknown>;
    const value = v[variableName];

    if (typeof value === 'string') {
      return value;
    }

    throw new Error(`Cypress env ${variableName} is not a string.`);
  });
}

/**
 * Retrieves the password for the reader user from the Cypress environment variables.
 * @returns a Cypress string chainable containing the reader password
 */
export function getReaderPw(): Cypress.Chainable<string> {
  return getStringCypressEnv('KEYCLOAK_READER_PASSWORD');
}
/**
 * Retrieves the password for the uploader user from the Cypress environment variables.
 * @returns a Cypress string chainable containing the uploader password
 */
export function getUploaderPw(): Cypress.Chainable<string> {
  return getStringCypressEnv('KEYCLOAK_UPLOADER_PASSWORD');
}
/**
 * Retrieves the password for the reviewer user from the Cypress environment variables.
 * @returns a Cypress string chainable containing the reviewer password
 */
export function getReviewerPw(): Cypress.Chainable<string> {
  return getStringCypressEnv('KEYCLOAK_REVIEWER_PASSWORD');
}
/**
 * Retrieves the password for the judge user from the Cypress environment variables.
 * @returns a Cypress string chainable containing the judge password
 */
export function getJudgePw(): Cypress.Chainable<string> {
  return getStringCypressEnv('KEYCLOAK_JUDGE_PASSWORD');
}
/**
 * Retrieves the password for the premium user from the Cypress environment variables.
 * @returns a Cypress string chainable containing the premium user password
 */
export function getPremiumUserPw(): Cypress.Chainable<string> {
  return getStringCypressEnv('KEYCLOAK_PREMIUM_USER_PASSWORD');
}
/**
 * Retrieves the password for the admin user from the Cypress environment variables.
 * @returns a Cypress string chainable containing the admin password
 */
export function getAdminPw(): Cypress.Chainable<string> {
  return getStringCypressEnv('KEYCLOAK_DATALAND_ADMIN_PASSWORD');
}

/**
 * A higher level function that operates on a list of elements (dataArray) and applys a
 * potentially time-intensive operation (processor) to each element. These operations are completed in chunks
 * of chunkSize. The resulting promise is entered into the Cypress chain
 * @param dataArray the list of data elements to operate on
 * @param chunkSize the maximum number of operations that should be queued at the same time
 * @param processor a function performing some operation on a single element from the data array
 * @returns the Cypress chainable after queueing all operations
 */
export function doThingsInChunks<T>(
  dataArray: Array<T>,
  chunkSize: number,
  processor: (element: T) => Promise<void>
): Cypress.Chainable<void> {
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
 * Wraps a browser promise to a Cypress promise
 * @param promise the browser promise
 * @returns the converted Cypress (Bluebird) promise
 */
export function wrapPromiseToCypressPromise<T>(promise: Promise<T>): Bluebird<T> {
  return new Cypress.Promise((resolve, reject): void => {
    promise
      .then(
        (result): void => resolve(result),
        (error_): void => reject(error_)
      )
      .catch((error_): void => reject(error_));
  });
}

/**
 * Wraps a browser promise to a Cypress promise and enters it into the Cypress execution queue
 * @param promise the browser promise to execute in the Cypress chain
 * @returns a Cypress chainable
 */
export function browserThen<T>(promise: Promise<T>): Cypress.Chainable<T> {
  return cy.then((): Bluebird<T> => wrapPromiseToCypressPromise(promise));
}

/**
 * Returns the base url from the Cypress configuration
 * @returns the Cypress baseUrl
 */
export function getBaseUrl(): string {
  const cypressBaseUrl = Cypress.config('baseUrl');
  if (cypressBaseUrl) {
    return cypressBaseUrl;
  }
  throw new Error('Cypress baseUrl is unexpectedly null');
}
