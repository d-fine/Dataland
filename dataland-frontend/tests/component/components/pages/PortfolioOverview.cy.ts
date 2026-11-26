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
    sessionStorage.clear();
  });

  /**
   * Mounts the portfolio overview including a DynamicDialog so that portfolio dialogs can be opened.
   */
  function mountPortfolioOverview(): void {
    cy.mountWithDialog(PortfolioOverview, { keycloak: minimalKeycloakMock({}) }, {});
  }

  it('Should display portfolio information correctly', () => {
    cy.intercept('GET', '**/users/portfolios/names', basePortfolioNames).as('basePortfolios');
    cy.intercept('GET', '**/users/portfolios/*/enriched-portfolio', enrichedPortfolioResponse).as('enrichedPortfolio');
    cy.intercept('GET', '**/community/inherited-roles/**', inheritedRolesResponse).as('inheritedRoles');

    mountPortfolioOverview();

    cy.wait(['@basePortfolios', '@enrichedPortfolio', '@inheritedRoles']);

    expect(basePortfolioNames).to.have.length.greaterThan(0);
    cy.get('[data-test="add-portfolio"]').should('contain', 'ADD NEW PORTFOLIO');
    cy.get('[data-test="portfolios"] [data-p-active="true"]').should('contain', basePortfolioNames[0].portfolioName);
  });

  it('Should show a loading indicator until the portfolios are loaded', () => {
    cy.intercept('GET', '**/users/portfolios/names', { body: [], delay: 500 }).as('basePortfolios');

    mountPortfolioOverview();

    cy.get('[data-test="portfolios-loading"]').should('be.visible');
    cy.contains('No Portfolios available.').should('not.exist');
    cy.wait('@basePortfolios');
    cy.get('[data-test="portfolios-loading"]').should('not.exist');
    cy.contains('No Portfolios available.').should('be.visible');
  });

  it('Should handle empty portfolio list', () => {
    cy.intercept('GET', '**/users/portfolios/names', []).as('basePortfolios');

    mountPortfolioOverview();

    cy.wait('@basePortfolios');

    cy.get('[data-pc-name="tablist-tab-list"]').should('have.length', 0);
    cy.get('[data-test="add-portfolio"]').should('contain', 'ADD NEW PORTFOLIO');
    cy.contains('No Portfolios available.').should('be.visible');
  });

  it('Should show an error message if the portfolios cannot be loaded', () => {
    cy.intercept('GET', '**/users/portfolios/names', {
      statusCode: 500,
      body: { message: 'Internal server error' },
    }).as('basePortfolios');

    mountPortfolioOverview();

    cy.wait('@basePortfolios');

    cy.get('[data-test="portfolios"] ul').should('have.length', 0);
    cy.get('[data-test="portfolios-error"]').should('be.visible').and('contain.text', 'Error loading portfolios');
    cy.contains('No Portfolios available.').should('not.exist');
  });

  it('Should refresh the tabs and select a newly created portfolio', () => {
    const newPortfolio: BasePortfolioName = { portfolioId: 'new-portfolio-id', portfolioName: 'New Portfolio' };
    const companyEntry = enrichedPortfolioResponse.entries[0];
    cy.intercept('GET', '**/users/portfolios/names', basePortfolioNames).as('basePortfolios');
    cy.intercept('GET', '**/users/portfolios/*/enriched-portfolio', enrichedPortfolioResponse).as('enrichedPortfolio');
    cy.intercept('GET', '**/community/inherited-roles/**', inheritedRolesResponse);
    cy.intercept('POST', '**/companies/validation', [
      { identifier: companyEntry.companyId, companyInformation: companyEntry },
    ]).as('validateCompanies');

    mountPortfolioOverview();
    cy.wait('@basePortfolios');

    cy.get('[data-test="add-portfolio"]').click();
    cy.get('[data-test="portfolio-name-input"]').type(newPortfolio.portfolioName);
    cy.get('[data-test="company-identifiers-input"]').type(companyEntry.companyId);
    cy.get('[data-test="portfolio-dialog-add-companies"]').click();
    cy.wait('@validateCompanies');

    cy.intercept('POST', '**/users/portfolios/', { ...enrichedPortfolioResponse, ...newPortfolio }).as(
      'createPortfolio'
    );
    cy.intercept('GET', '**/users/portfolios/names', [...basePortfolioNames, newPortfolio]).as('refreshedPortfolios');
    cy.get('[data-test="portfolio-dialog-save-button"]').click();

    cy.wait('@createPortfolio');
    cy.wait('@refreshedPortfolios');
    cy.get('[data-test="portfolio-name-input"]').should('not.exist');
    cy.get('[data-test="portfolios"] [data-p-active="true"]').should('contain', newPortfolio.portfolioName);
  });
});
