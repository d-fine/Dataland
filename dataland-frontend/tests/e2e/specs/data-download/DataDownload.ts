import { DataTypeEnum, ExportFileType, type LksgData, type StoredCompany } from '@clients/backend';
import { admin_name, admin_pw, getBaseUrl, reader_name, reader_pw } from '@e2e/utils/Cypress.ts';
import { getKeycloakToken } from '@e2e/utils/Auth.ts';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload.ts';
import { uploadFrameworkDataForPublicToolboxFramework } from '@e2e/utils/FrameworkUpload.ts';
import LksgBaseFrameworkDefinition from '@/frameworks/lksg/BaseFrameworkDefinition';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { ExportFileTypeInformation } from '@/types/ExportFileTypeInformation.ts';
import { describeIf } from '@e2e/support/TestUtility.ts';
import { ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER } from '@/utils/Constants.ts';
import { humanizeStringOrNumber } from '@/utils/StringFormatter.ts';

describeIf(
  'As a user, I want to be able to download datasets from Dataland',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  () => {
    const reportingPeriod = '2021';
    const dataType = DataTypeEnum.Lksg;
    const minimumFileSizeInByte = 5000;
    const DOWNLOADS_FOLDER = Cypress.config('downloadsFolder');

    let storedCompany: StoredCompany;
    let lksgFixtureWithNoNullFields: FixtureData<LksgData>;

    /**
     * Checks that the downloaded file does actually exist
     * @param filePath path to file
     */
    function checkThatFileExists(filePath: string): void {
      cy.readFile(filePath, { timeout: Cypress.env('short_timeout_in_ms') as number }).should('exist');
    }

    /**
     * Deletes the file and checks that it is deleted
     * @param filePath path to file
     */
    function deleteFile(filePath: string): void {
      cy.task('deleteFile', filePath).then(() => {
        cy.readFile(filePath).should('not.exist');
      });
    }

    /**
     * Checks that the downloaded file has an appropriate size and delete afterward
     * @param filePath path to file
     */
    function checkFileSizeAndDeleteAfterwards(filePath: string): void {
      cy.task('getFileSize', filePath).then((size) => {
        expect(size).to.be.greaterThan(minimumFileSizeInByte);
      });
      deleteFile(filePath);
    }

    /**
     * Visit framework data page, select download format and click download button
     * @param fileType Needs to be one of the identifiers of an ExportFileTypes
     * @param includeAliases specifies if aliases are to be exported
     */
    function visitPageAndClickDownloadButton(fileType: string, includeAliases: boolean = false): void {
      const fileTypeMap: Record<string, string> = {
        JSON: 'JavaScript Object Notation (.json)',
        CSV: 'Comma-separated Values (.csv)',
        EXCEL: 'Excel File (.xlsx)',
      };

      cy.visit(getBaseUrl() + `/companies/${storedCompany.companyId}/frameworks/${dataType}`);

      cy.get('button[data-test=downloadDataButton]').should('exist').click();
      cy.get('[data-test="listOfReportingPeriods"]')
        .contains(reportingPeriod)
        .should('be.visible')
        .click({ force: true });
      if (!includeAliases) {
        cy.get('[data-test="includeAliasSwitch"]').should('have.class', 'p-inputswitch-checked').click({ force: true });
        cy.get('[data-test="includeAliasSwitch"]').should('not.have.class', 'p-inputswitch-checked');
      } else {
        cy.get('[data-test="includeAliasSwitch"]').should('have.class', 'p-inputswitch-checked');
      }

      const dropdownValue = fileTypeMap[fileType.toUpperCase()];
      if (!dropdownValue) {
        throw new Error(`Unsupported fileType: ${fileType}`);
      }
      cy.get('[data-test="fileTypeSelector"]').select(dropdownValue);
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
      const downloadsFolder = DOWNLOADS_FOLDER;

      cy.wait(Cypress.env('medium_timeout_in_ms') as number); // optional short delay
      cy.task('findFileByPrefix', {
        folder: downloadsFolder,
        prefix: partialFileNamePrefix,
        extension: fileExtension,
      }).then((filePath) => {
        const filePathStr = filePath as string;
        expect(filePathStr).to.exist;
        checkThatFileExists(filePathStr);
        checkFileSizeAndDeleteAfterwards(filePathStr);
      });
    }

    /**
     * Verifies that the downloaded file contains an alias when specified and a column header that is not an alias,
     * when export without aliases is selected
     *
     * @param partialFileNamePrefix
     * @param fileExtension
     * @param includeAliases - The file extension to match (e.g. 'csv', 'xlsx', 'json').
     */
    function verifyAliases(partialFileNamePrefix: string, fileExtension: string, includeAliases: boolean): void {
      const downloadsFolder = DOWNLOADS_FOLDER;
      cy.wait(Cypress.env('medium_timeout_in_ms') as number);
      cy.task('findFileByPrefix', {
        folder: downloadsFolder,
        prefix: partialFileNamePrefix,
        extension: fileExtension,
      }).then((filePath) => {
        if (typeof filePath === 'string') {
          cy.readFile(filePath).then((txt) => {
            if (includeAliases) {
              expect(txt).to.contains('COMPANY_NAME');
              expect(txt).to.not.contains('companyName');
            } else {
              expect(txt).to.contains('companyName');
              expect(txt).to.not.contains('COMPANY_NAME');
            }
          });
          deleteFile(filePath);
        }
      }); // optional short delay
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
      cy.fixture('CompanyInformationWithLksgPreparedFixtures').then((jsonContent) => {
        const preparedFixturesLksg = jsonContent as Array<FixtureData<LksgData>>;
        lksgFixtureWithNoNullFields = getPreparedFixture('lksg-all-fields', preparedFixturesLksg);
      });

      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        const uniqueCompanyMarker = Date.now().toString();
        const testStoredCompanyName = 'Company-Created-For-Download-Test-' + uniqueCompanyMarker;
        return uploadCompanyViaApi(token, generateDummyCompanyInformation(testStoredCompanyName)).then(
          (newStoredCompany) => {
            storedCompany = newStoredCompany;
            return uploadFrameworkDataForPublicToolboxFramework(
              LksgBaseFrameworkDefinition,
              token,
              storedCompany.companyId,
              reportingPeriod,
              lksgFixtureWithNoNullFields.t,
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
      const includeAliases = true;
      visitPageAndClickDownloadButton(ExportFileType.Csv.toString(), includeAliases);
      verifyAliases(`data-export-${frameworkLabel}`, ExportFileTypeInformation.CSV.fileExtension, includeAliases);
    });

    it('Download data as CSV file, check that a non-alias column name exists and delete it afterwards', () => {
      const frameworkLabel = getFrameworkLabel();
      const includeAliases = false;
      visitPageAndClickDownloadButton(ExportFileType.Csv.toString(), includeAliases);
      verifyAliases(`data-export-${frameworkLabel}`, ExportFileTypeInformation.CSV.fileExtension, includeAliases);
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
