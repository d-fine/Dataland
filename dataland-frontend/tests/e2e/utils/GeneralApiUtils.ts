import { CompanyDataControllerApi, Configuration, DataTypeEnum, StoredCompany } from "@clients/backend";
import { RouteHandler } from "cypress/types/net-stubbing";
import { FixtureData } from "../fixtures/FixtureUtils";

export interface UploadIds {
  companyId: string;
  dataId: string;
}

export async function getStoredCompaniesForDataType(token: string, dataType: DataTypeEnum): Promise<StoredCompany[]> {
  const response = await new CompanyDataControllerApi(new Configuration({ accessToken: token })).getCompanies(
    undefined,
    new Set([dataType])
  );
  return response.data;
}

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
