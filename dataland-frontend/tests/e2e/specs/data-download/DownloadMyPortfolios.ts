import { admin_name, admin_pw } from '@e2e/utils/Cypress.ts';
import { describeIf } from '@e2e/support/TestUtility.ts';
import {
  type DataMetaInformation,
  DataTypeEnum,
  type EutaxonomyNonFinancialsData,
  type StoredCompany,
} from '@clients/backend';
import { getKeycloakToken } from '@e2e/utils/Auth.ts';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload.ts';
import { assignCompanyOwnershipToDatalandAdmin } from '@e2e/utils/CompanyRolesUtils.ts';
import { uploadGenericFrameworkData } from '@e2e/utils/FrameworkUpload.ts';
import { getBasePublicFrameworkDefinition } from '@/frameworks/BasePublicFrameworkRegistry.ts';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures.ts';

let storedCompany: StoredCompany;
let secondCompany: StoredCompany;
let portfolioName: string;

const reportingYearsToSelect = ['2025', '2024', '2023', '2022', '2021', '2020'];
const unavailableYears = ['2021', '2020'];

let euTaxonomyForNonFinancialsFixtureForTest: FixtureData<EutaxonomyNonFinancialsData>;

/**
 * Uploads data for a given company for specified years using a generic framework.
 *
 * @param {string} token - The authentication token to access the API.
 * @param {StoredCompany} company - The company information including the company ID.
 * @param {string[]} years - An array of years for which the data is to be uploaded.
 * @return {Promise<DataMetaInformation[]>} A promise resolving to an array of additional information for the uploaded data.
 */
function uploadDataForCompany(token: string, company: StoredCompany, years: string[]): Promise<DataMetaInformation[]> {
  return Promise.all(
    years.map((year) =>
      uploadGenericFrameworkData(token, company.companyId, year, euTaxonomyForNonFinancialsFixtureForTest.t, (config) =>
        getBasePublicFrameworkDefinition(DataTypeEnum.EutaxonomyNonFinancials)!.getPublicFrameworkApiClient(config)
      )
    )
  );
}

/**
 * Sets up a company with provided data by uploading company information, assigning ownership,
 * and uploading associated data for specified years.
 *
 * @param {string} token - The authentication token used to access the API.
 * @param {string} companyName - The name of the company to be set up.
 * @param {number[]} years - An array of years for which data will be uploaded for the company.
 * @return {Promise<Object>} A promise that resolves to the created company object.
 */
function setupCompanyWithData(token: string, companyName: string, years: string[]): Promise<StoredCompany> {
  return uploadCompanyViaApi(token, generateDummyCompanyInformation(companyName)).then((company) => {
    return assignCompanyOwnershipToDatalandAdmin(token, company.companyId)
      .then(() => uploadDataForCompany(token, company, years))
      .then(() => company);
  });
}

/**
 * Creates a new portfolio with the specified companies and name.
 *
 * @param {StoredCompany} company1 - The first company to be included in the portfolio. Must contain a `companyId` property.
 * @param {StoredCompany} company2 - The second company to be included in the portfolio. Must contain a `companyId` property.
 * @param {string} portfolioName - The name of the portfolio to be created.
 * @return {void} This function does not return a value.
 */
function createPortfolio(company1: StoredCompany, company2: StoredCompany, portfolioName: string): void {
  cy.ensureLoggedIn(admin_name, admin_pw);
  cy.visitAndCheckAppMount('/portfolios');
  cy.get('[data-test="addNewPortfolio"]').click();
  cy.get('[name="portfolioName"]').type(portfolioName);
  cy.get('[name="company-identifiers"]').type(`${company1.companyId},${company2.companyId}`);
  cy.get('[data-test="addCompanies"]').click();
  cy.get('[data-test="saveButton"]').click();
}

/**
 * Tests the functionality for downloading a portfolio file with specified options.
 *
 * @param {Object} params - The parameters for the download operation.
 * @param {string} params.description - A description of the test scenario.
 * @param {'Comma-separated Values (.csv)' | 'Excel-compatible CSV File (.csv)'} params.fileType - The format in which the portfolio file should be downloaded.
 * @param {boolean} [params.keepValuesOnly=true] - Specifies whether to include additional information in the downloaded file.
 * @return {void} This function does not return a value.
 */
