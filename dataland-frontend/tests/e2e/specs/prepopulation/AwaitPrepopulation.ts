import { countCompaniesAndDataSetsForDataType } from "@e2e/utils/GeneralApiUtils";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { reader_name, reader_pw } from "@e2e/utils/Cypress";
import { describeIf } from "@e2e/support/TestUtility";
import { frameworkFixtureMap } from "@e2e/utils/FixtureMap";

describeIf(
  "I want to ensure that the prepopulation has finished before executing any further tests",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
  },
  () => {
    let expectedNumberOfCompanies = 0;

    before(function () {
      const fixtures = Object.values(frameworkFixtureMap);
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
        cy.wait(5000)
          .then(() => getKeycloakToken(reader_name, reader_pw))
          .then(async (token) => {
            const responsePromises = Object.keys(frameworkFixtureMap).map((key) =>
              countCompaniesAndDataSetsForDataType(token, key as keyof typeof frameworkFixtureMap),
            );

            const totalCompanies = (await Promise.all(responsePromises))
              .map((it) => it.numberOfCompaniesForDataType)
              .reduce((x, y) => x + y, 0);

            assert(
              totalCompanies >= expectedNumberOfCompanies,
              `Found ${totalCompanies} companies (Expecting at least ${expectedNumberOfCompanies})`,
            );
          });
      },
    );
  },
);
