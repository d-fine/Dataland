import { join } from 'path';
import { DataTypeEnum, ExportFileType, type LksgData, type StoredCompany } from '@clients/backend';
import { admin_name, admin_pw, getBaseUrl, reader_name, reader_pw } from '@e2e/utils/Cypress.ts';
import { getKeycloakToken } from '@e2e/utils/Auth.ts';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload.ts';
import { uploadFrameworkDataForPublicToolboxFramework } from '@e2e/utils/FrameworkUpload.ts';
import LksgBaseFrameworkDefinition from '@/frameworks/lksg/BaseFrameworkDefinition';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { ExportFileTypeInformation } from '@/types/ExportFileTypeInformation.ts';
import { describeIf } from '@e2e/support/TestUtility.ts';

describeIf(
  'As a user, I want to be able to download datasets from Dataland',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  () => {
    const reportingPeriod = '2021';
    const dataType = DataTypeEnum.Lksg;
    const minimumFileSizeInByte = 5000;

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
     * Checks that the downloaded file has an appropriate size and delete afterward
     * @param filePath path to file
     */
    function checkFileSizeAndDeleteAfterwards(filePath: string): void {
      cy.task('getFileSize', filePath).then((size) => {
        expect(size).to.be.greaterThan(minimumFileSizeInByte);
      });

      cy.task('deleteFile', filePath).then(() => {
        cy.readFile(filePath).should('not.exist');
      });
    }

    /**
     * Visit framework data page, select download format and click download button
     * @param fileType Needs to be one of the identifiers of an ExportFileTypes
     */
    function visitPageAndClickDownloadButton(fileType: string): void {
      cy.visit(getBaseUrl() + `/companies/${storedCompany.companyId}/frameworks/${dataType}`);

      cy.get('button[data-test=downloadDataButton]').should('exist').click();
      cy.get('[data-test=downloadModal]')
        .should('exist')
        .within(() => {
          cy.get('[data-test="reportingYearSelector"]').select(reportingPeriod);
          cy.get('[data-test="fileTypeSelector"]').select(fileType);
          cy.get('button[data-test=downloadDataButtonInModal]').click();
        });
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

    it('Download data as csv file, check for appropriate size and delete it afterwards', () => {
      const exportFileType = ExportFileType.Csv;
      const fileTypeInformation = ExportFileTypeInformation.CSV;
      const fileName = `${reportingPeriod}-${dataType}-${storedCompany.companyId}.${fileTypeInformation.fileExtension}`;

      visitPageAndClickDownloadButton(exportFileType.toString());

      const filePath = join(Cypress.config('downloadsFolder'), fileName);
      checkThatFileExists(filePath);
      checkFileSizeAndDeleteAfterwards(filePath);
    });

    it('Download data as Excel-compatible csv file, check for appropriate size and delete it afterwards', () => {
      const exportFileType = ExportFileType.Excel;
      const fileTypeInformation = ExportFileTypeInformation.EXCEL;
      const fileName = `${reportingPeriod}-${dataType}-${storedCompany.companyId}.${fileTypeInformation.fileExtension}`;

      visitPageAndClickDownloadButton(exportFileType.toString());

      const filePath = join(Cypress.config('downloadsFolder'), fileName);
      checkThatFileExists(filePath);

      const termToCheck = 'sep=,';
      cy.task('checkFileContent', { path: filePath, term: termToCheck }).then((isFound) => {
        expect(isFound).to.be.true;
      });
      checkFileSizeAndDeleteAfterwards(filePath);
    });

    it('Download data as json file, check for appropriate size and delete it afterwards', () => {
      const exportFileType = ExportFileType.Json;
      const fileTypeInformation = ExportFileTypeInformation.JSON;
      const fileName = `${reportingPeriod}-${dataType}-${storedCompany.companyId}.${fileTypeInformation.fileExtension}`;

      visitPageAndClickDownloadButton(exportFileType.toString());

      const filePath = join(Cypress.config('downloadsFolder'), fileName);
      checkThatFileExists(filePath);
      checkFileSizeAndDeleteAfterwards(filePath);
    });
  }
);
