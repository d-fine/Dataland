import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import PortfolioMonitoringContent from '@/components/resources/portfolio/PortfolioMonitoring.vue';
import { type EnrichedPortfolio } from '@clients/userservice';

describe('Portfolio Monitoring Modal', function () {
  let portfolioFixture: EnrichedPortfolio;

  before(function () {
    cy.fixture('enrichedPortfolio.json').then(function (jsonContent) {
      portfolioFixture = jsonContent as EnrichedPortfolio;
    });
  });

  beforeEach(function () {
    // @ts-ignore
    cy.mountWithPlugins(PortfolioMonitoringContent, {
      keycloak: minimalKeycloakMock({}),
      global: {
        provide: {
          dialogRef: {
            value: {
              data: {
                portfolio: portfolioFixture,
              },
              close: cy.stub().as('dialogRefClose'),
            },
          },
          getKeycloakPromise: () => Promise.resolve(minimalKeycloakMock({})),
        },
      },
    });
  });

  it('shows validation errors when framework is missing', function () {
    cy.get('[data-test="activateMonitoringToggle"]').click();

    cy.get('[data-test="saveChangesButton"]').click();

    cy.get('[data-test="frameworkError"]')
      .should('contain', 'Please select at least one Framework.')
      .should('be.visible');
  });

  it('submits successfully if framework is selected', function () {
    cy.intercept('PATCH', '**/portfolios/**/monitoring').as('activateMonitoring');
    cy.get('[data-test="activateMonitoringToggle"]').click();

    cy.get('[data-test="frameworkSelection"]')
      .first()
      .within(() => {
        cy.get('input[type="checkbox"]').check();
      });

    cy.get('[data-test="saveChangesButton"]').click();
    cy.get('@activateMonitoring.all').should('have.length', 1);
  });

  it('displays EU Taxonomy message when that framework is selected', function () {
    cy.get('[data-test="activateMonitoringToggle"]').click();
    cy.contains('[data-test="frameworkSelection"]', 'EU Taxonomy').find('input[type="checkbox"]').click();

    cy.get('[data-test="frameworkSelectionText"]').should(
      'contain.text',
      'Select frameworks: SFDR and EU Taxonomy (Financials, Non-Financials, Nuclear & Gas).'
    );
  });

  it('toggle all frameworks on and off', function () {
    cy.get('[data-test="activateMonitoringToggle"]').click();
    cy.get('[data-test="frameworkSelection"]').each(($row) => {
      cy.wrap($row).within(() => {
        cy.get('input[type="checkbox"]').check().should('be.checked');
        cy.get('input[type="checkbox"]').uncheck().should('not.be.checked');
      });
    });
  });

  it('toggles time window threshold on and off', function () {
    cy.get('[data-test="activateMonitoringToggle"]').click();

    cy.get('[data-test="timeWindowThresholdToggle"]').click();
    cy.get('[data-test="timeWindowThresholdToggle"]').should('have.class', 'p-toggleswitch-checked');

    cy.get('[data-test="timeWindowThresholdToggle"]').click();
    cy.get('[data-test="timeWindowThresholdToggle"]').should('not.have.class', 'p-toggleswitch-checked');
  });

  it('sends correct time window threshold value in PATCH request', function () {
    cy.intercept('PATCH', '**/portfolios/**/monitoring').as('patchMonitoring');

    cy.get('[data-test="activateMonitoringToggle"]').click();
    cy.get('[data-test="frameworkSelection"]').first().find('input[type="checkbox"]').check();
    cy.get('[data-test="timeWindowThresholdToggle"]').click();

    cy.get('[data-test="saveChangesButton"]').click();

    cy.wait('@patchMonitoring').its('request.body.timeWindowThreshold').should('equal', 'Extended');
  });

  it('sends undefined threshold when deactivating monitoring', function () {
    cy.intercept('PATCH', '**/portfolios/**/monitoring').as('patchMonitoring');

    cy.get('[data-test="activateMonitoringToggle"]').click();
    cy.get('[data-test="activateMonitoringToggle"]').click();
    cy.get('[data-test="saveChangesButton"]').click();

    cy.wait('@patchMonitoring').its('request.body.timeWindowThreshold').should('be.undefined');
  });
});
