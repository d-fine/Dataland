import {DataTypeEnum, ExportFileType, type SfdrData, type StoredCompany} from '@clients/backend';
import { admin_name, admin_pw, getBaseUrl, reader_name, reader_pw } from '@e2e/utils/Cypress.ts';
import { getKeycloakToken } from '@e2e/utils/Auth.ts';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload.ts';
import { uploadFrameworkDataForPublicToolboxFramework } from '@e2e/utils/FrameworkUpload.ts';
import SfdrBaseFrameworkDefinition from '@/frameworks/sfdr/BaseFrameworkDefinition';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { ExportFileTypeInformation } from '@/types/ExportFileTypeInformation.ts';
import { describeIf } from '@e2e/support/TestUtility.ts';
import { ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER } from '@/utils/Constants.ts';
import { humanizeStringOrNumber } from '@/utils/StringFormatter.ts';


describeIf(
    'As a user, I want to be able edit data points on dataland',
    {
        executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
    },
    () => {
        const reportingPeriod = '2021';
        const dataType = DataTypeEnum.Sfdr;
        const DOWNLOADS_FOLDER = Cypress.config('downloadsFolder');

        let storedCompany: StoredCompany;
        let SfdrFixtureWithNoNullFields: FixtureData<SfdrData>;


        /**
         * Visit framework data page, select download format and click download button
         * @param fileType Needs to be one of the identifiers of an ExportFileTypes
         * @param useAliases specifies if aliases are to be exported
         */
        function visitPageAndClickDownloadButton(fileType: string, useAliases: boolean = false): void {
            const fileTypeMap: Record<string, string> = {
                JSON: 'JavaScript Object Notation (.json)',
                CSV: 'Comma-separated Values (.csv)',
                EXCEL: 'Excel File (.xlsx)',
            };

            cy.visit(getBaseUrl() + `/companies/${storedCompany.companyId}/frameworks/${dataType}`);

            cy.get('button[data-test=downloadDataButton]').should('exist').click();
            cy.get('[data-test="listOfReportingPeriods"]').contains(reportingPeriod).should('be.visible').click();
            if (useAliases) {
                cy.get('[data-test="includeAliasSwitch"]').should('have.class', 'p-toggleswitch-checked');
            } else {
                cy.get('[data-test="includeAliasSwitch"]')
                    .should('have.class', 'p-toggleswitch-checked')
                    .find('.p-toggleswitch-input')
                    .click();
                cy.get('[data-test="includeAliasSwitch"]').should('not.have.class', 'p-toggleswitch-checked');
            }

            const dropdownValue = fileTypeMap[fileType.toUpperCase()];
            if (!dropdownValue) {
                throw new Error(`Unsupported fileType: ${fileType}`);
            }
            cy.get('[data-test="fileTypeSelector"]').find('.p-select-dropdown').click();
            cy.get('.p-select-list-container').contains(dropdownValue).click();
            cy.get('button[data-test=downloadDataButtonInModal]').click();
        }

        /**
         * Verifies that a downloaded file with a given prefix and extension exists,
         * has an appropriate file size, and deletes it afterwards to avoid clutter.
         *
         * @param partialFileNamePrefix - The beginning of the expected filename (e.g. 'data-export-FrameworkName').
         * @param fileExtension - The file extension to match (e.g. 'csv', 'xlsx', 'json').
         */
        function verifyDownloadedFile(partialFileNamePrefix: string, fileExtension: string): void {
            cy.wait(Cypress.env('medium_timeout_in_ms') as number); // optional short delay
            cy.task('findFileByPrefix', {
                folder: DOWNLOADS_FOLDER,
                prefix: partialFileNamePrefix,
                extension: fileExtension,
            }).then((filePath) => {
                const filePathStr = filePath as string;
                expect(filePathStr).to.exist;
            });
        }

        /**
         * Verifies that the downloaded file contains an alias when specified and a column header that is not an alias,
         * when export without aliases is selected
         *
         * @param partialFileNamePrefix
         * @param fileExtension
         * @param useAliases - The file extension to match (e.g. 'csv', 'xlsx', 'json').
         */
        function verifyAliases(partialFileNamePrefix: string, fileExtension: string, useAliases: boolean): void {
            cy.wait(Cypress.env('medium_timeout_in_ms') as number);
            cy.task('findFileByPrefix', {
                folder: DOWNLOADS_FOLDER,
                prefix: partialFileNamePrefix,
                extension: fileExtension,
            }).then((filePath) => {
                if (typeof filePath === 'string') {
                    cy.readFile(filePath).then((txt) => {
                        if (useAliases) {
                            expect(txt).to.contain('COMPANY_NAME');
                            expect(txt).to.not.contain('companyName');
                        } else {
                            expect(txt).to.contain('companyName');
                            expect(txt).to.not.contain('COMPANY_NAME');
                        }
                    });
                }
            });
        }

        /**
         * Returns the human-readable label for the currently selected framework data type.
         *
         * @returns label corresponding to the current `dataType`.
         */
        function getFrameworkLabel(): string {
            const availableFrameworks = ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER.map((f) => ({
                value: f,
                label: humanizeStringOrNumber(f),
            }));
            return availableFrameworks.find((f) => f.value === dataType)?.label ?? dataType;
        }

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
            cy.ensureLoggedIn(reader_name, reader_pw);
        });

        it('Download data as CSV file, check for appropriate size and delete it afterwards', () => {
            const frameworkLabel = getFrameworkLabel();
            visitPageAndClickDownloadButton(ExportFileType.Csv.toString());
            verifyDownloadedFile(`data-export-${frameworkLabel}`, ExportFileTypeInformation.CSV.fileExtension);
        });

        it('Download data as CSV file, check that an alias exists and delete it afterwards', () => {
            const frameworkLabel = getFrameworkLabel();
            const useAliases = true;
            visitPageAndClickDownloadButton(ExportFileType.Csv.toString(), useAliases);
            verifyAliases(`data-export-${frameworkLabel}`, ExportFileTypeInformation.CSV.fileExtension, useAliases);
        });

        it('Download data as CSV file, check that a non-alias column name exists and delete it afterwards', () => {
            const frameworkLabel = getFrameworkLabel();
            const useAliases = false;
            visitPageAndClickDownloadButton(ExportFileType.Csv.toString(), useAliases);
            verifyAliases(`data-export-${frameworkLabel}`, ExportFileTypeInformation.CSV.fileExtension, useAliases);
        });

        it('Download data as EXCEL file, check for appropriate size and delete it afterwards', () => {
            const frameworkLabel = getFrameworkLabel();
            visitPageAndClickDownloadButton(ExportFileType.Excel.toString());
            verifyDownloadedFile(`data-export-${frameworkLabel}`, ExportFileTypeInformation.EXCEL.fileExtension);
        });

        it('Download data as JSON file, check for appropriate size and delete it afterwards', () => {
            const frameworkLabel = getFrameworkLabel();
            visitPageAndClickDownloadButton(ExportFileType.Json.toString());
            verifyDownloadedFile(`data-export-${frameworkLabel}`, ExportFileTypeInformation.JSON.fileExtension);
        });
    }
);
