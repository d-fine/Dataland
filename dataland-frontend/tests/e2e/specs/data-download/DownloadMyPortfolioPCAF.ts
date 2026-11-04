import { describeIf } from '@e2e/support/TestUtility.ts';
import { type DataMetaInformation, DataTypeEnum, type PcafData, type StoredCompany } from '@clients/backend';
import { getAdminToken } from '@e2e/utils/Auth.ts';
import { generateDummyCompanyInformation, getOrUploadCompanyViaApi } from '@e2e/utils/CompanyUpload.ts';
import { assignCompanyOwnershipToDatalandAdmin } from '@e2e/utils/CompanyRolesUtils.ts';
import { uploadGenericFrameworkData } from '@e2e/utils/FrameworkUpload.ts';
import { getBasePublicFrameworkDefinition } from '@/frameworks/BasePublicFrameworkRegistry.ts';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures.ts';

type FileTypeLabel = 'Comma-separated Values (.csv)' | 'Excel File (.xlsx)' | 'JavaScript Object Notation (.json)';

let storedCompany: StoredCompany;
let secondCompany: StoredCompany;
let portfolioName: string;

let pcafFixtureForTest: FixtureData<PcafData>;

const reportingYearsToSelect = ['2024', '2023', '2022'];
const unavailableYears = ['2025', '2021', '2020'];
const mediumTimeoutInMs = Number(Cypress.expose('medium_timeout_in_ms') ?? 30000);
const minimumFileSizeInByte = 5000;
const partialFileNamePrefix = 'data-export-PCAF';

// Values taken from the prepared PCAF fixture. They have to be contained in every export format.
const expectedValuesInExport = ['2024-11-30', 'Energy', 'Unlisted'];

/**
 * Uploads PCAF data for a given company for the specified years.
 *
 * @param token the authentication token to access the API
 * @param company the company to upload the data for
 * @param years the reporting periods for which the data is uploaded
 * @returns a promise resolving to the meta information of the uploaded datasets
 */
function uploadDataForCompany(token: string, company: StoredCompany, years: string[]): Promise<DataMetaInformation[]> {
  return Promise.all(
    years.map((year) =>
      uploadGenericFrameworkData(token, company.companyId, year, pcafFixtureForTest.t, (config) =>
        getBasePublicFrameworkDefinition(DataTypeEnum.Pcaf)!.getPublicFrameworkApiClient(config)
      )
    )
  );
}

/**
 * Uploads a company, makes the admin its owner and uploads PCAF data for the given years.
 *
 * @param token the authentication token used to access the API
 * @param companyName the name of the company to be set up
 * @param years the reporting periods for which PCAF data is uploaded
 * @returns a promise resolving to the created company
 */
function setupCompanyWithData(token: string, companyName: string, years: string[]): Promise<StoredCompany> {
  return getOrUploadCompanyViaApi(token, generateDummyCompanyInformation(companyName)).then((company) => {
    return assignCompanyOwnershipToDatalandAdmin(token, company.companyId)
      .then(() => uploadDataForCompany(token, company, years))
      .then(() => company);
  });
}

/**
 * Creates a new portfolio containing the two given companies.
 *
 * @param company1 the first company of the portfolio
 * @param company2 the second company of the portfolio
 * @param name the name of the portfolio
 */
function createPortfolio(company1: StoredCompany, company2: StoredCompany, name: string): void {
  cy.ensureLoggedInAsAdmin();
  cy.visitAndCheckAppMount('/portfolios');
  cy.get('[data-test="add-portfolio"]').click();
  cy.get('[data-test="portfolio-name-input"]:visible').invoke('val', name).trigger('input');
  cy.get('[data-test="company-identifiers-input"]')
    .invoke('val', `${company1.companyId},${company2.companyId}`)
    .trigger('input');
  cy.get('[data-test="portfolio-dialog-add-companies"]').click();
  cy.waitUntil(() => cy.get('[data-test="company-identifiers-input"]:visible').should('be.empty'));
  cy.get('[data-test="portfolio-dialog-save-button"]').should('not.be.disabled').click();
}

/**
 * Returns the file extension belonging to the file type label shown in the download dialog.
 *
 * @param fileType the file type label
 */
