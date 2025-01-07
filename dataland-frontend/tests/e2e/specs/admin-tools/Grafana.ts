import { getStringCypressEnv } from '@e2e/utils/Cypress';

describe('As a developer, I expect Grafana to be available to me', () => {
  it('Checks if Grafana is available and the login page is shown', () => {
    cy.visit('http://dataland-admin:6789/grafana');
    cy.get('input[name=email]').should('exist').type('admin@dataland.com');
    cy.get('input[name=password]').should('exist').type(getStringCypressEnv('GRAFANA_PASSWORD'));
    cy.get('button[name=internal_button]').should('contain.text', 'Log in').click();
    cy.get('span[class=file-name]').should('contain.text', 'Grafana');
  });
});
