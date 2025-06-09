import { admin_name, admin_pw } from '@e2e/utils/Cypress';
import { getKeycloakToken } from '@e2e/utils/Auth.ts';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import { IdentifierType } from '@clients/backend';
import { describeIf } from '@e2e/support/TestUtility.ts';

describe('Portfolio Monitoring Bulk Data Request Modal', () => {
  describeIf(
    'As a user I want to be able to monitor a portfolio',
    { executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'] },
    () => {
      let permIdNonFinancial: string;
      let permIdFinancial: string;
      let permIdNoSector: string;

      const nonFinancialPortfolio = `nonFinancialPortfolio ${Date.now()}`;
      const financialPortfolio = `financialPortfolio ${Date.now()}`;
      const nonSectorPortfolio = `NonFinancialPortfolio ${Date.now()}`;
      const sfdrPortfolio = `sfdrPortfolio ${Date.now()}`;

      before(() => {
        getKeycloakToken(admin_name, admin_pw).then(async (token) => {
          const nonFinancialCompany = generateDummyCompanyInformation(`Test Co. ${Date.now()}`);
          const financialCompany = generateDummyCompanyInformation(`Test Co. ${Date.now()}`, 'financials');
          const noSectorCompany = generateDummyCompanyInformation(`Test Co. ${Date.now()}`, undefined);

          permIdNonFinancial = assertDefined(nonFinancialCompany.identifiers[IdentifierType.PermId][0]);
          await uploadCompanyViaApi(token, nonFinancialCompany);

          permIdFinancial = assertDefined(financialCompany.identifiers[IdentifierType.PermId][0]);
          await uploadCompanyViaApi(token, financialCompany);

          permIdNoSector = assertDefined(noSectorCompany.identifiers[IdentifierType.PermId][0]);
          await uploadCompanyViaApi(token, noSectorCompany);
        });
      });

      beforeEach(() => {
        cy.intercept('POST', '**/community/requests/bulk').as('postBulkRequest');
        cy.ensureLoggedIn(admin_name, admin_pw);
        cy.visitAndCheckAppMount('/portfolios');
      });

      /**
       * Helper Function for creating bulk requests
       */
      function testBulkDataRequest({
        portfolioName,
        permId,
        expectedDataTypes,
        notExpectedDataTypes = [],
        framework,
      }: {
        portfolioName: string;
        permId: string;
        expectedDataTypes: string[];
        notExpectedDataTypes?: string[];
        framework: string;
      }): void {
        cy.wait(Cypress.env('medium_timeout_in_ms') as number);

        cy.get('[data-test="addNewPortfolio"]').click({ force: true });
        cy.get('[name="portfolioName"]').type(portfolioName);
        cy.get('[data-test="saveButton"]').should('be.disabled');
        cy.get('[name="company-identifiers"]').type(permId);
        cy.get('[data-test="addCompanies"]').click();
        cy.get('[data-test="saveButton"]').should('not.be.disabled');
        cy.get('[data-test="saveButton"]').click();

        cy.wait(Cypress.env('medium_timeout_in_ms') as number);
        cy.get('[data-test="monitor-portfolio"]').filter(':visible').click();
        cy.get('[data-test="listOfReportingPeriods"]').click();
        cy.get('.p-dropdown-item').contains('2023').click();

        cy.get('[data-test="frameworkSelection"]')
          .contains(framework)
          .parent()
          .find('input[type="checkbox"]')
          .click({ force: true });

        cy.get('[data-test="saveChangesButton"]').click();

        cy.wait('@postBulkRequest')
          .its('request.body')
          .should((body) => {
            expect(body.reportingPeriods).to.include(2023);
            expectedDataTypes.forEach((type) => expect(body.dataTypes).to.include(type));
            notExpectedDataTypes.forEach((type) => expect(body.dataTypes).not.to.include(type));
          });
      }

      it('submits bulk data request when inputs are valid for non financial company', () => {
        testBulkDataRequest({
          portfolioName: nonFinancialPortfolio,
          permId: permIdNonFinancial,
          expectedDataTypes: ['eutaxonomy-non-financials', 'nuclear-and-gas'],
          notExpectedDataTypes: ['eutaxonomy-financials'],
          framework: 'EU Taxonomy',
        });
      });

      it('submits bulk data request when inputs are valid for financial company', () => {
        testBulkDataRequest({
          portfolioName: financialPortfolio,
          permId: permIdFinancial,
          expectedDataTypes: ['eutaxonomy-financials', 'nuclear-and-gas'],
          notExpectedDataTypes: ['eutaxonomy-non-financials'],
          framework: 'EU Taxonomy',
        });
      });

      it('submits bulk data request when inputs are valid for non sector company', () => {
        testBulkDataRequest({
          portfolioName: nonSectorPortfolio,
          permId: permIdNoSector,
          expectedDataTypes: ['eutaxonomy-financials', 'nuclear-and-gas', 'eutaxonomy-non-financials'],
          framework: 'EU Taxonomy',
        });
      });

      it('submits bulk data request when inputs are valid for non sector company for sfdr', () => {
        testBulkDataRequest({
          portfolioName: sfdrPortfolio,
          permId: permIdNoSector,
          expectedDataTypes: ['sfdr'],
          notExpectedDataTypes: ['eutaxonomy-financials', 'nuclear-and-gas', 'eutaxonomy-non-financials'],
          framework: 'SFDR',
        });
      });
    }
  );
});
