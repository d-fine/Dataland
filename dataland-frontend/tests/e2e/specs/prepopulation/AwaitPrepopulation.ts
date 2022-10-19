import { countCompanyAndDataIds } from "@e2e/utils/ApiUtils";
import { DataTypeEnum } from "@clients/backend";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { reader_name, reader_pw } from "@e2e/utils/Cypress";

describe("I want to ensure that the prepopulation has finished before executing any further tests", () => {
  let minimumNumberNonFinancialCompanies = 0;
  let minimumNumberFinancialCompanies = 0;
  before(function () {
    cy.fixture("CompanyInformationWithEuTaxonomyDataForNonFinancials").then(function (companies: []) {
      minimumNumberNonFinancialCompanies += companies.length;
    });
    cy.fixture("CompanyInformationWithEuTaxonomyDataForFinancials").then(function (companies: []) {
      minimumNumberFinancialCompanies += companies.length;
    });
  });

  it(
    "Should wait until prepopulation has finished",
    {
      retries: {
        runMode: Cypress.env("AWAIT_PREPOPULATION_RETRIES") as number,
        openMode: Cypress.env("AWAIT_PREPOPULATION_RETRIES") as number,
      },
    },
    () => {
      cy.wait(5000)
        .then(() => getKeycloakToken(reader_name, reader_pw))
        .then(async (token) => {
          const financialResponse = await countCompanyAndDataIds(token, DataTypeEnum.EutaxonomyFinancials);
          assert(
            financialResponse.matchingCompanies >= minimumNumberFinancialCompanies,
            `Found ${financialResponse.matchingCompanies} financial companies (Expecting at least ${minimumNumberFinancialCompanies})`
          );
          const nonFinancialResponse = await countCompanyAndDataIds(token, DataTypeEnum.EutaxonomyNonFinancials);
          assert(
            nonFinancialResponse.matchingCompanies >= minimumNumberNonFinancialCompanies,
            `Found ${nonFinancialResponse.matchingCompanies} non-financial companies (Expecting at least ${minimumNumberNonFinancialCompanies})`
          );
        });
    }
  );
});
