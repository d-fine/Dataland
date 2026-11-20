import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import SharedPortfolioOverview from '@/components/pages/SharedPortfolioOverview.vue';
import { type BasePortfolio, type BasePortfolioName, type EnrichedPortfolio } from '@clients/userservice';

describe('Check the portfolio overview view', () => {
  let basePortfolioNames: BasePortfolioName[];
  let enrichedPortfolioResponse: EnrichedPortfolio;

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

  it('Should display portfolio information correctly', () => {
    cy.intercept('GET', '**/users/portfolios/shared/names', basePortfolioNames).as('basePortfolios');
    cy.intercept('GET', '**/users/portfolios/*/enriched-portfolio', enrichedPortfolioResponse).as('enrichedPortfolio');

    // @ts-ignore
    cy.mountWithPlugins(SharedPortfolioOverview, {
      keycloak: minimalKeycloakMock({}),
    });

    cy.wait(['@basePortfolios', '@enrichedPortfolio']);

    expect(basePortfolioNames).to.have.length.greaterThan(0);
    cy.get('[data-test="add-portfolio"]').should('not.exist');
    cy.get('[data-test="portfolios"] [data-p-active="true"]').should('contain', basePortfolioNames[0].portfolioName);
  });

  it('Should handle empty portfolio list', () => {
    cy.intercept('GET', '**/users/portfolios/shared/names', []).as('basePortfolios');

    // @ts-ignore
    cy.mountWithPlugins(SharedPortfolioOverview, {
      keycloak: minimalKeycloakMock({}),
    });

    cy.wait('@basePortfolios');

    cy.get('[data-pc-name="tablist-tab-list"]').should('have.length', 0);
    cy.get('[data-test="add-portfolio"]').should('not.exist');
  });

  it('Should show an error message if the portfolios cannot be loaded', () => {
    cy.intercept('GET', '**/users/portfolios/shared/names', {
      statusCode: 500,
      body: { message: 'Internal server error' },
    }).as('basePortfolios');

    // @ts-ignore
    cy.mountWithPlugins(SharedPortfolioOverview, {
      keycloak: minimalKeycloakMock({}),
    });

    cy.wait('@basePortfolios');

    cy.get('[data-test="portfolios"] ul').should('have.length', 0);
    cy.get('[data-test="portfolios-error"]').should('be.visible');
  });

  it('Should remove the tab of a portfolio after the access to it was removed', () => {
    cy.intercept('GET', '**/users/portfolios/shared/names', basePortfolioNames).as('basePortfolios');
    cy.intercept('GET', '**/users/portfolios/*/enriched-portfolio', enrichedPortfolioResponse);
    cy.intercept('GET', '**/users/portfolios/*/access-rights', []);

    // @ts-ignore
    cy.mountWithPlugins(SharedPortfolioOverview, {
      keycloak: minimalKeycloakMock({}),
    });
    cy.wait('@basePortfolios');
    cy.get('[data-test="portfolios"] [data-p-active="true"]').should('contain', basePortfolioNames[0].portfolioName);

    cy.intercept('DELETE', '**/users/portfolios/shared/*', { statusCode: 204 }).as('removeSharing');
    cy.intercept('GET', '**/users/portfolios/shared/names', []).as('refreshedPortfolios');
    cy.get('[data-test="remove-portfolio"]').click();
    cy.get('[data-test="remove-confirmation-button"]').click();

    cy.wait('@removeSharing');
    cy.wait('@refreshedPortfolios');
    cy.get('[data-pc-name="tablist-tab-list"]').should('have.length', 0);
    cy.contains('No Portfolios available.').should('be.visible');
  });
});
