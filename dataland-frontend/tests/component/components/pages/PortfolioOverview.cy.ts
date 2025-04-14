import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import PortfolioOverview from '@/components/pages/PortfolioOverview.vue';
import { type BasePortfolio, type BasePortfolioName } from '@clients/userservice';

describe('Check the portfolio overview view', function (): void {
  let basePortfolioNames: BasePortfolioName[];

  before(function () {
    cy.fixture('enrichedPortfolio.json').then(function (jsonContent) {
      const portfolioFixture = jsonContent as BasePortfolio;
      basePortfolioNames = [
        {
          portfolioId: portfolioFixture.portfolioId,
          portfolioName: portfolioFixture.portfolioName,
        },
      ];
    });
  });

  it('Should display portfolio information correctly', function (): void {
    cy.intercept('**/users/portfolios/names', basePortfolioNames).as('basePortfolios');

    // @ts-ignore
    cy.mountWithPlugins(PortfolioOverview, {
      keycloak: minimalKeycloakMock({}),
    }).then(() => {
      cy.wait('@basePortfolios').then(() => {
        cy.get('[data-test="portfolios"] ul').should('contain', 'New Portfolio');
        cy.get('[data-test="portfolios"] ul li.p-highlight').should('contain', basePortfolioNames[0].portfolioName);
      });
    });
  });

  it('Should handle empty portfolio list', function (): void {
    cy.intercept('**/users/portfolios/names', []).as('basePortfolios');

    // @ts-ignore
    cy.mountWithPlugins(PortfolioOverview, {
      keycloak: minimalKeycloakMock({}),
    }).then(() => {
      cy.wait('@basePortfolios').then(() => {
        cy.get('[data-test="portfolios"] ul').should('have.length', 1);
        cy.get('[data-test="portfolios"] ul li.p-highlight').should('contain', 'New Portfolio');
      });
    });
  });

  it('Should handle API error gracefully', function (): void {
    cy.intercept('**/users/portfolios/names', {
      statusCode: 500,
      body: { message: 'Internal server error' },
    }).as('basePortfolios');

    // @ts-ignore
    cy.mountWithPlugins(PortfolioOverview, {
      keycloak: minimalKeycloakMock({}),
    }).then(() => {
      cy.wait('@basePortfolios').then(() => {
        cy.get('[data-test="portfolios"] ul').should('have.length', 1);
        cy.get('[data-test="portfolios"] ul li.p-highlight').should('contain', 'New Portfolio');
      });
    });
  });
});
