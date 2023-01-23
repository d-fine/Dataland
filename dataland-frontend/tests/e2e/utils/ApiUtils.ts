import { CompanyDataControllerApi, Configuration, DataTypeEnum, StoredCompany } from "@clients/backend";
import { RouteHandler } from "cypress/types/net-stubbing";

/**
 * Uses the Dataland API to retrieve all companies that have a dataset for the specified dataType
 *
 * @param token the JWT token used to authorize the API requests
 * @param dataType the dataType to filter companies by
 * @returns all companies that have data for the specified dataType
 */
export async function getCompanyAndDataIds(token: string, dataType: DataTypeEnum): Promise<StoredCompany[]> {
  const dataset = await new CompanyDataControllerApi(new Configuration({ accessToken: token })).getCompanies(
    undefined,
    new Set([dataType])
  );
  return dataset.data;
}

/**
 * Uses the Dataland API to count how many datasets are stored on Dataland of a specified type and how many companies
 * have at least one such dataset associated to them.
 *
 * @param token the JWT token used to authorize the API requests
 * @param dataType the dataType of interest
 * @returns the counters for matching datasets and companies
 */
export async function countCompanyAndDataIds(
  token: string,
  dataType: DataTypeEnum
): Promise<{ matchingCompanies: number; matchingDataIds: number }> {
  const dataset = await getCompanyAndDataIds(token, dataType);
  const matchingCompanies = dataset.length;
  let matchingDataIds = 0;
  dataset.forEach((it) => {
    matchingDataIds += it.dataRegisteredByDataland.length;
  });

  return {
    matchingDataIds,
    matchingCompanies,
  };
}

/**
 * Registers a cypress interceptor that intercepts all requests with a 5XX status code.
 * The interceptor fails the test if a 5XX status code is returned.
 * Individual requests can be allowed to return 5XX status codes by setting the DATALAND-ALLOW-5XX header to true
 */
export function interceptAllAndCheckFor500Errors(): void {
  const handler: RouteHandler = (req) => {
    const allow500 = req.headers["DATALAND-ALLOW-5XX"] === "true";
    delete req.headers["DATALAND-ALLOW-5XX"];
    req.continue((res) => {
      if (res.statusCode >= 500 && !allow500) {
        assert(false, `Received a ${res.statusCode} Response from the Dataland backend (request to ${req.url})`);
      }
    });
  };
  cy.intercept("/api/**", handler);
  cy.intercept("/api-keys/**", handler);
}
