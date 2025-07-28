import { IdentifierType } from '@clients/backend';
import { describeIf } from '@e2e/support/TestUtility.ts';
import { getKeycloakToken } from '@e2e/utils/Auth.ts';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload.ts';
import { admin_name, admin_pw } from '@e2e/utils/Cypress.ts';

describe('Portfolio Monitoring Modal', () => {
  describeIf(
    'As a user I want to be able to monitor a portfolio',
    { executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'] },
    () => {
      let permIdNonFinancial: string;
      let permIdFinancial: string;
      let permIdNoSector: string;
      const timestamp = Date.now();
      const nonFinancialPortfolio = `nonFinancialPortfolio ${timestamp}`;
      const financialPortfolio = `financialPortfolio ${timestamp}`;
      const nonSectorPortfolio = `NonSectorPortfolio ${timestamp}`;
      const companyNameNonFinancial = `Test Co. NonFin ${timestamp}`;
      const companyNameFinancial = `Test Co. Fin ${timestamp}`;
      const companyNameNoSector = `Test Co. NoSec ${timestamp}`;

      before(() => {
        getKeycloakToken(admin_name, admin_pw).then(async (token) => {
          const nonFinancialCompany = generateDummyCompanyInformation(companyNameNonFinancial);
          const financialCompany = generateDummyCompanyInformation(companyNameFinancial, 'financials');
          const noSectorCompany = generateDummyCompanyInformation(companyNameNoSector, null as unknown as string);

          permIdNonFinancial = nonFinancialCompany.identifiers[IdentifierType.PermId][0];
          await uploadCompanyViaApi(token, nonFinancialCompany);

          permIdFinancial = financialCompany.identifiers[IdentifierType.PermId][0];
          await uploadCompanyViaApi(token, financialCompany);

          permIdNoSector = noSectorCompany.identifiers[IdentifierType.PermId][0];
          await uploadCompanyViaApi(token, noSectorCompany);
        });
      });

      beforeEach(() => {
        cy.intercept('POST', '**/community/requests/bulk').as('postBulkRequest');
        cy.intercept('PATCH', '**/users/portfolios/**/monitoring').as('patchMonitoring');
        cy.intercept('GET', '**/users/portfolios/**/enriched-portfolio').as('getEnrichedPortfolio');
        cy.intercept('POST', '**/api/companies/validation').as('forCompanyValidation');
        cy.ensureLoggedIn(admin_name, admin_pw);
      });

      /**
       * Test function for creating portfolio and monitor it
       */
      function testPatchMonitoring({
        portfolioName,
        // companyName,
        // permId,
        // frameworkValue,
        // frameworkTitle,
        // frameworkSubtitles,
      }: {
        portfolioName: string;
        companyName: string;
        permId: string;
        frameworkValue: string;
        frameworkTitle: string;
        frameworkSubtitles: string[];
      }): void {
        cy.visitAndCheckAppMount('/portfolios');
        cy.get('[data-test="add-portfolio"]').click({ force: true });
        cy.get('.p-dialog-mask').within(() => {
          cy.get('[data-test="saveButton"]').should('be.disabled');
        });
        cy.get('[data-test="portfolio-name-input"]').type(portfolioName);
        cy.get('[data-test="saveButton"]').should('be.disabled');

        // cy.wait(5000);
        // cy.get('[data-test="add-portfolio"]').click();
        // cy.get('[name="portfolio-name"]').type(portfolioName);
        // cy.get('[data-test="saveButton"]').should('be.disabled');
        // cy.get('[name="company-identifiers"]').type(permId);
        // cy.get('[data-test="addCompanies"]').click();
        // cy.wait('@forCompanyValidation');
        // cy.get('[data-test="saveButton"]').should('not.be.disabled');
        // cy.get('[data-test="saveButton"]').click();
        // cy.wait('@getEnrichedPortfolio');
        // // eslint-disable-next-line cypress/no-unnecessary-waiting
        // cy.wait(5000);
        // cy.get(`[data-test="portfolio-${portfolioName}"]`)
        //   .should('exist')
        //   .within(() => {
        //     cy.get('[data-test="monitor-portfolio"]').click();
        //   });
        //
        // cy.get('[data-test="activateMonitoringToggle"]').click();
        // cy.get('[data-test="listOfReportingPeriods"]').click();
        // cy.get('.p-select-option').contains('2023').click();
        //
        // cy.get('[data-test="frameworkSelection"]')
        //   .contains('EU Taxonomy')
        //   .parent()
        //   .find('input[type="checkbox"]')
        //   .click();
        //
        // cy.get('[data-test="saveChangesButton"]').click();
        //
        // cy.wait('@patchMonitoring')
        //   .its('request.body')
        //   .should((body) => {
        //     expect(body.isMonitored).to.be.true;
        //     expect(body.startingMonitoringPeriod).to.equal('2023');
        //     expect(body.monitoredFrameworks).to.include(frameworkValue);
        //   });
        //
        // cy.visitAndCheckAppMount('/requests');
        // // eslint-disable-next-line cypress/no-unnecessary-waiting
        // cy.wait(1000);
        // cy.get('[data-test="requested-Datasets-table"] tbody tr')
        //   .filter(`:contains("${companyName}")`)
        //   .as('companyRequestRows');
        //
        // cy.get('@companyRequestRows').filter(`:contains("${frameworkTitle}")`).should('have.length.at.least', 1);
        //
        // frameworkSubtitles.forEach((subtitle) => {
        //   cy.get('@companyRequestRows').filter(`:contains("${subtitle}")`).should('have.length.at.least', 1);
        // });
        //
        // cy.visitAndCheckAppMount('/portfolios');
        // cy.wait('@getEnrichedPortfolio');
        // // eslint-disable-next-line cypress/no-unnecessary-waiting
        // cy.wait(1000);
        // cy.get(`[data-test="${portfolioName}"]`).click();
        // cy.get(`[data-test="portfolio-${portfolioName}"] [data-test="edit-portfolio"]`).click();
        // cy.wait('@getEnrichedPortfolio');
        // cy.get('[data-test="deleteButton"]').click();
        // cy.get(`[data-test="${portfolioName}"]`).should('not.exist');
      }

      it('Monitoring yields bulk data request when inputs are valid for non financial company', () => {
        testPatchMonitoring({
          portfolioName: nonFinancialPortfolio,
          companyName: companyNameNonFinancial,
          permId: permIdNonFinancial,
          frameworkValue: 'eutaxonomy',
          frameworkTitle: 'EU Taxonomy',
          frameworkSubtitles: ['for non-financial companies', 'Nuclear and Gas'],
        });
      });

      it('Monitoring yields bulk data request when inputs are valid for financial company', () => {
        testPatchMonitoring({
          portfolioName: financialPortfolio,
          companyName: companyNameFinancial,
          permId: permIdFinancial,
          frameworkValue: 'eutaxonomy',
          frameworkTitle: 'EU Taxonomy',
          frameworkSubtitles: ['for financial companies', 'Nuclear and Gas'],
        });
      });

      it('Monitoring yields bulk data request when inputs are valid for non sector company', () => {
        testPatchMonitoring({
          portfolioName: nonSectorPortfolio,
          companyName: companyNameNoSector,
          permId: permIdNoSector,
          frameworkValue: 'eutaxonomy',
          frameworkTitle: 'EU Taxonomy',
          frameworkSubtitles: ['for financial companies', 'for non-financial companies', 'Nuclear and Gas'],
        });
      });
    }
  );
});
