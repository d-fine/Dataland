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

    it('Check reporting period selection via MultiSelect', function (): void {
      cy.get('[data-test="latestReportingPeriodSwitch"]').click();
      cy.get('[data-test="reportingPeriodSelector"]').should('exist');
      cy.get('[data-test="reportingPeriodSelector"]').click();
      cy.get('.p-multiselect-list').contains('2024').click();
      cy.get('.p-multiselect-list').contains('2023').click();
    });

    it('Check error message visibility when no reporting period selected', function (): void {
      cy.get('[data-test="latestReportingPeriodSwitch"]').click();
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
      cy.stub(globalThis, 'XMLHttpRequest').callsFake(function () {
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
      cy.get('[data-test="latestReportingPeriodSwitch"]').click();
      cy.get('[data-test="reportingPeriodSelector"]').click();
      cy.get('.p-multiselect-list').contains('2024').click();
      cy.get('body').click(0, 0);
      cy.get('[data-test="fileTypeSelector"]').find('.p-select-dropdown').click();
      cy.get('.p-select-list-container').contains('Comma-separated Values').click();
      cy.get('[data-test="downloadDataButtonInModal"]').click();
    });

    it('Check that latest reporting period toggle is on by default and disables reporting period selector', function (): void {
      cy.get('[data-test="reportingPeriodSelector"]').should('have.class', 'p-disabled');
      cy.get('[data-test="latestReportingPeriodSwitch"]').click();
      cy.get('[data-test="reportingPeriodSelector"]').should('not.have.class', 'p-disabled');
    });

    it('Check that no reporting period error is shown when latest is selected by default', function (): void {
      cy.get('[data-test="fileTypeSelector"]').find('.p-select-dropdown').click();
      cy.get('.p-select-list-container').contains('Comma-separated Values').click();
      cy.get('[data-test="downloadDataButtonInModal"]').click();
      cy.get('[data-test="reportingYearError"]').should('not.exist');
    });

    it('Change framework and check that unavailable periods are disabled in dropdown', function (): void {
      cy.get('[data-test="latestReportingPeriodSwitch"]').click();
      cy.get('[data-test="frameworkSelector"]').find('.p-select-dropdown').click();
      cy.get('.p-select-list-container').contains('EU Taxonomy Financials').click();
      cy.get('[data-test="reportingPeriodSelector"]').click();
      cy.get('.p-multiselect-list').contains('2021').should('be.visible');
      cy.get('.p-multiselect-list').contains('2024').parent().should('have.attr', 'data-p-disabled', 'true');
    });
  });
});
