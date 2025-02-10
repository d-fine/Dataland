import {
  CompanyDataControllerApi,
  Configuration,
  type DataTypeEnum,
  type BasicCompanyInformation,
  MetaDataControllerApi,
} from '@clients/backend';
import { type RouteHandler } from 'cypress/types/net-stubbing';

import { KEYCLOAK_ROLE_REVIEWER } from '@/utils/KeycloakRoles';

export interface UploadIds {
  companyId: string;
  dataId: string;
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
 * Counts the number of stored companies which contain at least one dataset with the provided data type and the
 * total number of datasets for that datatype
 * @param token The API bearer token to use
 * @param dataType The data type to use while counting companies and number of datasets for that data type
 * @returns an object which contains the resulting number of companies and number of datasets
 */
export async function countCompaniesAndDataSetsForDataType(
  token: string,
  dataType: DataTypeEnum
): Promise<{ numberOfCompaniesForDataType: number; numberOfDataSetsForDataType: number }> {
  const basicCompanyInformations = await searchBasicCompanyInformationForDataType(token, dataType);
  let numberOfDataSetsForDataType = 0;
  const metaDataController = new MetaDataControllerApi(new Configuration({ accessToken: token }));
  for (const basicCompanyInfo of basicCompanyInformations) {
    numberOfDataSetsForDataType += (await metaDataController.getListOfDataMetaInfo(basicCompanyInfo.companyId)).data
      .length;
  }

  return {
    numberOfDataSetsForDataType,
    numberOfCompaniesForDataType: basicCompanyInformations.length,
  };
}

/**
 * Intercepts all requests to the backend, checks if a certain allow-flag is set in the headers, then checks if the
 * response has a status code greater or equal 500, and throws an error depending on the allow-flag
 */
export function interceptAllAndCheckFor500Errors(): void {
  const handler: RouteHandler = (incomingRequest) => {
    const is500ResponseAllowed = incomingRequest.headers['DATALAND-ALLOW-5XX'] === 'true';
    delete incomingRequest.headers['DATALAND-ALLOW-5XX'];
    incomingRequest.continue((response) => {
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
  const handler: RouteHandler = (incomingRequest) => {
    const isQaRequired = incomingRequest.headers['REQUIRE-QA'] === 'true';
    delete incomingRequest.headers['REQUIRE-QA'];
    if (isQaRequired) {
      incomingRequest.query['bypassQa'] = 'false';
      return;
    }
    const authorizationHeader = (incomingRequest.headers['authorization'] ??
      incomingRequest.headers['Authorization']) as string;
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
