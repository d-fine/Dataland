import { CompanyDataControllerApi, Configuration, DataTypeEnum, StoredCompany } from "@clients/backend";

export async function getCompanyAndDataIds(token: string, dataType: DataTypeEnum): Promise<StoredCompany[]> {
  const dataset = await new CompanyDataControllerApi(new Configuration({ accessToken: token })).getCompanies(
    undefined,
    new Set([dataType])
  );
  return dataset.data;
}

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

export function interceptAllAndCheckFor500Errors(): void {
  cy.intercept("/api/**", (req) => {
    const allow500 = req.headers["DATALAND-ALLOW-5XX"] === "true";
    delete req.headers["DATALAND-ALLOW-5XX"];
    req.continue((res) => {
      if (res.statusCode >= 500 && !allow500) {
        assert(false, `Received a ${res.statusCode} Response from the Dataland backend (request to ${req.url})`);
      }
    });
  }).as("Detect 500");
}
