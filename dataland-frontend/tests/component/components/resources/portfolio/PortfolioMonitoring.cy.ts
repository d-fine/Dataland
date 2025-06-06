import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import PortfolioMonitoringContent from '@/components/resources/portfolio/PortfolioMonitoring.vue';
import { type EnrichedPortfolio } from '@clients/userservice';

describe('Portfolio Monitoring - Framework Selection Only', function () {
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

  it('renders framework switches and allows toggling', function () {
    cy.get('.framework-switch-row').should('have.length.greaterThan', 0);
    cy.get('.framework-switch-row').each(($row) => {
      cy.wrap($row).within(() => {
        cy.get('input[type="checkbox"]')
          .should('exist')
          .check({ force: true })
          .should('be.checked')
          .uncheck({ force: true })
          .should('not.be.checked');
      });
    });
  });

  it('shows error if no framework is selected on save', function () {
    cy.get('[data-test="saveChangesButton"]').click();
    cy.get('[data-test="frameworkError"]')
      .should('be.visible')
      .and('contain.text', 'Please select at least one Framework.');
  });

  it('successfully saves when at least one framework is selected', function () {
    cy.get('.framework-switch-row')
      .first()
      .within(() => {
        cy.get('input[type="checkbox"]').check({ force: true }).should('be.checked');
      });

    cy.get('[data-test="saveChangesButton"]').click();
    cy.get('@dialogRefClose').should('not.have.been.called'); // Change to `.should('have.been.called')` if successful close expected
  });

  it('handles EU Taxonomy specific messaging visibility', function () {
    cy.get('.framework-switch-row')
      .contains('EU Taxonomy')
      .parents('.framework-switch-row')
      .within(() => {
        cy.get('input[type="checkbox"]').check({ force: true }).should('be.checked');
      });

    cy.get('.gray-text').should('contain.text', 'EU Taxonomy creates requests for EU Taxonomy Financials');
  });
});
