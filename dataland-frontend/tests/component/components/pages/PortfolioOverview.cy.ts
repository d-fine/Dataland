import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import PortfolioOverview from '@/components/pages/PortfolioOverview.vue';
import { type BasePortfolio, type BasePortfolioName, type EnrichedPortfolio } from '@clients/userservice';

describe('Check the portfolio overview view', () => {
  let basePortfolioNames: BasePortfolioName[];
  let enrichedPortfolioResponse: EnrichedPortfolio;
  const inheritedRolesResponse = {};

  before(() => {
    cy.fixture('enrichedPortfolio.json').then((jsonContent) => {
      const portfolioFixture = jsonContent as BasePortfolio;
      enrichedPortfolioResponse = jsonContent as EnrichedPortfolio;
      basePortfolioNames = [
        {
          portfolioId: portfolioFixture.portfolioId,
          portfolioName: portfolioFixture.portfolioName,
        },
      ];
    });
  });

  beforeEach(() => {
    cy.intercept('GET', '**/users/portfolios/names', basePortfolioNames).as('basePortfolios');
    cy.intercept('GET', '**/users/portfolios/*/enriched-portfolio', enrichedPortfolioResponse).as('enrichedPortfolio');
    cy.intercept('GET', '**/community/inherited-roles/**', inheritedRolesResponse).as('inheritedRoles');
  });

  it('Should display portfolio information correctly', () => {
    // @ts-ignore
    cy.mountWithPlugins(PortfolioOverview, {
      keycloak: minimalKeycloakMock({}),
    });

    cy.wait('@basePortfolios');

    expect(basePortfolioNames).to.have.length.greaterThan(0);
    cy.get('[data-test="add-portfolio"]').should('contain', 'ADD NEW PORTFOLIO');
    cy.get('[data-test="portfolios"] [data-p-active="true"]').should('contain', basePortfolioNames[0]!.portfolioName);
  });

  it('Should handle empty portfolio list', () => {
    cy.intercept('GET', '**/users/portfolios/names', []).as('basePortfolios');

    // @ts-ignore
    cy.mountWithPlugins(PortfolioOverview, {
      keycloak: minimalKeycloakMock({}),
    });

    cy.wait('@basePortfolios');

    cy.get('[data-pc-name="tablist-tab-list"]').should('have.length', 0);
    cy.get('[data-test="add-portfolio"]').should('contain', 'ADD NEW PORTFOLIO');
  });

  it('Should handle API error gracefully', () => {
    cy.intercept('GET', '**/users/portfolios/names', {
      statusCode: 500,
      body: { message: 'Internal server error' },
    }).as('basePortfolios');

    cy.intercept('GET', '**/users/portfolios/*/enriched-portfolio', { statusCode: 500, body: {} }).as(
      'enrichedPortfolio'
    );
    cy.intercept('GET', '**/community/inherited-roles/**', { statusCode: 500, body: {} }).as('inheritedRoles');

    // @ts-ignore
    cy.mountWithPlugins(PortfolioOverview, {
      keycloak: minimalKeycloakMock({}),
    });

    cy.wait('@basePortfolios');

    cy.get('[data-test="portfolios"] ul').should('have.length', 0);
  });
});
