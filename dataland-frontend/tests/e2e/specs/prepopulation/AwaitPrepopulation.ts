import { countCompaniesAndDataSetsForDataType } from "@e2e/utils/GeneralApiUtils";
import { DataTypeEnum } from "@clients/backend";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { reader_name, reader_pw } from "@e2e/utils/Cypress";

describe("I want to ensure that the prepopulation has finished before executing any further tests", () => {
  let expectedNumberOfCompanies = 0;

  before(function () {
    const fixtures = [
      "CompanyInformationWithEuTaxonomyDataForNonFinancials",
      "CompanyInformationWithEuTaxonomyDataForFinancials",
    ];
    if (Cypress.env("DATA_ENVIRONMENT") === "fakeFixtures") {
      fixtures.push(
        "CompanyInformationWithLksgData",
        "CompanyInformationWithSfdrData",
        "CompanyInformationWithSmeData"
      );
    }
    fixtures.forEach((fixtureFile) => {
      cy.fixture(fixtureFile).then(function (companies: []) {
        expectedNumberOfCompanies += companies.length;
      });
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
      cy.wait(5000) // TODO rather wait for specific stuff than random timespans
        .then(() => getKeycloakToken(reader_name, reader_pw))
        .then(async (token) => {
          const financialResponse = await countCompaniesAndDataSetsForDataType(
            token,
            DataTypeEnum.EutaxonomyFinancials
          );
          const nonFinancialResponse = await countCompaniesAndDataSetsForDataType(
            token,
            DataTypeEnum.EutaxonomyNonFinancials
          );
          let totalCompanies =
            financialResponse.numberOfCompaniesForDataType + nonFinancialResponse.numberOfCompaniesForDataType;
          if (Cypress.env("DATA_ENVIRONMENT") === "fakeFixtures") {
            const lksgResponse = await countCompaniesAndDataSetsForDataType(token, DataTypeEnum.Lksg);
            const sfdrResponse = await countCompaniesAndDataSetsForDataType(token, DataTypeEnum.Sfdr);
            const smeResponse = await countCompaniesAndDataSetsForDataType(token, DataTypeEnum.Sme);
            totalCompanies +=
              lksgResponse.numberOfCompaniesForDataType +
              sfdrResponse.numberOfCompaniesForDataType +
              smeResponse.numberOfCompaniesForDataType;
          }
          assert(
            totalCompanies >= expectedNumberOfCompanies,
            `Found ${totalCompanies} companies (Expecting at least ${expectedNumberOfCompanies})`
          );
        });
    }
  );
});
