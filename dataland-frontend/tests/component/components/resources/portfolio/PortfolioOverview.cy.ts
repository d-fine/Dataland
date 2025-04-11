import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import PortfolioOverview from '@/components/pages/PortfolioOverview.vue';
import { type BasePortfolioName } from '@clients/userservice';

describe('Check the portfolio overview view', function (): void {
  const basePortfolioNames: BasePortfolioName[] = [
    {
      portfolioId: 'test-portfolio-id-1',
      portfolioName: 'Test Portfolio 1',
    },
    {
      portfolioId: 'test-portfolio-id-2',
      portfolioName: 'Test Portfolio 2',
    },
    {
      portfolioId: 'test-portfolio-id-3',
      portfolioName: 'Test Portfolio 3',
    },
  ];

  it('Should display portfolio information correctly', function (): void {
    cy.intercept('**/users/portfolios/names', basePortfolioNames).as('getPortfolioNames');

    // @ts-ignore
    cy.mountWithPlugins(PortfolioOverview, {
      keycloak: minimalKeycloakMock({}),
    }).then(() => {
      cy.wait('@getPortfolioNames').then(() => {
        // Check if all portfolio names are displayed in the tabs
        basePortfolioNames.forEach((portfolio) => {
          cy.get('.p-tabview-nav').should('contain', portfolio.portfolioName);
        });

        // Check if the portfolio details component is rendered
        cy.get('.p-tabview-panels').should('be.visible');
      });
    });
  });

  it('Should handle empty portfolio list', function (): void {
    cy.intercept('**/users/portfolios/names', []).as('getPortfolioNames');

    // @ts-ignore
    cy.mountWithPlugins(PortfolioOverview, {
      keycloak: minimalKeycloakMock({}),
    }).then(() => {
      cy.wait('@getPortfolioNames').then(() => {
        // Check if only the "New Portfolio" tab is visible
        cy.get('.p-tabview-nav').should('have.length', 1);
        cy.get('.p-tabview-nav').should('contain', 'New Portfolio');
      });
    });
  });

  it('Should handle API error gracefully', function (): void {
    cy.intercept('**/users/portfolios/names', {
      statusCode: 500,
      body: { message: 'Internal server error' },
    }).as('getPortfolioNames');

    // @ts-ignore
    cy.mountWithPlugins(PortfolioOverview, {
      keycloak: minimalKeycloakMock({}),
    }).then(() => {
      cy.wait('@getPortfolioNames').then(() => {
        // Check if the error state is handled
        cy.get('.p-tabview-nav').should('have.length', 1);
        cy.get('.p-tabview-nav').should('contain', 'New Portfolio');
      });
    });
  });

  it('Should show new portfolio dialog when clicking new portfolio tab', function (): void {
    cy.intercept('**/users/portfolios/names', basePortfolioNames).as('getPortfolioNames');

    // @ts-ignore
    cy.mountWithPlugins(PortfolioOverview, {
      keycloak: minimalKeycloakMock({}),
    }).then(() => {
      cy.wait('@getPortfolioNames').then(() => {
        // Click on the new portfolio tab
        cy.get('.p-tabview-nav').contains('New Portfolio').click();

        // Check if the new portfolio dialog is shown
        cy.get('h1').should('contain', 'New Portfolio dialog here');
      });
    });
  });

  it('Should maintain current tab when clicking new portfolio tab', function (): void {
    cy.intercept('**/users/portfolios/names', basePortfolioNames).as('getPortfolioNames');

    // @ts-ignore
    cy.mountWithPlugins(PortfolioOverview, {
      keycloak: minimalKeycloakMock({}),
    }).then(() => {
      cy.wait('@getPortfolioNames').then(() => {
        // Store the current tab index
        const initialTabIndex = 0;

        // Click on the new portfolio tab
        cy.get('.p-tabview-nav').contains('New Portfolio').click();

        // Verify we're still on the initial tab
        cy.get('.p-tabview-nav').eq(initialTabIndex).should('have.class', 'p-highlight');
      });
    });
  });
});
