import { getStringCypressEnv } from '@e2e/utils/Cypress';

describe('As a developer, I expect the PGAdmin console to be available to me', () => {
  it('Checks if the PGAdmin console is available and the login page is shown', () => {
    cy.visitAndCheckExternalAdminPage({
      url: 'http://dataland-admin:6789/pgadmin',
      interceptPattern: '**/pgadmin/**',
      elementSelector: 'input[name=email]',
      urlShouldInclude: '/pgadmin',
    });

    cy.get('input[name=email]').should('exist').type('admin@dataland.com');
    getStringCypressEnv('PGADMIN_PASSWORD').then((password) => {
      cy.get('#password').should('exist').type(password);
    });
    cy.get('button[name=internal_button]').should('contain.text', 'Login').click();

    cy.waitForPageLoad({
      urlShouldInclude: '/pgadmin/browser',
      elementSelectors: ['span[class=file-name]:contains("BackendDb")', '.aciTreeText:contains("BackendDb")'],
      errorMsg: 'PGAdmin interface did not load within the timeout period',
    });

    cy.log('✅ PGAdmin successfully loaded');
  });
});
