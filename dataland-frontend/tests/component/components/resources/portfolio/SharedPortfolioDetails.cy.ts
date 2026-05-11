import SharedPortfolioDetails from '@/components/resources/portfolio/SharedPortfolioDetails.vue';
import {
  type EnrichedPortfolio,
  type PortfolioUserAccessRight,
  PortfolioUserAccessRightPortfolioAccessRoleEnum,
} from '@clients/userservice';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';

const userId = '9bba9b59-c1ab-48f2-be92-196c5ea83d5f';
const adminId = '136a9394-4873-4a61-a25b-65b1e8e7cc2f';
const adminEmail = 'data.admin@example.com';

interface ConfigurationParameters {
  keycloakRoles: string[];
  portfolioResponse: EnrichedPortfolio;
}

let adminConfigurationParametersWithoutMonitoring: ConfigurationParameters;
let adminConfigurationParametersWithMonitoring: ConfigurationParameters;

/**
 * Intercepts the API calls for inherited roles and portfolio download, mounts the SharedPortfolioDetails component,
 * and waits for the portfolio download to complete.
 * @param configurationParameters parameters for configuring the test scenario
 * @returns A Cypress.Chainable that resolves when the portfolio download is complete
 */
function interceptApiCallsAndMountAndWaitForDownload(
  configurationParameters: ConfigurationParameters
): Cypress.Chainable {
  cy.intercept('**/users/portfolios/*/enriched-portfolio', configurationParameters.portfolioResponse).as(
    'downloadComplete'
  );
  interceptPortfolioAccessRightsCall();

  return (
    cy
      // @ts-ignore
      .mountWithPlugins(SharedPortfolioDetails, {
        keycloak: minimalKeycloakMock({
          userId: userId,
        }),
        props: { portfolioId: configurationParameters.portfolioResponse.portfolioId },
      })
      .then(() => cy.wait('@downloadComplete'))
  );
}

/**
 * Intercepts the API call for fetching portfolio access rights and provides a mock response.
 */
function interceptPortfolioAccessRightsCall(): void {
  const response: PortfolioUserAccessRight[] = [
    {
      userId: adminId,
      userEmail: adminEmail,
      portfolioAccessRole: PortfolioUserAccessRightPortfolioAccessRoleEnum.Owner,
    },
  ];

  cy.intercept('GET', '**/users/portfolios/*/access-rights', {
    body: response,
    status: 200,
  });
}

describe('Check the portfolio details view', function (): void {
  let portfolioFixtureWithoutMonitoring: EnrichedPortfolio;
  let portfolioFixtureWithMonitoring: EnrichedPortfolio;

  before(function () {
    cy.fixture('enrichedPortfolio.json')
      .then(function (jsonContent) {
        portfolioFixtureWithoutMonitoring = jsonContent as EnrichedPortfolio;
      })
      .then(() => {
        portfolioFixtureWithMonitoring = {
          ...portfolioFixtureWithoutMonitoring,
          isMonitored: true,
          monitoredFrameworks: new Set(['sfdr', 'eutaxonomy']),
        } as EnrichedPortfolio;
        adminConfigurationParametersWithoutMonitoring = {
          keycloakRoles: ['ROLE_ADMIN'],
          portfolioResponse: portfolioFixtureWithoutMonitoring,
        };
        adminConfigurationParametersWithMonitoring = {
          keycloakRoles: ['ROLE_ADMIN'],
          portfolioResponse: portfolioFixtureWithMonitoring,
        };
      });
  });

  it('Check Remove Button does exist', function (): void {
    interceptApiCallsAndMountAndWaitForDownload(adminConfigurationParametersWithMonitoring).then(() => {
      cy.get('[data-test="remove-portfolio"]').should('be.visible').and('contain.text', 'REMOVE PORTFOLIO');
    });
  });

  it('Check Not Monitored Tag for Dataland admin', function (): void {
    interceptApiCallsAndMountAndWaitForDownload(adminConfigurationParametersWithoutMonitoring).then(() => {
      cy.get('[data-test="monitor-portfolio"]').should('not.exist');
      cy.get('[data-test="is-monitored-tag"]')
        .should('be.visible')
        .and('contain.text', 'Portfolio not actively monitored');
    });
  });

  it('Check Monitored Tag for Dataland admin', function (): void {
    interceptApiCallsAndMountAndWaitForDownload(adminConfigurationParametersWithMonitoring).then(() => {
      cy.get('[data-test="monitor-portfolio"]').should('not.exist');
      cy.get('[data-test="is-monitored-tag"]').should('be.visible').and('contain.text', 'Portfolio actively monitored');
    });
  });

  it('Check Shared By Tag for Dataland admin', function (): void {
    interceptApiCallsAndMountAndWaitForDownload(adminConfigurationParametersWithMonitoring).then(() => {
      cy.get('[data-test="shared-by-tag"]')
        .should('be.visible')
        .and('contain.text', 'Shared by')
        .and('contain.text', adminEmail);
    });
  });
});