function getFileExtension(fileType: FileTypeLabel): string {
  switch (fileType) {
    case 'Comma-separated Values (.csv)':
      return 'csv';
    case 'Excel File (.xlsx)':
      return 'xlsx';
    case 'JavaScript Object Notation (.json)':
      return 'json';
  }
}

/**
 * Checks that the file is larger than the minimum expected size, deletes it and verifies that it is gone.
 *
 * @param filePath the path of the downloaded file
 */
function checkFileSizeAndDeleteAfterwards(filePath: string): void {
  cy.task('getFileSize', filePath).then((size) => {
    expect(size).to.be.greaterThan(minimumFileSizeInByte);
  });
  deleteFile(filePath);
}

/**
 * Deletes the file and checks that it is gone.
 *
 * @param filePath the path of the downloaded file
 */
function deleteFile(filePath: string): void {
  cy.task('deleteFile', filePath).then(() => {
    cy.readFile(filePath).should('not.exist');
  });
}

/**
 * Selects the file type and the export options in the open download dialog and starts the download.
 * By default the dialog has "Values only" and "Shorten field names" switched on.
 *
 * @param fileType the file type to download
 * @param options whether "Shorten field names" and "Values only" shall be switched on
 */
function startDownload(
  fileType: FileTypeLabel,
  options: { useAliases: boolean; keepValuesOnly: boolean } = { useAliases: true, keepValuesOnly: true }
): void {
  cy.get('[data-test="fileTypeSelector"]').find('.p-select-dropdown').click();
  cy.get('.p-select-list-container').contains(fileType).click();
  if (!options.keepValuesOnly) {
    // Switching off "Values only" also switches off "Shorten field names"
    cy.get('[data-test="valuesOnlySwitch"]').find('.p-toggleswitch-input').click();
    cy.get('[data-test="includeAliasSwitch"]').should('not.have.class', 'p-toggleswitch-checked');
  } else if (!options.useAliases) {
    cy.get('[data-test="includeAliasSwitch"]').find('.p-toggleswitch-input').click();
    cy.get('[data-test="includeAliasSwitch"]').should('not.have.class', 'p-toggleswitch-checked');
  }
  cy.get('[data-test="downloadDataButtonInModal"]').click();
}

/**
 * Waits for the download and passes the path of the downloaded file to the callback.
 *
 * @param fileType the file type that was downloaded
 * @param callback receives the path of the downloaded file
 */
function withDownloadedFile(fileType: FileTypeLabel, callback: (filePath: string) => void): void {
  cy.wait(mediumTimeoutInMs);
  cy.task('findFileByPrefix', {
    folder: Cypress.config('downloadsFolder'),
    prefix: partialFileNamePrefix,
    extension: getFileExtension(fileType),
  }).then((filePath) => {
    const filePathStr = filePath as string;
    expect(filePathStr).to.exist;
    cy.readFile(filePathStr, { timeout: mediumTimeoutInMs }).should('exist');
    callback(filePathStr);
  });
}

/**
 * Checks that a text based export (CSV or JSON) contains all expected PCAF values.
 *
 * @param filePath the path of the downloaded file
 */
function checkThatFileContainsExpectedValues(filePath: string): void {
  cy.readFile(filePath).then((content) => {
    const text = typeof content === 'string' ? content : JSON.stringify(content);
    for (const expectedValue of expectedValuesInExport) {
      expect(text).to.contain(expectedValue);
    }
  });
}

/**
 * Checks whether the column headers in the file are aliases (e.g. COMPANY_NAME) or plain names (e.g. companyName).
 *
 * @param filePath the path of the downloaded file
 * @param useAliases whether aliases are expected
 */
