import { admin_name, admin_pw } from '@e2e/utils/Cypress';
import { describeIf } from '@e2e/support/TestUtility';
import { DataTypeEnum, type EutaxonomyNonFinancialsData, type StoredCompany } from '@clients/backend';
import { getKeycloakToken } from '@e2e/utils/Auth';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { assignCompanyOwnershipToDatalandAdmin } from '@e2e/utils/CompanyRolesUtils.ts';
import { uploadGenericFrameworkData } from '@e2e/utils/FrameworkUpload.ts';
import { getBasePublicFrameworkDefinition } from '@/frameworks/BasePublicFrameworkRegistry.ts';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures.ts';
import { join } from 'path';

let storedCompany: StoredCompany;
let secondCompany: StoredCompany;
let portfolioName: string;

const reportingYearsToSelect = ['2025', '2024', '2023', '2022', '2021', '2020'];
const unavailableYears = ['2021', '2020'];

let euTaxonomyForNonFinancialsFixtureForTest: FixtureData<EutaxonomyNonFinancialsData>;

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

      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName))
          .then((company1) => {
            storedCompany = company1;
            return assignCompanyOwnershipToDatalandAdmin(token, company1.companyId).then(() => {
              return Promise.all(
                ['2022', '2023', '2024'].map((year) =>
                  uploadGenericFrameworkData(
                    token,
                    company1.companyId,
                    year,
                    euTaxonomyForNonFinancialsFixtureForTest.t,
                    (config) =>
                      getBasePublicFrameworkDefinition(
                        DataTypeEnum.EutaxonomyNonFinancials
                      )!.getPublicFrameworkApiClient(config)
                  )
                )
              );
            });
          })
          .then(() =>
            uploadCompanyViaApi(token, generateDummyCompanyInformation(secondCompanyName)).then((company2) => {
              secondCompany = company2;
              return assignCompanyOwnershipToDatalandAdmin(token, company2.companyId).then(() => {
                return Promise.all(
                  ['2023', '2024'].map((year) =>
                    uploadGenericFrameworkData(
                      token,
                      company2.companyId,
                      year,
                      euTaxonomyForNonFinancialsFixtureForTest.t,
                      (config) =>
                        getBasePublicFrameworkDefinition(
                          DataTypeEnum.EutaxonomyNonFinancials
                        )!.getPublicFrameworkApiClient(config)
                    )
                  )
                );
              });
            })
          )
          .then(() => {
            cy.ensureLoggedIn(admin_name, admin_pw);
            cy.visitAndCheckAppMount('/portfolios');
            cy.get('[data-test="addNewPortfolio"]').click();
            cy.get('[name="portfolioName"]').type(portfolioName);
            cy.get('[name="company-identifiers"]').type(`${storedCompany.companyId},${secondCompany.companyId}`);
            cy.get('[data-test="addCompanies"]').click();
            cy.get('[data-test="saveButton"]').click();
          });
      });
    });

    beforeEach(() => {
      cy.ensureLoggedIn(admin_name, admin_pw);
      cy.visitAndCheckAppMount('/portfolios');
      cy.get('[data-test="portfolios"] [data-pc-name="tabpanel"]').contains(portfolioName).click();
      cy.get(`[data-test="portfolio-${portfolioName}"] [data-test="download-portfolio"]`).click();
      cy.get('[data-test="frameworkSelector"]').select('EU Taxonomy Non Financials');
      reportingYearsToSelect.forEach((year) => {
        cy.get('[data-test="listOfReportingPeriods"]').contains(year).should('be.visible').click({ force: true });
      });
    });

    /**
     * Tests the functionality for downloading a portfolio file with specified options.
     *
     * @param {Object} params - The parameters for the download operation.
     * @param {string} params.description - A description of the test scenario.
     * @param {'Comma-separated Values (.csv)' | 'Excel-compatible CSV File (.csv)'} params.fileType - The format in which the portfolio file should be downloaded.
     * @param {boolean} [params.includeMetaData=false] - Specifies whether to include metadata in the downloaded file.
     * @return {void} This function does not return a value.
     */
    function testDownloadPortfolio({
      description,
      fileType,
      includeMetaData = false,
    }: {
      description: string;
      fileType: 'Comma-separated Values (.csv)' | 'Excel-compatible CSV File (.csv)';
      includeMetaData?: boolean;
    }): void {
      it(description, () => {
        const downloadDir = Cypress.config('downloadsFolder');
        const expectedFileName = `Portfolio-${portfolioName}-eutaxonomy-non-financials.csv`;
        const downloadFilePath = join(downloadDir, expectedFileName);
        const minimumFileSizeInByte = 5000;

        cy.get('[data-test="fileTypeSelector"]').select(fileType);
        if (includeMetaData) {
          cy.get('[data-test="includeMetaData"]').click();
        }
        cy.get('[data-test="downloadButton"]').click();

        cy.readFile(downloadFilePath, { timeout: 15000 }).should('exist');
        cy.task('getFileSize', downloadFilePath).then((size) => {
          expect(size).to.be.greaterThan(minimumFileSizeInByte);
        });

        cy.task('deleteFile', downloadFilePath).then(() => {
          cy.readFile(downloadFilePath, { timeout: 5000 }).should('not.exist');
        });
      });
    }

    testDownloadPortfolio({
      description: 'Download the portfolio as a CSV file without Meta Data',
      fileType: 'Comma-separated Values (.csv)',
    });

    testDownloadPortfolio({
      description: 'Download the portfolio as an Excel-compatible CSV file without Meta Data',
      fileType: 'Excel-compatible CSV File (.csv)',
    });

    testDownloadPortfolio({
      description: 'Download the portfolio as CSV file with Meta Data',
      fileType: 'Comma-separated Values (.csv)',
      includeMetaData: true,
    });

    testDownloadPortfolio({
      description: 'Download the portfolio as an Excel-compatible CSV file with Meta Data',
      fileType: 'Excel-compatible CSV File (.csv)',
      includeMetaData: true,
    });

    it('Shows error message when no data is available for selected years', () => {
      cy.intercept('GET', '**/api/data/eutaxonomy-non-financials/export**', (req) => {
        req.reply((res) => {
          res.send({
            statusCode: 500,
            body: {},
          });
        });
      }).as('downloadRequest');

      reportingYearsToSelect.forEach((year) => {
        cy.get('[data-test="listOfReportingPeriods"]').contains(year).click({ force: true });
      });

      unavailableYears.forEach((year) => {
        cy.get('[data-test="listOfReportingPeriods"]').contains(year).click({ force: true });
      });

      cy.get('[data-test="fileTypeSelector"]').select('Excel-compatible CSV File (.csv)');
      cy.get('[data-test="downloadButton"]').click();

      cy.wait('@downloadRequest');
      cy.get('.p-message-error, [data-test="portfolio-download-content"]')
        .contains('No data available', { timeout: 10000 })
        .should('be.visible');
    });
  }
);
