import { CompanyDataControllerApi, Configuration, DataTypeEnum, StoredCompany } from "@clients/backend";
import { RouteHandler } from "cypress/types/net-stubbing";
import { FixtureData } from "../fixtures/FixtureUtils";

export interface UploadIds {
  companyId: string;
  dataId: string;
}

/**
 * Gets stored companies that have at least one dataset with the provided data type
 *
 * @param token The API bearer token to use
 * @param dataType Data type for which the returned companies should have at least one dataset
 * @returns an array of stored companies
 */
export async function getStoredCompaniesForDataType(token: string, dataType: DataTypeEnum): Promise<StoredCompany[]> {
  const response = await new CompanyDataControllerApi(new Configuration({ accessToken: token })).getCompanies(
    undefined,
    new Set([dataType])
  );
  return response.data;
}

/**
 * Counts the number of stored companies which contain at least one dataset with the provided data type and the
 * total number of datasets for that datatype
 *
 * @param token The API bearer token to use
 * @param dataType The data type to use while counting companies and number of datasets for that data type
 * @returns an object which contains the resulting number of companies and number of datasets
 */
export async function countCompaniesAndDataSetsForDataType(
  token: string,
  dataType: DataTypeEnum
): Promise<{ numberOfCompaniesForDataType: number; numberOfDataSetsForDataType: number }> {
  const storedCompaniesForDataType = await getStoredCompaniesForDataType(token, dataType);
  let numberOfDataSetsForDataType = 0;
  storedCompaniesForDataType.forEach((storedCompany) => {
    numberOfDataSetsForDataType += storedCompany.dataRegisteredByDataland.length;
  });

  return {
    numberOfDataSetsForDataType,
    numberOfCompaniesForDataType: storedCompaniesForDataType.length,
  };
}

/**
 * TODO  talk with Marc about this and why it is needed
 * Intercepts all requests to the backend, checks if a certain allow-flag is set in the headers, then checks if the
 * response has a status code greater or equal 500, and throws an error depending on the allow-flag
 */
export function interceptAllAndCheckFor500Errors(): void {
  const handler: RouteHandler = (incomingRequest) => {
    const is500ResponseAllowed = incomingRequest.headers["DATALAND-ALLOW-5XX"] === "true";
    delete incomingRequest.headers["DATALAND-ALLOW-5XX"];
    incomingRequest.continue((response) => {
      if (response.statusCode >= 500 && !is500ResponseAllowed) {
        assert(
          false,
          `Received a ${response.statusCode} Response from the Dataland backend (request to ${incomingRequest.url})`
        );
      }
    });
  };
  cy.intercept("/api/**", handler);
  cy.intercept("/api-keys/**", handler); // TODO I think this line of code might be redundant.  this url-route should be covered by the interception above
}

/**
 * Generic function to retrieve the first prepared fixture whose company name equals the provided search string
 *
 * @param name Search string to look for in the prepared fixtures
 * @param preparedFixtures The parsed array of prepared fixtures
 * @returns the first prepared fixture whose name equals the provided search string
 */
export function getPreparedFixture<T>(name: string, preparedFixtures: FixtureData<T>[]): FixtureData<T> {
  const preparedFixture = preparedFixtures.find((it): boolean => it.companyInformation.companyName == name)!;
  if (!preparedFixture) {
    throw new ReferenceError(
      "Variable preparedFixture is undefined because the provided company name could not be found in the prepared fixtures."
    );
  } else {
    return preparedFixture;
  }
}
