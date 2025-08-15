import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import PortfolioDialog from '@/components/resources/portfolio/PortfolioDialog.vue';
import { type EnrichedPortfolio } from '@clients/userservice';

describe('Check the portfolio dialog', function (): void {
  let portfolioFixture: EnrichedPortfolio;

  before(function () {
    cy.fixture('enrichedPortfolio.json').then(function (jsonContent) {
      portfolioFixture = jsonContent as EnrichedPortfolio;
    });
  });

  it('Should display empty dialog for new portfolio', function (): void {
    // @ts-ignore
    cy.mountWithPlugins(PortfolioDialog, {
      keycloak: minimalKeycloakMock({}),
    }).then(() => {
      cy.get('[data-test="portfolio-name-input"]').should('have.value', '');
      cy.get('[data-test="company-identifiers-input"]').should('have.value', '');
      cy.get('[data-test="portfolio-dialog-save-button"]').should('contain.text', 'SAVE PORTFOLIO').and('be.disabled');
    });
  });

  it('Should display existing portfolio data when editing', function (): void {
    // @ts-ignore
    cy.mountWithPlugins(PortfolioDialog, {
      keycloak: minimalKeycloakMock({}),
      global: {
        provide: {
          dialogRef: {
            value: {
              data: {
                portfolio: portfolioFixture,
              },
            },
          },
        },
      },
    }).then(() => {
      cy.get('[data-test="portfolio-name-input"]').should('have.value', portfolioFixture.portfolioName);
      portfolioFixture.entries.forEach((entry) => {
        cy.get('[id="existing-company-identifiers"]').should('contain', entry.companyName);
      });
      cy.get('[data-test="portfolio-dialog-save-button"]').should('contain.text', 'SAVE PORTFOLIO').and('not.be.disabled');
    });
  });

  it('Should be possible to edit the portfolio', function (): void {
    const validIdentifier = portfolioFixture.entries[0].companyName;
    const invalidIdentifier = 'INVALID-ID';

    cy.intercept('POST', '**/companies/validation', {
      statusCode: 200,
      body: [
        {
          identifier: validIdentifier,
          companyInformation: portfolioFixture.entries[0],
        },
        {
          identifier: invalidIdentifier,
          companyInformation: null,
        },
      ],
    }).as('validateCompanies');

    // @ts-ignore
    cy.mountWithPlugins(PortfolioDialog, {
      keycloak: minimalKeycloakMock({}),
    }).then(() => {
      cy.get('[data-test="portfolio-dialog-save-button"]').should('be.disabled');

      cy.get('[data-test="portfolio-name-input"]').type('Test Portfolio');
      cy.get('[data-test="portfolio-dialog-save-button"]').should('be.disabled');

      cy.get('[data-test="company-identifiers-input"]').type(`${validIdentifier}, ${invalidIdentifier}`);
      cy.get('[data-test="portfolio-dialog-add-companies"]').click();
      cy.wait('@validateCompanies');
      cy.get('[id="existing-company-identifiers"]').should('contain', portfolioFixture.entries[0].companyName);
      cy.get('[data-test="company-identifiers-input"]').should('have.value', invalidIdentifier);
      cy.get('[data-test="portfolio-dialog-save-button"]').should('not.be.disabled');

      cy.get('[id="existing-company-identifiers"] .pi-trash').click();
      cy.get('[data-test="portfolio-dialog-save-button"]').should('be.disabled');
    });
  });

  it('Should handle API errors gracefully', function (): void {
    cy.intercept('POST', '**/companies/validation', {
      statusCode: 500,
      body: { message: 'Internal server error' },
    }).as('validateCompanies');

    // @ts-ignore
    cy.mountWithPlugins(PortfolioDialog, {
      keycloak: minimalKeycloakMock({}),
    }).then(() => {
      cy.get('[data-test="company-identifiers-input"]').type(portfolioFixture.entries[0].companyName);
      cy.get('[data-test="portfolio-dialog-add-companies"]').click();
      cy.wait('@validateCompanies').then(() => {
        // Check if error message is displayed
        cy.get('[data-test="unknown-portfolio-error"]').should('be.visible').and('contain.text', 'fail');
      });
    });
  });

  it('Should delete the portfolio', function (): void {
    cy.intercept('DELETE', '**/portfolios/**', (req) => {
      req.reply({
        statusCode: 200,
        body: { message: 'Portfolio deleted successfully' },
      });
    }).as('deletePortfolio');

    // @ts-ignore
    cy.mountWithPlugins(PortfolioDialog, {
      keycloak: minimalKeycloakMock({}),
      global: {
        provide: {
          dialogRef: {
            value: {
              data: {
                portfolio: portfolioFixture,
              },
              close: cy.stub(),
            },
          },
        },
      },
    }).then(() => {
      cy.get('[data-test="portfolio-dialog-delete-button"]').should('exist');
      cy.get('[data-test="portfolio-dialog-delete-button"]').click();
      cy.wait('@deletePortfolio').its('response.statusCode').should('eq', 200);
      cy.intercept('DELETE', '**/portfolios/**', (req) => {
        req.reply({
          statusCode: 200,
          body: { message: 'Portfolio deleted successfully' },
        });
      });
    });
  });
});
