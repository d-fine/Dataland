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
    cy.get('[data-test="activateMonitoringToggle"]').click();

    cy.get('.framework-switch-group')
      .first()
      .within(() => {
        cy.get('input[type="checkbox"]').check();
      });

    cy.get('[data-test="saveChangesButton"]').click();
  });

  it('displays EU Taxonomy message when that framework is selected', function () {
    cy.get('[data-test="activateMonitoringToggle"]').click();
    cy.contains('[data-test="frameworkSelection"]', 'EU Taxonomy').find('input[type="checkbox"]').click();

    cy.get('.dataland-info-text').should(
      'contain.text',
      'EU Taxonomy creates requests for EU Taxonomy Financials, Non-Financials and Nuclear and Gas'
    );
  });

  it('toggle all frameworks on and off', function () {
    cy.get('[data-test="activateMonitoringToggle"]').click();
    cy.get('.framework-switch-group').each(($row) => {
      cy.wrap($row).within(() => {
        cy.get('input[type="checkbox"]').check().should('be.checked');
        cy.get('input[type="checkbox"]').uncheck().should('not.be.checked');
      });
    });
  });
});
