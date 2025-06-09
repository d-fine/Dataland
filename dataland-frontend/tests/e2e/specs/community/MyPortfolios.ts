import { admin_name, admin_pw } from '@e2e/utils/Cypress';
import { describeIf } from '@e2e/support/TestUtility';
import { IdentifierType } from '@clients/backend';
import { getKeycloakToken } from '@e2e/utils/Auth';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { EU_TAXONOMY_FRAMEWORKS } from '@/utils/Constants.ts';

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
    });

    it('Creates, edits, monitores and deletes a portfolio', () => {
      cy.get('[data-test="addNewPortfolio"]').click();
      cy.get('[name="portfolioName"]').type(portfolioName);
      cy.get('[data-test="saveButton"]').should('be.disabled');
      cy.get('[name="company-identifiers"]').type(permIdOfExistingCompany);
      cy.get('[data-test="addCompanies"]').click();
      cy.get('[data-test="saveButton"]').should('not.be.disabled');
      cy.get('[data-test="saveButton"]').click();

      cy.get('[data-test="monitor-portfolio"]').filter(':visible').click();
      cy.get('[data-test="listOfReportingPeriods"]').click();
      cy.get('.p-dropdown-item').contains('2023').click();

      cy.get('[data-test="frameworkSelection"]')
        .contains('EU Taxonomy')
        .parent()
        .find('input[type="checkbox"]')
        .click({ force: true });

      cy.get('[data-test="saveChangesButton"]').click();

      cy.wait('@postBulkRequest')
        .its('request.body')
        .should((body) => {
          expect(body.reportingPeriods).to.include(2023);
          EU_TAXONOMY_FRAMEWORKS.forEach((type) => expect(body.dataTypes).to.include(type));
          expect(body.dataTypes).not.to.include('sfdr');
        });

      cy.get('[data-test="portfolios"] [data-pc-name="tabpanel"]').contains(portfolioName).click({ force: true });
      cy.get(`[data-test="portfolio-${portfolioName}"] [data-test="edit-portfolio"]`).click();
      cy.get('[name="portfolioName"]').clear();
      cy.get('[name="portfolioName"]').type(editedPortfolioName);
      cy.get('[name="company-identifiers"]').type(permIdOfSecondCompany);
      cy.get('[data-test="addCompanies"]').click();
      cy.get('[data-test="saveButton"]').click();
      cy.wait('@postBulkRequest')
        .its('request.body')
        .should((body) => {
          expect(body.reportingPeriods).to.include('2023');
          EU_TAXONOMY_FRAMEWORKS.forEach((type) => expect(body.dataTypes).to.include(type));
          expect(body.dataTypes).not.to.include('sfdr');
        });
      cy.get('[data-test="portfolios"] [data-pc-name="tabpanel"]').contains(editedPortfolioName).click({ force: true });
      cy.get(`[data-test="portfolio-${editedPortfolioName}"] [data-test="edit-portfolio"]`).click({ force: true });

      cy.window().then((win) => {
        cy.stub(win, 'confirm').returns(true);
      });
      cy.get('[data-test="deleteButton"]').click();
      cy.get('[data-test="portfolios"] [data-pc-name="tabpanel"]').contains(editedPortfolioName).should('not.exist');
    });
  }
);
