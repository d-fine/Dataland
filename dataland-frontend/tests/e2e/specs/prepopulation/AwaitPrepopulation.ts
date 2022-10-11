import { countCompanyAndDataIds } from "@e2e/utils/ApiUtils";
import { DataTypeEnum } from "@clients/backend";
import { getKeycloakToken } from "@e2e/utils/Auth";

describe("I want to ensure that the prepopulation has finished before executing any further tests", () => {
  let minimumNumberNonFinancialCompanies = 0;
  let minimumNumberFinancialCompanies = 0;
  before(function () {
    cy.fixture("CompanyInformationWithEuTaxonomyDataForNonFinancials").then(function (companies) {
      minimumNumberNonFinancialCompanies += companies.length;
    });
    cy.fixture("CompanyInformationWithEuTaxonomyDataForFinancials").then(function (companies) {
      minimumNumberFinancialCompanies += companies.length;
    });
  });

  it(
    "Should wait until prepopulation has finished",
    {
      retries: {
        runMode: Cypress.env("AWAIT_PREPOPULATION_RETRIES"),
        openMode: Cypress.env("AWAIT_PREPOPULATION_RETRIES"),
      },
    },
    () => {
      cy.wait(5000)
        .then(() => getKeycloakToken("data_reader", Cypress.env("KEYCLOAK_READER_PASSWORD")))
        .then((token) => {
          countCompanyAndDataIds(token, DataTypeEnum.EutaxonomyFinancials).then((response) => {
            if (response.matchingCompanies < minimumNumberFinancialCompanies) {
              throw Error(
                `Only found ${response.matchingCompanies} financial companies (Expecting ${minimumNumberFinancialCompanies})`
              );
            }
          });
          countCompanyAndDataIds(token, DataTypeEnum.EutaxonomyNonFinancials).then((response) => {
            if (response.matchingCompanies < minimumNumberNonFinancialCompanies) {
              throw Error(
                `Only found ${response.matchingCompanies} non-financial companies (Expecting ${minimumNumberNonFinancialCompanies})`
              );
            }
          });
        });
    }
  );
});
