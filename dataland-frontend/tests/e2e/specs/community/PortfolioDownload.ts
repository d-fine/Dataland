import { admin_name, admin_pw } from '@e2e/utils/Cypress';
import { describeIf } from '@e2e/support/TestUtility';
import { IdentifierType } from '@clients/backend';
import { getKeycloakToken } from '@e2e/utils/Auth';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { assertDefined } from '@/utils/TypeScriptUtils';

describeIf(
  'As a user I want to download a portfolio with selected reporting periods for a specified framework and file type',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  () => {
    const portfolioName = `Download Portfolio ${Date.now()}`;
    let permId: string;

    before(() => {
      getKeycloakToken(admin_name, admin_pw).then(async (token) => {
        const company = generateDummyCompanyInformation(`Company ${Date.now()}`);
        permId = assertDefined(company.identifiers[IdentifierType.PermId][0]);
        await uploadCompanyViaApi(token, company);
      });
    });

    beforeEach(() => {
      cy.ensureLoggedIn(admin_name, admin_pw);
      cy.visitAndCheckAppMount('/portfolios');
    });

    it('Creates a portfolio and downloads a report using dynamically loaded reporting periods', () => {
      cy.get('[data-test="addNewPortfolio"]').click();
      cy.get('[name="portfolioName"]').type(portfolioName);
      cy.get('[name="company-identifiers"]').type(permId);
      cy.get('[data-test="addCompanies"]').click();
      cy.get('[data-test="saveButton"]').click();

      cy.get('[data-test="portfolios"] [data-pc-name="tabpanel"]').contains(portfolioName).click();
      cy.get(`[data-test="portfolio-${portfolioName}"] [data-test="download-portfolio"]`).click();

      const frameworks = ['sfdr', 'eutaxonomy-financials', 'eutaxonomy-non-financials', 'nuclear-and-gas'];
      frameworks.forEach((framework) => {
        cy.get('[data-test="frameworkSelector"]').select(framework);
        cy.get('[data-test="frameworkSelector"]').should('have.value', framework);
      });

      cy.get('[data-test="frameworkSelector"]').select('SFDR');

      const reportingYears = ['2025', '2024', '2023', '2022', '2021', '2020'];
      reportingYears.forEach((year) => {
        cy.contains('.toggle-chip-group', year).should('exist');
      });

      ['2025', '2024', '2023'].forEach((year) => {
        cy.get('.toggle-chip-group .chip').contains(year).should('be.visible').click({ force: true });
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
