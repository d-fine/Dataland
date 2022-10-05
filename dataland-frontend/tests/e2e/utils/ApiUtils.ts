import Chainable = Cypress.Chainable;
import {
  CompanyDataControllerApi,
  CompanyInformation,
  Configuration,
  DataTypeEnum,
  StoredCompany,
} from "../../../build/clients/backend";

export async function uploadCompany(token: string, companyInformation: CompanyInformation): Promise<StoredCompany> {
  const data = await new CompanyDataControllerApi(new Configuration({ accessToken: token })).postCompany(
    companyInformation
  );
  return data.data;
}
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

export function retrieveFirstCompanyIdWithFrameworkData(framework: string): Chainable<string> {
  return cy
    .getKeycloakToken("data_uploader", Cypress.env("KEYCLOAK_UPLOADER_PASSWORD"))
    .then((token) => {
      return cy.request({
        url: `/api/companies?dataTypes=${framework}`,
        method: "GET",
        headers: { Authorization: "Bearer " + token },
      });
    })
    .then((response) => {
      return response.body[0].companyId;
    });
}
