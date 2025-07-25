import { countCompaniesAndDatasetsForDataType } from '@e2e/utils/GeneralApiUtils';
import { getKeycloakToken } from '@e2e/utils/Auth';
import { reader_name, reader_pw } from '@e2e/utils/Cypress';
import { describeIf } from '@e2e/support/TestUtility';
import { convertKebabCaseToPascalCase } from '@/utils/StringFormatter';
import { DataTypeEnum } from '@clients/backend';

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
        `CompanyInformationWith${convertKebabCaseToPascalCase(dataType)}Data`.replace('-', '')
      );
      fixtures.forEach((fixtureFile) => {
        cy.fixture(fixtureFile).then(function (companies: []) {
          expectedNumberOfCompanies += companies.length;
        });
      });
    });

    it(
      'Should wait until prepopulation has finished',
      {
        retries: {
          runMode: Cypress.env('AWAIT_PREPOPULATION_RETRIES') as number,
          openMode: Cypress.env('AWAIT_PREPOPULATION_RETRIES') as number,
        },
      },
      () => {
        const delayToWaitForPrepopulationSoThatNotAllRetriesAreWastedInstantly = 5000;
        // eslint-disable-next-line cypress/no-unnecessary-waiting
        cy.wait(delayToWaitForPrepopulationSoThatNotAllRetriesAreWastedInstantly)
          .then(() => getKeycloakToken(reader_name, reader_pw))
          .then({ timeout: 150000 }, async (token) => {
            const responsePromises = prepopulatedDataTypes.map((key) =>
              countCompaniesAndDatasetsForDataType(token, key as DataTypeEnum)
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
