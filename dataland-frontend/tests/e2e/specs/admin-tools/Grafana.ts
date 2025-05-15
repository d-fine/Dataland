import { getStringCypressEnv } from '@e2e/utils/Cypress';

Cypress._.times(10, () => {
  describe('As a developer, I expect Grafana to be available to me', () => {
    beforeEach(() => {
      cy.setExceptionContext('grafana');

      cy.visitAndCheckExternalAdminPage({
        url: 'http://dataland-admin:6789/grafana',
        elementSelector: '[data-testid="data-testid Login button"]',
        urlShouldInclude: '/grafana',
      });

      // Login before each test
      cy.url().then((url) => {
        if (url.includes('/grafana/login')) {
          cy.get('input[name=user]').should('exist').type(getStringCypressEnv('GRAFANA_ADMIN'));
          cy.get('input[name=password]').should('exist').type(getStringCypressEnv('GRAFANA_PASSWORD'));
          cy.get('button[data-testid="data-testid Login button"]').should('contain.text', 'Log in').click();
        }
        cy.get('[data-testid="data-testid panel content"]').should('exist');
      });
    });

    it('Checks if Grafana is available loki logs are shown', () => {
      // Navigate to the Logs in the Drilldown section
      cy.get('button[aria-label="Expand section Drilldown"]').should('exist');
      cy.get('button[aria-label="Expand section Drilldown"]').should('exist').click();
      cy.get('[data-testid="data-testid Nav menu item"]').contains('Logs').click();
      cy.url().should('include', '/grafana-lokiexplore');

      // Check that there are some logs displayed
      cy.get('[data-testid="header-container"]').should('contain.text', 'dataland-');
      cy.get('[data-testid="data-testid panel content"]').should('exist');
    });

    it('Checks the health status of the preconfigured alerts', () => {
      // Navigate to the Alert rules in the Alerting section
      cy.get('a[href="/grafana/alerting"]').click();
      cy.url().should('include', '/grafana/alerting');
      cy.get('a[href="/grafana/alerting/list"]').click();
      cy.url().should('include', '/grafana/alerting/list');

      // Validate the health status of the alerts
      cy.get('[data-testid="rule-group"]').should('contain.text', 'Error Alerts');
      cy.get('[data-testid="data-testid group-collapse-toggle"]').should('exist').click();
      cy.get('[data-testid="rules-table"]').find('[data-testid="row"]').should('have.length', 4);
      cy.get('[data-testid="rules-table"] [data-testid="row"]').each(($row) => {
        cy.wrap($row).find('[data-column="Health"]').should('contain.text', 'ok');
      });
    });

    it('Checks the preconfigured Slack contact points', () => {
      // Navigate to the Contact Pomits in the Alerting section
      cy.get('a[href="/grafana/alerting"]').click();
      cy.url().should('include', '/grafana/alerting');
      cy.get('a[href="/grafana/alerting/notifications"]').click();
      cy.url().should('include', '/grafana/alerting/notifications');

      // Validate the Slack contact points configuration with sending test notifications
      cy.get('[data-testid="contact-point"]').should('have.length', 3);
      testProvisionedContactPoint('Slack Alert Bot');
      testProvisionedContactPoint('Slack Critical Alerts');
    });
  });
});

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
  // go back to contact points overview
  cy.get('a[href="/grafana/alerting/notifications"]').click();
  cy.url().should('include', '/grafana/alerting/notifications');
}
