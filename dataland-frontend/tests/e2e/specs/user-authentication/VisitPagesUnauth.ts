import { DataTypeEnum } from '@clients/backend';
import { getBaseUrl } from '@e2e/utils/Cypress';
import { loginWithCredentials, logout } from '@e2e/utils/Auth';

describe('As a user I expect to be redirected to the login page if I am unauthenticated', () => {
  const pages = [
    '/companies',
    `/companies/:companyID/frameworks/${DataTypeEnum.EutaxonomyFinancials}`,
    `/companies/:companyID/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}`,
    `/companies/:companyID/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}/upload`,
    `/companies/:companyID/frameworks/${DataTypeEnum.EutaxonomyFinancials}/upload`,
  ];

  pages.forEach((page) => {
    it(`Test Login Redirect for ${page}`, () => {
      cy.visit(page);
      cy.get('input[name=login]').should('exist').url().should('contain', 'keycloak');
    });
  });
});

describe('As an unauthenticated user I expect to be redirected to the page I started the login process on', () => {
  const pages = ['/companies/:companyID'];

  pages.forEach((page) => {
    it(`Test Login Redirect to ${page}`, () => {
      cy.visitAndCheckAppMount(page);
      cy.get('button.login-button[name="login_dataland_button"]').should('exist').click();

      loginWithCredentials();

      cy.url().should('eq', getBaseUrl() + page);
      logout();
    });

    it(`Test Register Redirect to ${page}`, () => {
      cy.visitAndCheckAppMount(page);
      cy.get('button.registration-button[name="signup_dataland_button"]').should('exist').click();

      cy.contains('button', 'LOGIN TO ACCOUNT').should('exist').should('be.visible').click();

      loginWithCredentials();

      cy.url().should('eq', getBaseUrl() + page);
      logout();
    });
  });
});
