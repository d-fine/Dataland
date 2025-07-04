import { countCompaniesAndDatasetsForDataType } from '@e2e/utils/GeneralApiUtils';
import { getKeycloakToken } from '@e2e/utils/Auth';
import { reader_name, reader_pw } from '@e2e/utils/Cypress';
import { describeIf } from '@e2e/support/TestUtility';
import { getAllPublicFrameworkIdentifiers } from '@/frameworks/BasePublicFrameworkRegistry';
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
            prepopulatedDataTypes = getAllPublicFrameworkIdentifiers();
            const fixtureFiles = prepopulatedDataTypes.map((dataType) =>
                `CompanyInformationWith${convertKebabCaseToPascalCase(dataType)}Data`
            );

            fixtureFiles.forEach((fixtureFile) => {
                cy.fixture(fixtureFile).then((companies: []) => {
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

                cy.wait(delayToWaitForPrepopulationSoThatNotAllRetriesAreWastedInstantly)
                    .then(() => getKeycloakToken(reader_name, reader_pw))
                    .then({ timeout: 120000 }, async (token) => {
                        const responsePromises = prepopulatedDataTypes.map((key) =>
                            countCompaniesAndDatasetsForDataType(token, DataTypeEnum[key as keyof typeof DataTypeEnum])
                        );

                        const totalCompanies = (await Promise.all(responsePromises))
                            .map((res) => res.numberOfCompaniesForDataType)
                            .reduce((acc, val) => acc + val, 0);

                        assert(
                            totalCompanies >= expectedNumberOfCompanies,
                            `Found ${totalCompanies} companies (Expecting at least ${expectedNumberOfCompanies})`
                        );
                    });
            }
        );
    }
);
