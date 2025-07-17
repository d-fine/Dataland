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
    let permIdOfSecondCompany: string;
    const portfolioName = `E2E Test Portfolio ${Date.now()}`;
    const editedPortfolioName = `${portfolioName} Edited ${Date.now()}`;

    before(() => {
      getKeycloakToken(admin_name, admin_pw).then(async (token) => {
        const companyToUpload = generateDummyCompanyInformation(`Test Co. ${Date.now()}`);
        permIdOfExistingCompany = assertDefined(companyToUpload.identifiers[IdentifierType.PermId][0]);
        await uploadCompanyViaApi(token, companyToUpload);
        const secondCompanyToUpload = generateDummyCompanyInformation(`Test Co.2 ${Date.now()}`);
        permIdOfSecondCompany = assertDefined(secondCompanyToUpload.identifiers[IdentifierType.PermId][0]);
        await uploadCompanyViaApi(token, secondCompanyToUpload);
      });
    });

    beforeEach(() => {
      cy.ensureLoggedIn(admin_name, admin_pw);
      cy.visitAndCheckAppMount('/portfolios');
      cy.intercept('POST', '**/community/requests/bulk').as('postBulkRequest');
      cy.intercept('GET', '**/users/portfolios/**/enriched-portfolio').as('getEnrichedPortfolio');
    });

    it('Creates, edits and deletes a portfolio', () => {
      cy.get('[data-test="add-portfolio"]').click();
      cy.get('[data-test="portfolio-name-input"]').type(portfolioName);
      cy.get('[data-test="saveButton"]').should('be.disabled');
      cy.get('[data-test="company-identifiers-input"]').type(permIdOfExistingCompany);
      cy.get('[data-test="addCompanies"]').click();
      cy.get('[data-test="saveButton"]').should('not.be.disabled');
      cy.get('[data-test="saveButton"]').click();
      cy.wait('@getEnrichedPortfolio');

      cy.get(`[data-test="${portfolioName}"]`).click();
      cy.get(`[data-test="portfolio-${portfolioName}"] [data-test="edit-portfolio"]`).click();
      cy.get('[data-test="portfolio-name-input"]').clear();
      cy.get('[data-test="portfolio-name-input"]').type(editedPortfolioName);
      cy.get('[data-test="company-identifiers-input"]').type(permIdOfSecondCompany);
      cy.get('[data-test="addCompanies"]').click();
      cy.get('[data-test="saveButton"]').click();
      cy.get(`[data-test="${editedPortfolioName}"]`).click();
      cy.get(`[data-test="portfolio-${editedPortfolioName}"] [data-test="edit-portfolio"]`).click();

      cy.window().then((win) => {
        cy.stub(win, 'confirm').returns(true);
      });
      cy.get('[data-test="deleteButton"]').click();
      cy.get(`[data-test="${editedPortfolioName}"]`).should('not.exist');
    });
  }
);
