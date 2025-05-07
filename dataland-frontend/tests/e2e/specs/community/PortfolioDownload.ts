import { admin_name, admin_pw } from '@e2e/utils/Cypress';
import { describeIf } from '@e2e/support/TestUtility';
import { DataTypeEnum, type EutaxonomyNonFinancialsData, type StoredCompany } from '@clients/backend';
import { getKeycloakToken } from '@e2e/utils/Auth';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { assignCompanyOwnershipToDatalandAdmin } from '@e2e/utils/CompanyRolesUtils.ts';
import { uploadGenericFrameworkData } from '@e2e/utils/FrameworkUpload.ts';
import { getBasePublicFrameworkDefinition } from '@/frameworks/BasePublicFrameworkRegistry.ts';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures.ts';

let storedCompany: StoredCompany;
let secondCompany: StoredCompany;


let euTaxonomyForNonFinancialsFixtureForTest: FixtureData<EutaxonomyNonFinancialsData>;
before(function () {
  cy.fixture('CompanyInformationWithEutaxonomyNonFinancialsPreparedFixtures.json').then(function (jsonContent) {
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
    const portfolioName = `Download Portfolio ${Date.now()}`;

    before(() => {
      const uniqueCompanyMarkerWithDate = Date.now().toString();
      const testCompanyName = 'Company-1-' + uniqueCompanyMarkerWithDate;
      const secondCompanyName = 'Company-2-' + uniqueCompanyMarkerWithDate;
      const reportingYears = ['2021', '2022', '2023'];

      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName)).then((company1) => {
          storedCompany = company1;
          return assignCompanyOwnershipToDatalandAdmin(token, company1.companyId).then(() => {
            return Promise.all(
              reportingYears.map((year) =>
                uploadGenericFrameworkData(
                  token,
                  company1.companyId,
                  year,
                  euTaxonomyForNonFinancialsFixtureForTest.t,
                  (config) =>
                    getBasePublicFrameworkDefinition(DataTypeEnum.EutaxonomyNonFinancials)!.getPublicFrameworkApiClient(config)
                )
              )
            );
          });
        }).then(() => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation(secondCompanyName)).then((company2) => {
            secondCompany = company2;
            return assignCompanyOwnershipToDatalandAdmin(token, company2.companyId).then(() => {
              return Promise.all(
                reportingYears.map((year) =>
                  uploadGenericFrameworkData(
                    token,
                    company2.companyId,
                    year,
                    euTaxonomyForNonFinancialsFixtureForTest.t,
                    (config) =>
                      getBasePublicFrameworkDefinition(DataTypeEnum.EutaxonomyNonFinancials)!.getPublicFrameworkApiClient(config)
                  )
                )
              );
            });
          });
        });
      });
    });

    beforeEach(() => {
      cy.ensureLoggedIn(admin_name, admin_pw);
      cy.visitAndCheckAppMount('/portfolios');
    });

    it('Creates a portfolio and downloads a report using dynamically loaded reporting periods', () => {
      cy.get('[data-test="addNewPortfolio"]').click();
      cy.get('[name="portfolioName"]').type(portfolioName);
      cy.get('[name="company-identifiers"]').type(`${storedCompany.companyId},${secondCompany.companyId}`);
      cy.get('[data-test="addCompanies"]').click();
      cy.get('[data-test="saveButton"]').click();

      cy.get('[data-test="portfolios"] [data-pc-name="tabpanel"]').contains(portfolioName).click();
      cy.get(`[data-test="portfolio-${portfolioName}"] [data-test="download-portfolio"]`).click();

      const frameworks = ['sfdr', 'eutaxonomy-financials', 'eutaxonomy-non-financials', 'nuclear-and-gas'];
      frameworks.forEach((framework) => {
        cy.get('[data-test="frameworkSelector"]').select(framework);
        cy.get('[data-test="frameworkSelector"]').should('have.value', framework);
      });

      cy.get('[data-test="frameworkSelector"]').select('EU Taxonomy Non Financials');

      const reportingYears = ['2025', '2024', '2023', '2022', '2021', '2020'];
      reportingYears.forEach((year) => {
        cy.contains('.toggle-chip-group', year).should('exist');
      });

      ['2023','2022','2021'].forEach((year) => {
        cy.get('[data-test="listOfReportingPeriods"]').contains(year).should('be.visible').click({ force: true });
      });

      const fileTypes = [
        { label: 'Comma-separated Values (.csv)', value: 'CSV' },
        { label: 'Excel-compatible CSV File (.csv)', value: 'EXCEL' },
      ];

      fileTypes.forEach((type) => {
        cy.get('[data-test="fileTypeSelector"]').select(type.label);
        cy.get('[data-test="fileTypeSelector"]').should('have.value', type.value);
      });

      cy.get('[data-test="fileTypeSelector"]').select('Comma-separated Values (.csv)');
      cy.get('[data-test="downloadButton"]').click();
    });
  }
);
