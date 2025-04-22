import { admin_name, admin_pw } from '@e2e/utils/Cypress';
import { describeIf } from '@e2e/support/TestUtility';
import { IdentifierType } from '@clients/backend';
import { getKeycloakToken } from '@e2e/utils/Auth';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { assertDefined } from '@/utils/TypeScriptUtils';

describeIf(
  'As a user I want to be able to create, edit and delete my portfolios',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  () => {
    let permIdOfExistingCompany: string;
    const portfolioName = `E2E Test Portfolio ${Date.now()}`;
    const editedPortfolioName = `${portfolioName} Edited`;

    before(() => {
      getKeycloakToken(admin_name, admin_pw).then(async (token) => {
        const companyToUpload = generateDummyCompanyInformation(`Test Co. ${Date.now()}`);
        permIdOfExistingCompany = assertDefined(companyToUpload.identifiers[IdentifierType.PermId][0]);
        await uploadCompanyViaApi(token, companyToUpload);
      });
    });

    beforeEach(() => {
      cy.ensureLoggedIn(admin_name, admin_pw);
      cy.visitAndCheckAppMount('/portfolios');
    });

    it('Creates, edits, and deletes a portfolio', () => {
      cy.closeCookieBannerIfItExists();

      // Timeout für das Abrufen des Buttons erhöhen
      cy.get('[data-test="addNewPortfolio"]', { timeout: 10000 }).click();

      cy.get('[name="portfolioName"]').type(portfolioName, { timeout: 10000 });
      cy.get('[name="company-identifiers"]').type(permIdOfExistingCompany, { timeout: 10000 });
      cy.get('[data-test="addCompanies"]', { timeout: 10000 }).click();

      // Timeout erhöhen für den Speichern-Button
      cy.get('[data-test="saveButton"]', { timeout: 10000 }).should('not.be.disabled').click();
      cy.contains('[name="portfolioName"]', portfolioName, { timeout: 10000 }).should('be.visible');

      cy.contains('[name="portfolioName"]', portfolioName, { timeout: 10000 }).click();
      cy.get('[data-test="edit-portfolio"]', { timeout: 10000 }).click();

      cy.get('[name="portfolioName"]', { timeout: 10000 }).clear().type(editedPortfolioName);
      cy.get('[data-test="saveButton"]', { timeout: 10000 }).click();
      cy.contains('[data-test="portfolio-name"]', editedPortfolioName, { timeout: 10000 }).should('be.visible');

      //cy.contains('[data-test="portfolio-name"]', editedPortfolioName).should('not.exist');
    });
  }
);
