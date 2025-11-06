import {DataTypeEnum, type SfdrData, type StoredCompany} from '@clients/backend';
import {admin_name, admin_pw, getBaseUrl} from '@e2e/utils/Cypress.ts';
import {getKeycloakToken} from '@e2e/utils/Auth.ts';
import {generateDummyCompanyInformation, uploadCompanyViaApi} from '@e2e/utils/CompanyUpload.ts';
import {uploadFrameworkDataForPublicToolboxFramework} from '@e2e/utils/FrameworkUpload.ts';
import SfdrBaseFrameworkDefinition from '@/frameworks/sfdr/BaseFrameworkDefinition.ts';
import {type FixtureData, getPreparedFixture} from '@sharedUtils/Fixtures.ts';
import {describeIf} from '@e2e/support/TestUtility.ts';

describeIf(
    'As a user, I want to be able edit data points on dataland',
    {
        executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
    },
    () => {
        const reportingPeriod = '2021';
        const dataType = DataTypeEnum.Sfdr;
        let storedCompany: StoredCompany;
        let SfdrFixtureWithNoNullFields: FixtureData<SfdrData>;

        before(() => {
            cy.fixture('CompanyInformationWithSfdrPreparedFixtures').then((jsonContent) => {
                const preparedFixturesSfdr = jsonContent as Array<FixtureData<SfdrData>>;
                SfdrFixtureWithNoNullFields = getPreparedFixture('Sfdr-dataset-with-no-null-fields', preparedFixturesSfdr);
            });

            getKeycloakToken(admin_name, admin_pw).then((token: string) => {
                const uniqueCompanyMarker = Date.now().toString();
                const testStoredCompanyName = 'Company-Created-For-Download-Test-' + uniqueCompanyMarker;
                return uploadCompanyViaApi(token, generateDummyCompanyInformation(testStoredCompanyName)).then(
                    (newStoredCompany) => {
                        storedCompany = newStoredCompany;
                        return uploadFrameworkDataForPublicToolboxFramework(
                            SfdrBaseFrameworkDefinition,
                            token,
                            storedCompany.companyId,
                            reportingPeriod,
                            SfdrFixtureWithNoNullFields.t,
                            true
                        );
                    }
                );
            });
        });

        beforeEach(() => {
            cy.ensureLoggedIn(admin_name, admin_pw);
        });

        it('should open EditDataPointDialog for a BigDecimalExtendedDataPointFormField modal and display its parts', () => {
            cy.visit(getBaseUrl() + `/companies/${storedCompany.companyId}/frameworks/${dataType}`);
            cy.get('button[data-test=editDatasetButton]').should('exist').click();
            cy.contains('span.table-left-label', 'Scope 1 GHG emissions')
                .closest('td')
                .next('td')
                .find('button[data-test="edit-data-point-icon"]')
                .click();
            cy.get('div.p-dialog-content').within(() => {
                cy.get('[data-test="big-decimal-input"]')
                    .should('exist')
                    .should('be.visible')
                    .find('input')
                    .should('have.value', '17,992.73');

                cy.get('[data-test="quality-select"]')
                    .should('exist')
                    .should('be.visible')
                    .find('.p-select-label, .p-dropdown-label, .p-inputwrapper, .p-select')
                    .should('contain', 'Estimated');

                cy.get('[data-test="page-number-input"]')
                    .should('exist')
                    .should('be.visible')
                    .should('have.value', '1060-1150');

                cy.get('[data-test="comment-textarea"]')
                    .should('exist')
                    .should('be.visible')
                    .should('have.value', 'connect haptic program');
            });
        });

        it.only('should open EditDataPointDialog, edit all fields and save changes successfully', () => {
            const newValue = '1234.56';
            const newComment = 'Updated via Cypress test';
            const newPage = '42';
            const newQuality = 'Reported';

            cy.visit(getBaseUrl() + `/companies/${storedCompany.companyId}/frameworks/${dataType}`);
            cy.get('button[data-test=editDatasetButton]').should('exist').click();

            cy.contains('span.table-left-label', 'Scope 1 GHG emissions')
                .closest('td')
                .next('td')
                .find('button[data-test="edit-data-point-icon"]')
                .click();

            cy.get('div.p-dialog-content').should('be.visible').within(() => {
                cy.get('[data-test="big-decimal-input"] input')
                    .should('exist')
                    .should('be.visible')
                    .should('have.value', '17,992.73');

            });

            cy.get('[data-test="quality-select"]')
                .should('exist')
                .should('be.visible')
                .find('.p-select-label, .p-dropdown-label')
                .should('contain', 'Estimated');

            cy.get('[data-test="comment-textarea"]').should('have.value', 'connect haptic program');


            cy.get('div.p-dialog-content').within(() => {
                cy.get('[data-test="big-decimal-input"] input')
                    .clear()
                    .type(newValue)
                    .blur();


                cy.intercept('POST', '**/api/data-points?bypassQa=true').as('saveDataPoint');
                cy.get('[data-test="save-data-point-button"]').should('be.visible').click();

                cy.wait('@saveDataPoint').its('response.statusCode').should('be.oneOf', [200, 201]);

                cy.get('div.p-dialog-content').should('not.exist');
            });

            cy.contains('span.table-left-label', 'Scope 1 GHG emissions')
                .closest('td')
                .next('td')
                .should('contain', '1,234.56');
        });

    }
);
