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

  // Your existing tests below

  it('renders dropdown and frameworks correctly', function () {
    cy.get('[data-test="listOfReportingPeriods"]').should('exist');
    cy.get('.framework-switch-row').should('have.length.greaterThan', 0);
  });

  it('shows validation errors when both inputs are missing', function () {
    cy.get('[data-test="saveChangesButton"]').click();

    cy.get('[data-test="reportingPeriodsError"]')
      .should('contain', 'Please select Starting Period.')
      .should('be.visible');

    cy.get('[data-test="frameworkError"]')
      .should('contain', 'Please select at least one Framework.')
      .should('be.visible');
  });

  it('shows only framework error if reporting year is selected', function () {
    cy.get('[data-test="listOfReportingPeriods"]').click();
    cy.contains('2024').click();

    cy.get('[data-test="saveChangesButton"]').click();

    cy.get('[data-test="frameworkError"]')
      .should('contain', 'Please select at least one Framework.')
      .should('be.visible');
  });

  it('shows only reporting year error if framework selected', function () {
    cy.get('.framework-switch-row')
      .first()
      .within(() => {
        cy.get('input[type="checkbox"]').check({ force: true }).should('be.checked');
      });

    cy.get('[data-test="saveChangesButton"]').click();

    cy.get('[data-test="reportingPeriodsError"]')
      .should('contain', 'Please select Starting Period.')
      .should('be.visible');
  });

  it('submits successfully when both year and framework are selected', function () {
    cy.get('[data-test="listOfReportingPeriods"]').click();
    cy.contains('2023').click();

    cy.get('.framework-switch-row')
      .first()
      .within(() => {
        cy.get('input[type="checkbox"]').check({ force: true });
      });

    cy.get('[data-test="saveChangesButton"]').click();
  });

  it('displays EU Taxonomy message when that framework is selected', function () {
    cy.get('.framework-switch-row')
      .contains('EU Taxonomy')
      .parents('.framework-switch-row')
      .within(() => {
        cy.get('input[type="checkbox"]').check({ force: true });
      });

    cy.get('.gray-text').should(
      'contain.text',
      'EU Taxonomy creates requests for EU Taxonomy Financials, Non-Financials and Nuclear and Gas'
    );
  });

  it('toggle all frameworks on and off', function () {
    cy.get('.framework-switch-row').each(($row) => {
      cy.wrap($row).within(() => {
        cy.get('input[type="checkbox"]').check({ force: true }).should('be.checked');
        cy.get('input[type="checkbox"]').uncheck({ force: true }).should('not.be.checked');
      });
    });
  });

  it('dropdown lists all years in order', function () {
    cy.get('[data-test="listOfReportingPeriods"]').click();
    ['2024', '2023', '2022', '2021', '2020', '2019'].forEach((year) => {
      cy.contains(year).should('exist');
    });
  });
  it('updates selectedStartingYear when dropdown changes', () => {
    cy.get('[data-test="listOfReportingPeriods"]').click();
    cy.contains('2022').click();

    // Kontrollieren, ob Dropdown den Wert annimmt (prüfen anhand sichtbarem Text)
    cy.get('[data-test="listOfReportingPeriods"]').should('contain.text', '2022');
  });

  it('toggles framework switches correctly', () => {
    cy.get('.framework-switch-row').each(($row) => {
      cy.wrap($row).within(() => {
        cy.get('input[type="checkbox"]').check({ force: true }).should('be.checked');
        cy.get('input[type="checkbox"]').uncheck({ force: true }).should('not.be.checked');
      });
    });
  });
});
