import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import PortfolioDownload from '@/components/resources/portfolio/PortfolioDownload.vue';
import { type EnrichedPortfolio } from '@clients/userservice';

describe('Check the Portfolio Download view', function (): void {
  describe('PortfolioDownload Component Tests', function () {
    let portfolioFixture: EnrichedPortfolio;

    before(function () {
      cy.fixture('enrichedPortfolio.json').then(function (jsonContent) {
        portfolioFixture = jsonContent as EnrichedPortfolio;
      });
    });

    beforeEach(function () {
      // @ts-ignore
      cy.mountWithPlugins(PortfolioDownload, {
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
            getKeycloakPromise: () => Promise.resolve(minimalKeycloakMock({})),
          },
        },
        props: { portfolioId: portfolioFixture.portfolioId },
      });
    });

    it('Check framework selection', function (): void {
      cy.get('[data-test="frameworkSelector"]').should('exist');
      cy.get('[data-test="frameworkSelector"]').select('EU Taxonomy Non Financials');
    });

    it('Check file type selection', function (): void {
      cy.get('[data-test="fileTypeSelector"]').should('exist');
      cy.get('[data-test="fileTypeSelector"]').select('CSV');
      cy.get('[data-test="fileTypeSelector"]').should('have.value', 'CSV');
    });

    it('Check reporting period type selection', function (): void {
      cy.get('[data-test="listOfReportingPeriods"]').should('exist');
      cy.get('[data-test="fileTypeSelector"]').select('CSV');
      const reportingYears = ['2025', '2024', '2023', '2022', '2021', '2020'];
      reportingYears.forEach((year) => {
        cy.get('[data-test="listOfReportingPeriods"]').contains(year).should('be.visible').click({ force: true });
      });
    });

    it('Check error message visibility when no framework selected', function (): void {
      cy.get('[data-test="downloadButton"]').click();
      cy.get('[data-test="frameworkError"]').should('be.visible');
      cy.get('[data-test="frameworkError"]').should('contain', 'Please select Framework.');
    });

    it('Check error message visibility when no reporting period selected', function (): void {
      cy.get('[data-test="downloadButton"]').click();
      cy.get('[data-test="frameworkSelector"]').select('sfdr');
      cy.get('[data-test="fileTypeSelector"]').select('CSV');
      cy.get('[data-test="downloadButton"]').click();
      cy.get('[data-test="reportingPeriodsError"]').should('be.visible');
      cy.get('[data-test="reportingPeriodsError"]').should('contain', 'Please select at least one Reporting Period.');
    });

    it('Check error message visibility when no file type selected', function (): void {
      cy.get('[data-test="downloadButton"]').click();
      cy.get('[data-test="fileTypeError"]').should('be.visible');
      cy.get('[data-test="fileTypeError"]').should('contain', 'Please select a File Type.');
    });

    it('Check download button functionality', function (): void {
      cy.stub(window, 'XMLHttpRequest').callsFake(function () {
        return {
          open: cy.stub(),
          send: cy.stub(),
          onprogress: null,
          onload: null,
          onerror: null,
          onabort: null,
          setRequestHeader: cy.stub(),
          responseType: '',
        };
      });

      cy.get('[data-test="frameworkSelector"]').select('sfdr');
      cy.get('[data-test="listOfReportingPeriods"]').contains('2024').click();
      cy.get('[data-test="fileTypeSelector"]').select('CSV');

      cy.get('[data-test="downloadButton"]').click();
      cy.get('[data-test="downloadButton"]').should('not.exist');
      cy.get('[data-test="downloadSpinner"]').should('exist');
    });
  });
});