function testDownloadPortfolio({
  description,
  fileType,
  keepValuesOnly = true,
}: {
  description: string;
  fileType: 'Comma-separated Values (.csv)' | 'Excel File (.xlsx)';
  keepValuesOnly?: boolean;
}): void {
  it(description, () => {
    const downloadDir = Cypress.config('downloadsFolder');

    const fileExtension = getFileExtension(fileType);

    const partialFileNamePrefix = 'data-export-EU Taxonomy Non-Financials';

    cy.get('[data-test="fileTypeSelector"]').select(fileType);
    if (keepValuesOnly) {
      cy.get('[data-test="valuesOnlySwitch"]').click();
    }
    cy.get('[data-test="downloadDataButtonInModal"]').click();

    cy.wait(Cypress.env('medium_timeout_in_ms') as number);
    cy.task('findFileByPrefix', {
      folder: downloadDir,
      prefix: partialFileNamePrefix,
      extension: fileExtension,
    }).then((filePath) => {
      const filePathStr = filePath as string;
      expect(filePathStr).to.exist;

      cy.readFile(filePathStr, { timeout: Cypress.env('medium_timeout_in_ms') as number }).should('exist');

      cy.task('getFileSize', filePathStr).then((size) => {
        expect(size).to.be.greaterThan(5000);
      });

      cy.task('deleteFile', filePathStr).then(() => {
        cy.readFile(filePathStr).should('not.exist');
      });
    });
  });
}

/**
 * Extracts file extension from user's choice
 * @param fileType type of file to be downloaded
 */
function getFileExtension(fileType: string): string {
  switch (fileType) {
    case 'Comma-separated Values (.csv)':
      return 'csv';
    case 'Excel File (.xlsx)':
      return 'xlsx';
    default:
      throw new Error(`Unknown fileType: ${fileType}`);
  }
}

before(function () {
  cy.fixture('CompanyInformationWithEutaxonomyNonFinancialsPreparedFixtures.json').then((jsonContent) => {
    const preparedFixtures = jsonContent as Array<FixtureData<EutaxonomyNonFinancialsData>>;
    euTaxonomyForNonFinancialsFixtureForTest = getPreparedFixture(
      'all-fields-defined-for-eu-taxo-non-financials-alpha',
      preparedFixtures
    );
  });
});

describeIf(
  'As a user I want to download a portfolio with selected reporting periods for a specified framework and file type',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  () => {
    before(() => {
      const uniqueCompanyMarkerWithDate = Date.now().toString();
      const testCompanyName = 'Company-1-' + uniqueCompanyMarkerWithDate;
      const secondCompanyName = 'Company-2-' + uniqueCompanyMarkerWithDate;
      portfolioName = `Download Portfolio ${Date.now()}`;

      return getKeycloakToken(admin_name, admin_pw).then((token) => {
        return setupCompanyWithData(token, testCompanyName, ['2022', '2023', '2024'])
          .then((company1) => {
            storedCompany = company1;
            return setupCompanyWithData(token, secondCompanyName, ['2023', '2024']);
          })
          .then((company2) => {
            secondCompany = company2;
            createPortfolio(storedCompany, secondCompany, portfolioName);
          });
      });
    });

    beforeEach(() => {
      cy.ensureLoggedIn(admin_name, admin_pw);
      cy.visitAndCheckAppMount('/portfolios');
      cy.get('[data-test="portfolios"] [data-pc-name="tabpanel"]').contains(portfolioName).click();
      cy.get(`[data-test="portfolio-${portfolioName}"] [data-test="download-portfolio"]`).click();
      cy.get('[data-test="frameworkSelector"]').select('EU Taxonomy Non-Financials');
      reportingYearsToSelect.forEach((year) => {
        cy.get('[data-test="listOfReportingPeriods"]').contains(year).should('be.visible').click({ force: true });
      });
    });

    testDownloadPortfolio({
      description: 'Download the portfolio as a CSV file without additional information',
      fileType: 'Comma-separated Values (.csv)',
      keepValuesOnly: false,
    });

    testDownloadPortfolio({
      description: 'Download the portfolio as an Excel-compatible CSV file without additional information',
      fileType: 'Excel File (.xlsx)',
      keepValuesOnly: false,
    });

    testDownloadPortfolio({
      description: 'Download the portfolio as CSV file with additional information',
      fileType: 'Comma-separated Values (.csv)',
    });

    testDownloadPortfolio({
      description: 'Download the portfolio as an Excel-compatible CSV file with additional information',
      fileType: 'Excel File (.xlsx)',
    });

    it('Shows that not all reporting periods are clickable when data is missing', () => {
      cy.get('[data-test="frameworkSelector"]').select('EU Taxonomy Non-Financials');

      cy.get('[data-test="listOfReportingPeriods"]').should('be.visible');

      unavailableYears.forEach((year) => {
        cy.get('[data-test="listOfReportingPeriods"]').contains(year).parent().should('have.class', 'disabled');
      });
    });
  }
);
