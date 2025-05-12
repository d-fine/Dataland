import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import PortfolioDownload from '@/components/resources/portfolio/PortfolioDownload.vue';
import { type EnrichedPortfolio } from '@clients/userservice';

describe('Check the Portfolio Download view', function (): void {
  let portfolioFixture: EnrichedPortfolio;

  before(function () {
    cy.fixture('enrichedPortfolio.json').then(function (jsonContent) {
      portfolioFixture = jsonContent as EnrichedPortfolio;
    });
  });

  it('Check framework selection', function (): void {
    cy.intercept('**/users/portfolios/*/enriched-portfolio', portfolioFixture).as('downloadComplete');

    // @ts-ignore Ignore TypeScript error for prop type mismatch temporarily
    cy.mountWithPlugins(PortfolioDownload, {
      keycloak: minimalKeycloakMock({}),
      props: { portfolioId: portfolioFixture.portfolioId }, // Ensure portfolioId is passed correctly
    }).then(() => {
      cy.wait('@downloadComplete').then(() => {
        // Check the initial state for the framework select dropdown
        cy.get('[data-test="frameworkSelector"]').should('exist');
        cy.get('[data-test="frameworkSelector"]').click();
        cy.get('[data-test="frameworkSelector"] option').should('have.length', 4); // Based on the availableFrameworks
        cy.get('[data-test="frameworkSelector"] option[value="sfdr"]').click();
        cy.get('[data-test="frameworkSelector"]').should('have.value', 'sfdr');
      });
    });
  });

  it('Check reporting period selection', function (): void {
    cy.intercept('**/users/portfolios/*/enriched-portfolio', portfolioFixture).as('downloadComplete');

    // @ts-ignore Ignore TypeScript error for prop type mismatch temporarily
    cy.mountWithPlugins(PortfolioDownload, {
      keycloak: minimalKeycloakMock({}),
      props: { portfolioId: portfolioFixture.portfolioId },
    }).then(() => {
      cy.wait('@downloadComplete').then(() => {
        // Check the reporting period toggle buttons
        cy.get('[data-test="listOfReportingPeriods"]').should('exist');
        cy.get('[data-test="listOfReportingPeriods"] .chip').should('have.length', 6); // Based on dynamicReportingPeriods
        cy.get('[data-test="listOfReportingPeriods"] .chip').first().click(); // Toggle the first reporting period
        cy.get('[data-test="listOfReportingPeriods"] .chip').first().should('have.class', 'active'); // Check if the first chip is selected
      });
    });
  });

  it('Check file type selection', function (): void {
    cy.intercept('**/users/portfolios/*/enriched-portfolio', portfolioFixture).as('downloadComplete');

    // @ts-ignore Ignore TypeScript error for prop type mismatch temporarily
    cy.mountWithPlugins(PortfolioDownload, {
      keycloak: minimalKeycloakMock({}),
      props: { portfolioId: portfolioFixture.portfolioId },
    }).then(() => {
      cy.wait('@downloadComplete').then(() => {
        // Check file type dropdown
        cy.get('[data-test="fileTypeSelector"]').should('exist');
        cy.get('[data-test="fileTypeSelector"]').click();
        cy.get('[data-test="fileTypeSelector"] option').should('have.length', 2); // Based on the fileTypeSelectionOptions
        cy.get('[data-test="fileTypeSelector"] option[value="CSV"]').click();
        cy.get('[data-test="fileTypeSelector"]').should('have.value', 'CSV');
      });
    });
  });

  it('Check error message visibility when no framework selected', function (): void {
    cy.intercept('**/users/portfolios/*/enriched-portfolio', portfolioFixture).as('downloadComplete');

    // @ts-ignore Ignore TypeScript error for prop type mismatch temporarily
    cy.mountWithPlugins(PortfolioDownload, {
      keycloak: minimalKeycloakMock({}),
      props: { portfolioId: portfolioFixture.portfolioId },
    }).then(() => {
      cy.wait('@downloadComplete').then(() => {
        // Check the framework error visibility when nothing is selected
        cy.get('[data-test="downloadButton"]').click();
        cy.get('[data-test="frameworkError"]').should('be.visible');
        cy.get('[data-test="frameworkError"]').should('contain', 'Please select Framework.');
      });
    });
  });

  it('Check error message visibility when no reporting period selected', function (): void {
    cy.intercept('**/users/portfolios/*/enriched-portfolio', portfolioFixture).as('downloadComplete');

    // @ts-ignore Ignore TypeScript error for prop type mismatch temporarily
    cy.mountWithPlugins(PortfolioDownload, {
      keycloak: minimalKeycloakMock({}),
      props: { portfolioId: portfolioFixture.portfolioId },
    }).then(() => {
      cy.wait('@downloadComplete').then(() => {
        // Check reporting period error message
        cy.get('[data-test="downloadButton"]').click();
        cy.get('[data-test="listOfReportingPeriods"] .chip').first().click(); // Select a reporting period
        cy.get('[data-test="frameworkSelector"]').select('sfdr'); // Select a framework
        cy.get('[data-test="downloadButton"]').click();
        cy.get('[data-test="reportingPeriodError"]').should('not.exist'); // No error now
      });
    });
  });

  it('Check download button functionality', function (): void {
    cy.intercept('**/users/portfolios/*/enriched-portfolio', portfolioFixture).as('downloadComplete');

    // @ts-ignore Ignore TypeScript error for prop type mismatch temporarily
    cy.mountWithPlugins(PortfolioDownload, {
      keycloak: minimalKeycloakMock({}),
      props: { portfolioId: portfolioFixture.portfolioId },
    }).then(() => {
      cy.wait('@downloadComplete').then(() => {
        // Check if download button starts download process
        cy.get('[data-test="downloadButton"]').should('be.visible').click();
        cy.get('[data-test="downloadProgressSpinner"]').should('be.visible');
        cy.get('[data-test="downloadButton"]').should('not.exist'); // Button should be hidden during download
      });
    });
  });

  it('Check download progress updates', function (): void {
    cy.intercept('**/users/portfolios/*/enriched-portfolio', portfolioFixture).as('downloadComplete');

    // @ts-ignore Ignore TypeScript error for prop type mismatch temporarily
    cy.mountWithPlugins(PortfolioDownload, {
      keycloak: minimalKeycloakMock({}),
      props: { portfolioId: portfolioFixture.portfolioId },
    }).then(() => {
      cy.wait('@downloadComplete').then(() => {
        // Simulate download progress by checking the progress updates
        cy.get('[data-test="downloadButton"]').click();
        cy.get('[data-test="downloadProgressSpinner"]')
          .should('have.attr', 'percent-completed') // Assuming the spinner component tracks progress
          .and('be.greaterThan', 0)
          .and('be.lessThan', 100);
      });
    });
  });

  it('Check error messages during download failure', function (): void {
    cy.intercept('**/users/portfolios/*/enriched-portfolio', portfolioFixture).as('downloadComplete');
    cy.intercept('**/frameworkDataApi/exportCompanyAssociatedDataByDimensions', {
      statusCode: 500,
      body: 'Internal Server Error',
    }).as('downloadFailure');

    // @ts-ignore Ignore TypeScript error for prop type mismatch temporarily
    cy.mountWithPlugins(PortfolioDownload, {
      keycloak: minimalKeycloakMock({}),
      props: { portfolioId: portfolioFixture.portfolioId },
    }).then(() => {
      cy.wait('@downloadComplete').then(() => {
        cy.get('[data-test="downloadButton"]').click();
        cy.wait('@downloadFailure').then(() => {
          cy.get('[data-test="errorMessage"]').should('be.visible');
          cy.get('[data-test="errorMessage"]').should('contain', 'An error occurred during download');
        });
      });
    });
  });
});
