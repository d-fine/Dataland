import { describeIf } from '@e2e/support/TestUtility.ts';
import {
  type DataMetaInformation,
  DataTypeEnum,
  type EutaxonomyNonFinancialsData,
  type PcafData,
  type StoredCompany,
} from '@clients/backend';
import { getAdminToken } from '@e2e/utils/Auth.ts';
import { generateDummyCompanyInformation, getOrUploadCompanyViaApi } from '@e2e/utils/CompanyUpload.ts';
import { assignCompanyOwnershipToDatalandAdmin } from '@e2e/utils/CompanyRolesUtils.ts';
import { uploadGenericFrameworkData } from '@e2e/utils/FrameworkUpload.ts';
import { getBasePublicFrameworkDefinition } from '@/frameworks/BasePublicFrameworkRegistry.ts';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures.ts';

type FileTypeLabel = 'Comma-separated Values (.csv)' | 'Excel File (.xlsx)' | 'JavaScript Object Notation (.json)';
type FileExtension = 'csv' | 'xlsx' | 'json';

/**
 * Describes a framework for which the portfolio download is tested.
 */
interface FrameworkTestConfig {
  /** The label of the framework in the download dialog */
  label: string;
  /** Loads the fixture that is uploaded for the companies of the portfolio */
  loadFixture: () => void;
  /** Uploads the loaded fixture for the given company and reporting period */
  upload: (token: string, companyId: string, year: string) => Promise<DataMetaInformation>;
  /** Values of the fixture that have to be contained in text based exports. If undefined, contents are not checked. */
  expectedValuesInExport?: string[];
  /** Minimum file size in bytes per file extension. Falls back to defaultMinimumFileSizeInByte. */
  minimumFileSizeInByte?: Partial<Record<FileExtension, number>>;
}

let euTaxonomyForNonFinancialsFixtureForTest: FixtureData<EutaxonomyNonFinancialsData>;
let pcafFixtureForTest: FixtureData<PcafData>;

const frameworkTestConfigs: FrameworkTestConfig[] = [
  {
    label: 'EU Taxonomy Non-Financials',
    loadFixture: (): void => {
      cy.fixture('CompanyInformationWithEutaxonomyNonFinancialsPreparedFixtures.json').then((jsonContent) => {
        const preparedFixtures = jsonContent as Array<FixtureData<EutaxonomyNonFinancialsData>>;
        euTaxonomyForNonFinancialsFixtureForTest = getPreparedFixture(
          'all-fields-defined-for-eu-taxo-non-financials-alpha',
          preparedFixtures
        );
      });
    },
    upload: (token, companyId, year) =>
      uploadGenericFrameworkData(token, companyId, year, euTaxonomyForNonFinancialsFixtureForTest.t, (config) =>
        getBasePublicFrameworkDefinition(DataTypeEnum.EutaxonomyNonFinancials)!.getPublicFrameworkApiClient(config)
      ),
  },
  {
    label: 'PCAF',
    loadFixture: (): void => {
      cy.fixture('CompanyInformationWithPcafPreparedFixtures.json').then((jsonContent) => {
        const preparedFixtures = jsonContent as Array<FixtureData<PcafData>>;
        pcafFixtureForTest = getPreparedFixture('All-fields-defined-for-PCAF-Framework-Company', preparedFixtures);
      });
    },
    upload: (token, companyId, year) =>
      uploadGenericFrameworkData(token, companyId, year, pcafFixtureForTest.t, (config) =>
        getBasePublicFrameworkDefinition(DataTypeEnum.Pcaf)!.getPublicFrameworkApiClient(config)
      ),
    // Values taken from the prepared PCAF fixture. They have to be contained in every text based export format.
    expectedValuesInExport: ['2024-11-30', 'Energy', 'Unlisted'],
    // The PCAF export has far fewer columns than other frameworks, so its files are smaller.
    minimumFileSizeInByte: { csv: 1000, xlsx: 3000 },
  },
];

const reportingYearsToSelect = ['2024', '2023', '2022'];
const unavailableYears = ['2025', '2021', '2020'];
const mediumTimeoutInMs = Number(Cypress.expose('medium_timeout_in_ms') ?? 30000);
const longTimeoutInMs = Number(Cypress.expose('long_timeout_in_ms') ?? 100000);
const defaultMinimumFileSizeInByte = 5000;

