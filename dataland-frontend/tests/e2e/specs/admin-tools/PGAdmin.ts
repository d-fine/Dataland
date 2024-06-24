import { getStringCypressEnv } from '@e2e/utils/Cypress';

describe('As a developer, I expect the PGAdmin console to be available to me', () => {
  it('Checks if the PGAdmin console is available and the login page is shown', () => {
    cy.visit('http://dataland-admin:6789/pgadmin');
    cy.get('input[name=email]').should('exist').type('admin@dataland.com');
    cy.get('input[name=password]').should('exist').type(getStringCypressEnv('PGADMIN_PASSWORD'));
    cy.get('button[name=internal_button]').should('contain.text', 'Login').click();
    cy.get('span[class=file-name]').should('contain.text', 'BackendDb');
  });
});
