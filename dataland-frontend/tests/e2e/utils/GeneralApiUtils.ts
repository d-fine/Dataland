import {
  CompanyDataControllerApi,
  Configuration,
  type DataTypeEnum,
  type BasicCompanyInformation,
  MetaDataControllerApi,
} from '@clients/backend';

import { KEYCLOAK_ROLE_REVIEWER } from '@/utils/KeycloakRoles';

export interface UploadIds {
  companyId: string;
  dataId: string;
}

interface InterceptRequest {
  headers: Record<string, string>;
  url: string;
  query: Record<string, string>;
  on: (event: string, callback: (response: InterceptResponse) => void) => void;
}

interface InterceptResponse {
  statusCode: number;
}

/**
 * Gets stored companies that have at least one dataset with the provided data type
 * @param token The API bearer token to use
 * @param dataType Data type for which the returned companies should have at least one dataset
 * @returns an array of stored companies
 */
export async function searchBasicCompanyInformationForDataType(
  token: string,
  dataType: DataTypeEnum
): Promise<BasicCompanyInformation[]> {
  const response = await new CompanyDataControllerApi(new Configuration({ accessToken: token })).getCompanies(
    undefined,
    new Set([dataType])
  );
  return response.data;
}

/**
 * Polls the companies search API until a company whose name matches the given search string is returned, i.e. until
 * the company is searchable. Uploads are processed asynchronously, so the UI may not find a company right after upload.
 * @param token The API bearer token to use
 * @param companyName Name (or name fragment) used as search string
 * @param dataType Optional data type the company must have at least one dataset for
 * @param timeoutInMs Maximum time to wait
 */
export function waitForCompanyToBeSearchableViaApi(
  token: string,
  companyName: string,
  dataType?: DataTypeEnum,
  timeoutInMs = 120000
): void {
  let lastCount = 0;
  let lastError = 'none';
  cy.waitUntil(
    () =>
      cy.wrap(
        new CompanyDataControllerApi(new Configuration({ accessToken: token }))
          .getCompanies(companyName, dataType ? new Set([dataType]) : undefined)
          .then((response) => {
            lastCount = response.data.length;
            return response.data.some((company) => company.companyName === companyName);
          })
          .catch((error: unknown) => {
            lastError = String(error);
            return false;
          }),
        { log: false, timeout: timeoutInMs }
      ),
    {
      timeout: timeoutInMs,
      interval: 2000,
      errorMsg: () =>
        `Company "${companyName}" did not become searchable via the API within ${timeoutInMs / 1000}s ` +
        `(data type filter: ${dataType ?? 'none'}). Last result count: ${lastCount}, last error: ${lastError}.`,
    }
  );
}

/**
 * Counts the number of stored companies which contain at least one dataset with the provided data type and the
 * total number of datasets for that datatype
 * @param token The API bearer token to use
 * @param dataType The data type to use while counting companies and number of datasets for that data type
 * @returns an object which contains the resulting number of companies and number of datasets
 */
export async function countCompaniesAndDatasetsForDataType(
  token: string,
  dataType: DataTypeEnum
): Promise<{ numberOfCompaniesForDataType: number; numberOfDatasetsForDataType: number }> {
  const basicCompanyInformations = await searchBasicCompanyInformationForDataType(token, dataType);
  let numberOfDatasetsForDataType = 0;
  const metaDataController = new MetaDataControllerApi(new Configuration({ accessToken: token }));
  for (const basicCompanyInfo of basicCompanyInformations) {
    const metaInfos = await metaDataController.getListOfDataMetaInfo(basicCompanyInfo.companyId); // NOSONAR: sequential, test helper
    numberOfDatasetsForDataType += metaInfos.data.length;
  }

  return {
    numberOfDatasetsForDataType: numberOfDatasetsForDataType,
    numberOfCompaniesForDataType: basicCompanyInformations.length,
  };
}

/**
 * Intercepts all requests to the backend, checks if a certain allow-flag is set in the headers, then checks if the
 * response has a status code greater or equal 500, and throws an error depending on the allow-flag
 */
export function interceptAllAndCheckFor500Errors(): void {
  const handler = (incomingRequest: InterceptRequest): void => {
    const is500ResponseAllowed = incomingRequest.headers['DATALAND-ALLOW-5XX'] === 'true';
    delete incomingRequest.headers['DATALAND-ALLOW-5XX'];
    incomingRequest.on('response', (response: InterceptResponse) => {
      if (response.statusCode >= 500 && !is500ResponseAllowed) {
        assert(
          false,
          `Received a ${response.statusCode} Response from the Dataland backend (request to ${incomingRequest.url})`
        );
      }
    });
  };
  cy.intercept('/api/**', handler);
  cy.intercept('/api-keys/**', handler);
}

/**
 * Intercepts all data upload requests to the backend and sets the bypassQa flag
 */
export function interceptAllDataPostsAndBypassQaIfPossible(): void {
  const handler = (incomingRequest: InterceptRequest): void => {
    const isQaRequired = incomingRequest.headers['REQUIRE-QA'] === 'true';
    delete incomingRequest.headers['REQUIRE-QA'];
    if (isQaRequired) {
      incomingRequest.query['bypassQa'] = 'false';
      return;
    }
    const authorizationHeader = incomingRequest.headers['authorization'] ?? incomingRequest.headers['Authorization'];
    if (authorizationHeader === undefined) {
      return;
    }
    const base64EncodedAuthorizationPayload = authorizationHeader.split('.')[1];
    const authorization = JSON.parse(atob(base64EncodedAuthorizationPayload)) as { realm_access: { roles: string[] } };
    if (authorization.realm_access.roles.includes(KEYCLOAK_ROLE_REVIEWER)) {
      incomingRequest.query['bypassQa'] = 'true';
    }
  };
  cy.intercept('/api/data/*', handler);
}