function checkAliasesInFile(filePath: string, useAliases: boolean): void {
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

before(function () {
  cy.fixture('CompanyInformationWithPcafPreparedFixtures.json').then((jsonContent) => {
    const preparedFixtures = jsonContent as Array<FixtureData<PcafData>>;
    pcafFixtureForTest = getPreparedFixture('All-fields-defined-for-PCAF-Framework-Company', preparedFixtures);
  });
});

describeIf(
  'As a user I want to download PCAF data of a portfolio with selected reporting periods as CSV, Excel and JSON',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  () => {
    before(() => {
      const uniqueCompanyMarkerWithDate = Date.now().toString();
      const testCompanyName = 'Company-1-' + uniqueCompanyMarkerWithDate;
      const secondCompanyName = 'Company-2-' + uniqueCompanyMarkerWithDate;
      portfolioName = `Download PCAF Portfolio ${Date.now()}`;

      let adminToken: string;

      return getAdminToken()
        .then((token) => {
          adminToken = token;
          return setupCompanyWithData(token, testCompanyName, ['2022', '2023', '2024']);
        })
        .then((company1) => {
          storedCompany = company1;
          return setupCompanyWithData(adminToken, secondCompanyName, ['2023', '2024']);
        })
        .then((company2) => {
          secondCompany = company2;
        })
        .then(() => {
          createPortfolio(storedCompany, secondCompany, portfolioName);
        });
    });

    beforeEach(() => {
      cy.ensureLoggedInAsAdmin();
      cy.visitAndCheckAppMount('/portfolios');
      cy.get(`[data-test="${portfolioName}"]`).contains(portfolioName).click();
      cy.get(`[data-test="portfolio-${portfolioName}"] [data-test="download-portfolio"]`).click();
      cy.get('[data-test="frameworkSelector"]').find('.p-select-dropdown').click();
      cy.get('.p-select-list-container').contains('PCAF').click();
      cy.get('[data-test="latestReportingPeriodSwitch"]').click();
      cy.get('[data-test="reportingPeriodSelector"]').click();
      for (const year of reportingYearsToSelect) {
        cy.get('.p-multiselect-list').contains(year).click();
      }
      cy.get('body').click(0, 0);
    });

    it('Download PCAF data as CSV file with shortened field names and check its content', () => {
      startDownload('Comma-separated Values (.csv)');
      withDownloadedFile('Comma-separated Values (.csv)', (filePath) => {
        checkThatFileContainsExpectedValues(filePath);
        checkAliasesInFile(filePath, true);
        checkFileSizeAndDeleteAfterwards(filePath);
      });
    });

    it('Download PCAF data as CSV file without shortened field names and check its content', () => {
      startDownload('Comma-separated Values (.csv)', { useAliases: false, keepValuesOnly: true });
      withDownloadedFile('Comma-separated Values (.csv)', (filePath) => {
        checkThatFileContainsExpectedValues(filePath);
        checkAliasesInFile(filePath, false);
        checkFileSizeAndDeleteAfterwards(filePath);
      });
    });

    it('Download PCAF data as CSV file with additional information (not only values)', () => {
      startDownload('Comma-separated Values (.csv)', { useAliases: false, keepValuesOnly: false });
      withDownloadedFile('Comma-separated Values (.csv)', (filePath) => {
        checkThatFileContainsExpectedValues(filePath);
        checkFileSizeAndDeleteAfterwards(filePath);
      });
    });

    it('Download PCAF data as Excel file and check its size', () => {
      startDownload('Excel File (.xlsx)');
      withDownloadedFile('Excel File (.xlsx)', (filePath) => {
        checkFileSizeAndDeleteAfterwards(filePath);
      });
    });

    it('Download PCAF data as JSON file and check its content', () => {
      startDownload('JavaScript Object Notation (.json)');
      withDownloadedFile('JavaScript Object Notation (.json)', (filePath) => {
        checkThatFileContainsExpectedValues(filePath);
        checkFileSizeAndDeleteAfterwards(filePath);
      });
    });

    it('Shows that unavailable reporting periods are disabled in the dropdown', () => {
      cy.get('[data-test="reportingPeriodSelector"]').click();
      for (const year of unavailableYears) {
        cy.get('.p-multiselect-list').contains(year).parent().should('have.attr', 'data-p-disabled', 'true');
      }
    });

    it('Verifies latest reporting period toggle controls reporting period selector', () => {
      cy.get('[data-test="latestReportingPeriodSwitch"]').click();
      cy.get('[data-test="reportingPeriodSelector"]').should('have.class', 'p-disabled');
      cy.get('[data-test="latestReportingPeriodSwitch"]').click();
      cy.get('[data-test="reportingPeriodSelector"]').should('not.have.class', 'p-disabled');
    });
  }
);
