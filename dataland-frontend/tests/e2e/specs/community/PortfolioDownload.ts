import { admin_name, admin_pw } from '@e2e/utils/Cypress';
import { describeIf } from '@e2e/support/TestUtility';
import { IdentifierType } from '@clients/backend';
import { getKeycloakToken } from '@e2e/utils/Auth';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { assertDefined } from '@/utils/TypeScriptUtils';

describeIf(
  'As a user I want to download a portfolio report file with dynamically loaded reporting periods',
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

    it('Creates a portfolio and downloads data using dynamically loaded reporting periods', () => {
      cy.closeCookieBannerIfItExists();

      cy.get('[data-test="addNewPortfolio"]').click();
      cy.get('[name="portfolioName"]').type(portfolioName);
      cy.get('[name="company-identifiers"]').type(permId);
      cy.get('[data-test="addCompanies"]').click();
      cy.get('[data-test="saveButton"]').click();

      cy.get('[data-test="portfolios"] [data-pc-name="tabpanel"]').contains(portfolioName).click();
      cy.get(`[data-test="portfolio-${portfolioName}"] [data-test="download-portfolio"]`).click();

      cy.get('[name="FrameworkSelection"]').click();
      cy.get('.p-dropdown-item').contains('SFDR').click();

      cy.get('.toggle-chip', { timeout: 5000 }).should('exist');

      cy.get('.toggle-chip').first().click();

      cy.get('[name="fileType"]').click();
      cy.get('.p-dropdown-item').contains('CSV').click();

      cy.window().then((win) => {
        cy.stub(win, 'Blob');
        cy.stub(win.URL, 'createObjectURL').returns('blob:http://localhost/fake-download-url');
      });

      cy.get('[data-test="downloadButton"]').should('not.be.disabled').click();

      cy.get('[data-test="errorMessage"]').should('not.exist');
    });
  }
);
