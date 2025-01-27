import { getStringCypressEnv } from '@e2e/utils/Cypress';

describe('As a developer, I expect Grafana to be available to me', () => {
  it('Checks if Grafana is available loki logs are shown', () => {
    cy.visit('http://dataland-admin:6789/grafana');
    cy.get('input[name=user]').should('exist').type(getStringCypressEnv('GRAFANA_ADMIN'));
    cy.get('input[name=password]').should('exist').type(getStringCypressEnv('GRAFANA_PASSWORD'));
    cy.get('button[data-testid="data-testid Login button"]').should('contain.text', 'Log in').click();
    cy.get('[data-testid="data-testid panel content"]').should('contain.text', 'Grafana');
    cy.get('button[aria-label="Expand section Explore"]').should('exist').click();
    cy.get('[data-testid="data-testid Nav menu item"]').contains('Logs').click();
    cy.get('[data-testid="header-container"]').should('contain.text', 'dataland-');
    cy.get('[data-testid="data-testid panel content"]').should('exist');
  });
});