/**
 * Sets up a company by uploading company information, assigning ownership and uploading data for the given years.
 *
 * @param config the framework configuration providing the upload
 * @param token the authentication token used to access the API
 * @param companyName the name of the company to be set up
 * @param years the reporting periods for which data is uploaded
 * @returns a promise resolving to the created company
 */
function setupCompanyWithData(
  config: FrameworkTestConfig,
  token: string,
  companyName: string,
  years: string[]
): Promise<StoredCompany> {
  return getOrUploadCompanyViaApi(token, generateDummyCompanyInformation(companyName)).then((company) => {
    return assignCompanyOwnershipToDatalandAdmin(token, company.companyId)
      .then(() => Promise.all(years.map((year) => config.upload(token, company.companyId, year))))
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
function getFileExtension(fileType: FileTypeLabel): FileExtension {
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
 * The file is deleted before the assertion so that a failing check cannot leave a stale file behind.
 *
 * @param filePath the path of the downloaded file
 * @param minimumSizeInByte the size the file has to exceed
 */
function checkFileSizeAndDeleteAfterwards(filePath: string, minimumSizeInByte: number): void {
  cy.task('getFileSize', filePath).then((size) => {
    cy.task('deleteFile', filePath).then(() => {
      cy.readFile(filePath).should('not.exist');
      expect(size).to.be.greaterThan(minimumSizeInByte);
    });
  });
}

/**
 * Selects the file type and the export options in the open download dialog and starts the download.
 * By default the dialog has "Values only" and "Shorten field names" switched on.
 *
 * @param fileType the file type to download
 * @param useAliases whether "Shorten field names" shall be switched on
 * @param keepValuesOnly whether "Values only" shall be switched on
 */
function startDownload(fileType: FileTypeLabel, useAliases: boolean = true, keepValuesOnly: boolean = true): void {
  cy.get('[data-test="fileTypeSelector"]').find('.p-select-dropdown').click();
  cy.get('.p-select-list-container').contains(fileType).click();
  if (!keepValuesOnly) {
    // Switching off "Values only" also switches off "Shorten field names"
    cy.get('[data-test="valuesOnlySwitch"]').find('.p-toggleswitch-input').click();
    cy.get('[data-test="includeAliasSwitch"]').should('not.have.class', 'p-toggleswitch-checked');
  } else if (!useAliases) {
    cy.get('[data-test="includeAliasSwitch"]').find('.p-toggleswitch-input').click();
    cy.get('[data-test="includeAliasSwitch"]').should('not.have.class', 'p-toggleswitch-checked');
  }
  cy.get('[data-test="downloadDataButtonInModal"]').click();
}

/**
 * Waits for the download and passes the path of the downloaded file to the callback.
 *
 * @param label the label of the downloaded framework, which is part of the file name
 * @param fileType the file type that was downloaded
 * @param callback receives the path of the downloaded file
 */
function withDownloadedFile(label: string, fileType: FileTypeLabel, callback: (filePath: string) => void): void {
  cy.wait(longTimeoutInMs);
  cy.task('findFileByPrefix', {
    folder: Cypress.config('downloadsFolder'),
    prefix: `data-export-${label}`,
    extension: getFileExtension(fileType),
  }).then((filePath) => {
    const filePathStr = filePath as string;
    expect(filePathStr).to.exist;
    cy.readFile(filePathStr, { timeout: mediumTimeoutInMs }).should('exist');
    callback(filePathStr);
  });
}

/**
 * Checks that a text based export contains all expected values.
 *
 * @param filePath the path of the downloaded file
 * @param expectedValues the values that have to be contained
 */
function checkThatFileContainsExpectedValues(filePath: string, expectedValues: string[]): void {
  cy.readFile(filePath).then((content) => {
    const text = typeof content === 'string' ? content : JSON.stringify(content);
    for (const expectedValue of expectedValues) {
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

/**
 * Registers a test that downloads the portfolio in the given format and checks the downloaded file.
 *
 * @param config the framework configuration
 * @param description the description of the test
 * @param fileType the file type to download
 * @param useAliases whether "Shorten field names" shall be switched on
 * @param keepValuesOnly whether "Values only" shall be switched on
 * @param checkContent whether the content of the file is checked (only possible for text based formats)
 */
function testDownloadPortfolio(
  config: FrameworkTestConfig,
  description: string,
  fileType: FileTypeLabel,
  useAliases: boolean,
  keepValuesOnly: boolean,
  checkContent: boolean
): void {
  it(description, () => {
    startDownload(fileType, useAliases, keepValuesOnly);
    withDownloadedFile(config.label, fileType, (filePath) => {
      if (checkContent && config.expectedValuesInExport) {
        checkThatFileContainsExpectedValues(filePath, config.expectedValuesInExport);
        if (fileType === 'Comma-separated Values (.csv)' && keepValuesOnly) {
          checkAliasesInFile(filePath, useAliases);
        }
      }
      const minimumSize = config.minimumFileSizeInByte?.[getFileExtension(fileType)] ?? defaultMinimumFileSizeInByte;
      checkFileSizeAndDeleteAfterwards(filePath, minimumSize);
    });
  });
}

for (const config of frameworkTestConfigs) {
  describeIf(
    `As a user I want to download a ${config.label} portfolio with selected reporting periods and file type`,
    {
      executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
    },
    () => {
      let portfolioName: string;

      before(() => {
        config.loadFixture();
        const uniqueCompanyMarkerWithDate = Date.now().toString();
        const testCompanyName = `Company-1-${config.label}-${uniqueCompanyMarkerWithDate}`;
        const secondCompanyName = `Company-2-${config.label}-${uniqueCompanyMarkerWithDate}`;
        portfolioName = `Download ${config.label} Portfolio ${Date.now()}`;

        let adminToken: string;
        let firstCompany: StoredCompany;

        return getAdminToken()
          .then((token) => {
            adminToken = token;
            return setupCompanyWithData(config, token, testCompanyName, ['2022', '2023', '2024']);
          })
          .then((company1) => {
            firstCompany = company1;
            return setupCompanyWithData(config, adminToken, secondCompanyName, ['2023', '2024']);
          })
          .then((company2) => {
            createPortfolio(firstCompany, company2, portfolioName);
          });
      });

      beforeEach(() => {
        cy.ensureLoggedInAsAdmin();
        cy.visitAndCheckAppMount('/portfolios');
        cy.get(`[data-test="${portfolioName}"]`).contains(portfolioName).click();
        cy.get(`[data-test="portfolio-${portfolioName}"] [data-test="download-portfolio"]`).click();
        cy.get('[data-test="frameworkSelector"]').find('.p-select-dropdown').click();
        cy.get('.p-select-list-container').contains(config.label).click();
        cy.get('[data-test="latestReportingPeriodSwitch"]').click();
        cy.get('[data-test="reportingPeriodSelector"]').click();
        for (const year of reportingYearsToSelect) {
          cy.get('.p-multiselect-list').contains(year).click();
        }
        cy.get('body').click(0, 0);
      });

      testDownloadPortfolio(
        config,
        'Download the portfolio as a CSV file without additional information',
        'Comma-separated Values (.csv)',
        true,
        true,
        true
      );

      testDownloadPortfolio(
        config,
        'Download the portfolio as a CSV file without shortened field names',
        'Comma-separated Values (.csv)',
        false,
        true,
        true
      );

      testDownloadPortfolio(
        config,
        'Download the portfolio as a CSV file with additional information',
        'Comma-separated Values (.csv)',
        false,
        false,
        true
      );

      testDownloadPortfolio(
        config,
        'Download the portfolio as an Excel file without additional information',
        'Excel File (.xlsx)',
        true,
        true,
        false
      );

      testDownloadPortfolio(
        config,
        'Download the portfolio as an Excel file with additional information',
        'Excel File (.xlsx)',
        false,
        false,
        false
      );

      testDownloadPortfolio(
        config,
        'Download the portfolio as a JSON file',
        'JavaScript Object Notation (.json)',
        true,
        true,
        true
      );

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
}
