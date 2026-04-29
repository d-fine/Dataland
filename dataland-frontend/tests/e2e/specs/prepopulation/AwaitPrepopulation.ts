import { countCompaniesAndDatasetsForDataType } from '@e2e/utils/GeneralApiUtils';
import { getReaderToken } from '@e2e/utils/Auth';
import { describeIf } from '@e2e/support/TestUtility';
import { convertKebabCaseToPascalCase } from '@/utils/StringFormatter';
import { DataTypeEnum } from '@clients/backend';

const awaitPrepopulationRetries = Number(Cypress.expose('AWAIT_PREPOPULATION_RETRIES') ?? 250);

describeIf(
  'I want to ensure that the prepopulation has finished before executing any further tests',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  () => {
    let expectedNumberOfCompanies = 0;
    let prepopulatedDataTypes: string[] = [];

    before(function () {
      const dataTypesWithToolboxSupport = Object.values(DataTypeEnum);
      // At the moment, VSME prepopulation is broken.
      prepopulatedDataTypes = dataTypesWithToolboxSupport.filter((element) => element !== 'vsme');
      const fixtures = prepopulatedDataTypes.map((dataType) =>
        `CompanyInformationWith${convertKebabCaseToPascalCase(dataType)}Data`.replaceAll('-', '')
      );
      for (const fixtureFile of fixtures) {
        cy.fixture(fixtureFile).then(function (companies: []) {
          expectedNumberOfCompanies += companies.length;
        });
      }
    });

    it(
      'Should wait until prepopulation has finished',
      {
        retries: {
          runMode: awaitPrepopulationRetries,
          openMode: awaitPrepopulationRetries,
        },
      },
      () => {
        const delayToWaitForPrepopulationSoThatNotAllRetriesAreWastedInstantly = 5000;
        // eslint-disable-next-line cypress/no-unnecessary-waiting
        cy.wait(delayToWaitForPrepopulationSoThatNotAllRetriesAreWastedInstantly)
          .then(() => getReaderToken())
          .then({ timeout: 150000 }, async (error_) => {
            const responsePromises = prepopulatedDataTypes.map((key) =>
              countCompaniesAndDatasetsForDataType(error_, key as DataTypeEnum)
            );

            const totalCompanies = (await Promise.all(responsePromises))
              .map((it) => it.numberOfCompaniesForDataType)
              .reduce((x, y) => x + y, 0);

            assert(
              totalCompanies >= expectedNumberOfCompanies,
              `Found ${totalCompanies} companies (Expecting at least ${expectedNumberOfCompanies})`
            );
          });
      }
    );
  }
);
