import { getStringCypressEnv } from '@e2e/utils/Cypress';
import { describeIf } from '@e2e/support/TestUtility';
import type { ExecutionConfig } from '@e2e/support/TestUtility';

// Only works locally if Grafan is set up correctly
const executionConfig: ExecutionConfig = {
  executionEnvironments: ['developmentLocal', 'developmentCd'],
};

describeIf('As a developer, I expect Grafana to be available to me', executionConfig, () => {
  beforeEach(() => {
    cy.setExceptionContext('grafana');

    cy.visitAndCheckExternalAdminPage({
      url: 'http://dataland-admin:6789/grafana',
      elementSelector: '[data-testid="data-testid Login button"]',
      urlShouldInclude: '/grafana',
    });

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
    cy.get('button[aria-label="Expand section Drilldown"]').should('exist').click();
    cy.get('[data-testid="data-testid Nav menu item"]').contains('Logs').click();
    cy.url().should('include', '/grafana-lokiexplore');
    cy.get('[data-testid="header-container"]').should('contain.text', 'dataland-');
    cy.get('[data-testid="data-testid panel content"]').should('exist');
  });

  it('Checks the health status of the preconfigured alerts', () => {
    cy.get('a[href="/grafana/alerting"]').click();
    cy.url().should('include', '/grafana/alerting');
    cy.get('a[href="/grafana/alerting/list"]').click();
    cy.url().should('include', '/grafana/alerting/list');
    cy.get('[data-testid="rule-group"]').should('contain.text', 'Error Alerts');
    cy.get('[data-testid="data-testid group-collapse-toggle"]').should('exist').click();
    cy.get('[data-testid="rules-table"]').find('[data-testid="row"]').should('have.length', 4);
    cy.get('[data-testid="rules-table"] [data-testid="row"]').each(($row) => {
      cy.wrap($row).find('[data-column="Health"]').should('contain.text', 'ok');
    });
  });

  it('Checks the preconfigured Slack contact points', () => {
    cy.get('a[href="/grafana/alerting"]').click();
    cy.url().should('include', '/grafana/alerting');
    cy.get('a[href="/grafana/alerting/notifications"]').click();
    cy.url().should('include', '/grafana/alerting/notifications');
    cy.get('[data-testid="contact-point"]').should('have.length', 3);
    testProvisionedContactPoint('Slack Alert Bot');
    testProvisionedContactPoint('Slack Critical Alerts');
  });
});

/**
 * Function to test the provisioned contact points.
 * @param contactPointName - The name of the contact point to test.
 */
function testProvisionedContactPoint(contactPointName: string): void {
  cy.get('[data-testid="contact-point"]')
    .contains(contactPointName)
    .parents('[data-testid="contact-point"]')
    .within(() => {
      cy.contains('Provisioned').should('exist');
      cy.get('[data-testid="view-action"]').click();
    });
  cy.contains('This contact point cannot be edited through the UI').should('exist');
  cy.get('a[href="/grafana/alerting/notifications"]').click();
  cy.url().should('include', '/grafana/alerting/notifications');
}
