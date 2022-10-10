import { retrieveCompanyIdsList } from "@e2e/utils/ApiUtils";

describe("I want to ensure that the prepopulation has finished before executing any further tests", (): void => {
  let minimumCompanySum = 0;
  before(function (): void {
    cy.fixture("CompanyInformationWithEuTaxonomyDataForNonFinancials").then(function (companies: {
      length: number;
    }): void {
      minimumCompanySum += companies.length;
    });
    cy.fixture("CompanyInformationWithEuTaxonomyDataForFinancials").then(function (companies: {
      length: number;
    }): void {
      minimumCompanySum += companies.length;
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
    (): void => {
      cy.wait(5000)
        .then((): Cypress.Chainable<string[]> => retrieveCompanyIdsList())
        .then((ids): void => {
          if (ids.length < minimumCompanySum) {
            throw Error(`Only found ${ids.length} companies (Expecting ${minimumCompanySum})`);
          }
        });
    }
  );
});
