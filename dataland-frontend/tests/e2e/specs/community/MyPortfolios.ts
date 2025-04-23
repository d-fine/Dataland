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

      cy.get('[data-test="addNewPortfolio"]').click();

      cy.get('[name="portfolioName"]').type(portfolioName);
      cy.get('[name="company-identifiers"]').type(permIdOfExistingCompany);
      cy.get('[data-test="addCompanies"]').click();

      cy.get('[data-test="saveButton"]').should('not.be.disabled');
      cy.get('[data-test="saveButton"]').click();
      cy.contains('[name="portfolioName"]', portfolioName).should('be.visible');

      cy.contains('[name="portfolioName"]', portfolioName).click();
      cy.get('[data-test="edit-portfolio"]').click();

      cy.get('[name="portfolioName"]').clear();
      cy.get('[name="portfolioName"]').type(editedPortfolioName);
      cy.get('[data-test="saveButton"]').click();
      cy.contains('[data-test="portfolio-name"]', editedPortfolioName).should('be.visible');
    });
  }
);
