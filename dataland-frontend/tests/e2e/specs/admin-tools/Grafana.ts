import { getStringCypressEnv } from '@e2e/utils/Cypress';

describe('As a developer, I expect Grafana to be available to me', () => {
  it('Checks if Grafana is available loki logs are shown', () => {
    // Step 1: Log in to Grafana
    loginToGrafana();

    // Step 2: Navigate to the Logs in the Explore section
    cy.get('button[aria-label="Expand section Explore"]').should('exist').click();
    cy.get('[data-testid="data-testid Nav menu item"]').contains('Logs').click();

    // Step 3: Check that there are some logs displayed
    cy.get('[data-testid="header-container"]').should('contain.text', 'dataland-');
    cy.get('[data-testid="data-testid panel content"]').should('exist');
  });

  it('Checks the health status of the preconfigured alerts', () => {
    // Step 1: Log in to Grafana
    loginToGrafana();

    // Step 2: Navigate to the Alert rules in the Alerting section
    cy.get('a[href="/grafana/alerting"]').click();
    cy.url().should('include', '/grafana/alerting');
    cy.get('a[href="/grafana/alerting/list"]').click();
    cy.url().should('include', '/grafana/alerting/list');

    // Step 4: Validate the health status of the alerts
    cy.get('[data-testid="rule-group"]').should('contain.text', 'Error Alerts');
    cy.get('[data-testid="data-testid group-collapse-toggle"]').should('exist').click();
    cy.get('[data-testid="rules-table"]').find('[data-testid="row"]').should('have.length', 4);
    cy.get('[data-testid="rules-table"] [data-testid="row"]').each(($row) => {
      cy.wrap($row).find('[data-column="Health"]').should('contain.text', 'ok');
    });
  });

  it('Checks the preconfigured Slack contact points', () => {
    // Step 1: Log in to Grafana
    loginToGrafana();

    // Step 2: Navigate to the Contact Pomits in the Alerting section
    cy.get('a[href="/grafana/alerting"]').click();
    cy.url().should('include', '/grafana/alerting');
    cy.get('a[href="/grafana/alerting/notifications"]').click();
    cy.url().should('include', '/grafana/alerting/notifications');

    // Step 3: Validate the Slack contact points configuration with sending test notifications
    cy.get('[data-testid="contact-point"]').should('have.length', 3);
    testProvisionedContactPoint('Slack Alert Bot');
    testProvisionedContactPoint('Slack Critical Alerts');
  });
});

/**
 * Function to log in to Grafana.
 */
function loginToGrafana(): void {
  cy.visit('http://dataland-admin:6789/grafana');
  cy.url().should('include', '/grafana/login');
  cy.get('input[name=user]').should('exist').type(getStringCypressEnv('GRAFANA_ADMIN'));
  cy.get('input[name=password]').should('exist').type(getStringCypressEnv('GRAFANA_PASSWORD'));
  cy.get('button[data-testid="data-testid Login button"]').should('contain.text', 'Log in').click();
  cy.get('[data-testid="data-testid panel content"]').should('contain.text', 'Grafana');
}

/**
 * Function to test the provisioned contact points.
 * @param contactPointName - The name of the contact point to test.
 */
function testProvisionedContactPoint(contactPointName: string): void {
  // View details of contactPointName
  cy.get('[data-testid="contact-point"]')
    .contains(contactPointName)
    .parents('[data-testid="contact-point"]')
    .within(() => {
      cy.contains('Provisioned').should('exist');
      cy.get('[data-testid="view-action"]').click();
    });
  cy.contains('This contact point cannot be edited through the UI').should('exist');
  // Send test notification
  cy.contains('button', 'Test').should('exist').click();
  cy.contains('button', 'Send test notification').should('exist').click();
  cy.contains('Test alert sent').should('exist');
  // go back to contact points overview
  cy.get('button[aria-label="Close"]').should('exist').click();
  cy.get('a[href="/grafana/alerting/notifications"]').click();
  cy.url().should('include', '/grafana/alerting/notifications');
}
