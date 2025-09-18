import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import DownloadData from '@/components/general/DownloadData.vue';
import { type EnrichedPortfolio } from '@clients/userservice';

describe('Check the Portfolio Download view', function (): void {
  describe('DownloadData Component Tests', function () {
    let portfolioFixture: EnrichedPortfolio;

    before(function () {
      cy.fixture('enrichedPortfolio.json').then(function (jsonContent) {
        portfolioFixture = jsonContent as EnrichedPortfolio;
      });
    });

    beforeEach(function () {
      // @ts-ignore
      cy.mountWithPlugins(DownloadData, {
        keycloak: minimalKeycloakMock({}),
        global: {
          provide: {
            dialogRef: {
              value: {
                data: {
                  portfolio: portfolioFixture,
                  reportingPeriodsPerFramework: [
                    ['sfdr', ['2024', '2023', '2022']],
                    ['eutaxonomy-financials', ['2021']],
                  ],
                  isDownloading: false,
                },
              },
            },
            getKeycloakPromise: () => Promise.resolve(minimalKeycloakMock({})),
          },
        },
      });
    });

    it('Check framework selection', function (): void {
      cy.get('[data-test="frameworkSelector"]').find('.p-select-dropdown').click();
      cy.get('.p-select-list-container').contains('SFDR').click();
    });

    it('Check file type selection', function (): void {
      cy.get('[data-test="fileTypeSelector"]').find('.p-select-dropdown').click();
      cy.get('.p-select-list-container').contains('Comma-separated Values').click();
    });

    it('Check reporting period type selection', function (): void {
      cy.get('[data-test="listOfReportingPeriods"]').should('exist');
      const reportingYears = ['2024', '2023', '2022'];
      reportingYears.forEach((year) => {
        cy.get('[data-test="listOfReportingPeriods"]').contains(year).should('be.visible').click();
      });
    });

    it('Check error message visibility when no reporting period selected', function (): void {
      cy.get('[data-test="downloadDataButtonInModal"]').click();
      cy.get('[data-test="frameworkSelector"]').find('.p-select-dropdown').click();
      cy.get('.p-select-list-container').contains('SFDR').click();
      cy.get('[data-test="fileTypeSelector"]').find('.p-select-dropdown').click();
      cy.get('.p-select-list-container').contains('Comma-separated Values').click();
      cy.get('[data-test="downloadDataButtonInModal"]').click();
      cy.get('[data-test="reportingYearError"]').should('be.visible');
      cy.get('[data-test="reportingYearError"]').should('contain', 'Please select a reporting period.');
    });
    it('Check error message visibility when no file type selected', function (): void {
      cy.get('[data-test="downloadDataButtonInModal"]').click();
      cy.get('[data-test="fileTypeError"]').should('be.visible');
      cy.get('[data-test="fileTypeError"]').should('contain', 'Please select a file type.');
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
      cy.get('[data-test="frameworkSelector"]').find('.p-select-dropdown').click();
      cy.get('.p-select-list-container').contains('SFDR').click();
      cy.get('[data-test="listOfReportingPeriods"]').contains('2024').click();
      cy.get('[data-test="fileTypeSelector"]').find('.p-select-dropdown').click();
      cy.get('.p-select-list-container').contains('Comma-separated Values').click();
      cy.get('[data-test="downloadDataButtonInModal"]').click();
    });

    it('Change framework and check that only 2021 is selectable for eu-taxonomy-financials framework', function (): void {
      cy.get('[data-test="frameworkSelector"]').find('.p-select-dropdown').click();
      cy.get('.p-select-list-container').contains('SFDR').click();
      ['2024', '2023', '2022'].forEach((year) => {
        cy.get('[data-test="listOfReportingPeriods"]').contains(year).should('be.visible').click();
      });
      cy.get('[data-test="frameworkSelector"]').find('.p-select-dropdown').click();
      cy.get('.p-select-list-container').contains('EU Taxonomy Financials').click();
      cy.get('[data-test="listOfReportingPeriods"]').should('exist');
      ['2025', '2024', '2023', '2022', '2020'].forEach((year) => {
        cy.get('[data-test="listOfReportingPeriods"]')
          .contains(year)
          .parents('[data-test="toggle-chip"]')
          .should('have.class', 'disabled');
      });
      cy.get('[data-test="listOfReportingPeriods"]').contains('2021').should('be.visible').click();
    });
  });
});
