import { admin_name, admin_pw } from '@e2e/utils/Cypress';
import { type Interception } from 'cypress/types/net-stubbing';
import { type BulkDataRequestResponse } from '@clients/communitymanager';
import { describeIf } from '@e2e/support/TestUtility';
import { IdentifierType } from '@clients/backend';
import { getKeycloakToken } from '@e2e/utils/Auth';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { FRAMEWORKS_WITH_VIEW_PAGE } from '@/utils/Constants';

describeIf(
  'As a user I want to be able to conduct a bulk data request',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  () => {
    let permIdOfExistingCompany: string;
    before(() => {
      getKeycloakToken(admin_name, admin_pw).then(async (token) => {
        const companyToUpload = generateDummyCompanyInformation(`Test Co. ${new Date().getTime()}`);
        permIdOfExistingCompany = assertDefined(companyToUpload.identifiers[IdentifierType.PermId][0]);
        await uploadCompanyViaApi(token, companyToUpload);
      });
    });

    beforeEach(() => {
      cy.ensureLoggedIn(admin_name, admin_pw);
      cy.visitAndCheckAppMount('/bulkdatarequest');
    });

    it('When identifiers are accepted and rejected', () => {
      cy.intercept('POST', '**/community/requests/bulk').as('postRequestData');
      checksBasicValidation();
      chooseFirstReportingPeriod();
      chooseFrameworkByIndex(1);

      cy.get('textarea[name="listOfCompanyIdentifiers"]').type(`${permIdOfExistingCompany}, 12345incorrectNumber`);
      cy.get('button[type="submit"]').should('exist').click();

      cy.wait('@postRequestData', { timeout: Cypress.env('short_timeout_in_ms') as number }).then((interception) => {
        checkIfIdentifiersProperlyDisplayed(interception);
      });

      cy.get('[data-test="reportingPeriodsHeading"]').contains('1 REPORTING PERIOD');
      cy.get('[data-test="frameworksHeading"]').contains('1 FRAMEWORK');
    });

    it('When identifiers are accepted', () => {
      cy.intercept('POST', '**/community/requests/bulk').as('postRequestData');
      checksBasicValidation();
      chooseFirstReportingPeriod();
      chooseFrameworkByIndex(2);

      cy.get('textarea[name="listOfCompanyIdentifiers"]').type(permIdOfExistingCompany);
      cy.get('button[type="submit"]').should('exist').click();

      cy.wait('@postRequestData', { timeout: Cypress.env('short_timeout_in_ms') as number }).then((interception) => {
        checkIfIdentifiersProperlyDisplayed(interception);
      });

      cy.get('[data-test="requestStatusText"]').should('exist').contains('Success');
      cy.get('button[type="button"]').should('exist').should('be.visible').click();
      cy.url().should('not.include', '/bulkdatarequest');
      cy.url().should('include', '/requests');
    });

    it('When request already exists', () => {
      checksBasicValidation();
      chooseFirstReportingPeriod();
      chooseFrameworkByIndex(3);
      cy.get('textarea[name="listOfCompanyIdentifiers"]').type(permIdOfExistingCompany);
      cy.get('button[type="submit"]').should('exist').click();

      cy.visit('bulkdatarequest');
      cy.intercept('POST', '**/community/requests/bulk').as('postRequestData');
      checksBasicValidation();
      chooseFirstReportingPeriod();
      chooseFrameworkByIndex(3);

      cy.get('textarea[name="listOfCompanyIdentifiers"]').type(permIdOfExistingCompany);
      cy.get('button[type="submit"]').should('exist').click();

      cy.wait('@postRequestData', { timeout: Cypress.env('short_timeout_in_ms') as number }).then((interception) => {
        checkIfIdentifiersProperlyDisplayed(interception);
      });

      cy.get('[data-test="requestStatusText"]').should('exist').contains('Success');
      cy.get('button[type="button"]').should('exist').should('be.visible').click();
      cy.url().should('not.include', '/bulkdatarequest');
      cy.url().should('include', '/requests');
    });

    it('When identifiers are rejected', () => {
      cy.intercept('POST', '**/community/requests/bulk').as('postRequestData');
      checksBasicValidation();
      chooseFirstReportingPeriod();
      chooseFrameworkByIndex(1);

      cy.get('textarea[name="listOfCompanyIdentifiers"]').type('12345incorrectNumber, 54321incorrectnumber');
      cy.get('button[type="submit"]').should('exist').click();

      cy.wait('@postRequestData', { timeout: Cypress.env('short_timeout_in_ms') as number }).then((interception) => {
        checkIfIdentifiersProperlyDisplayed(interception);
      });

      cy.get('[data-test="requestStatusText"]').should('exist').contains('Request Unsuccessful');
    });

    /**
     * Choose reporting periods
     */
    function chooseFirstReportingPeriod(): void {
      cy.get('[data-test="reportingPeriodsDiv"] div[data-test="toggleChipsFormInput"]').should('exist');
      cy.get('[data-test="toggle-chip"').should('have.length', 5).first().click();
      cy.get('[data-test="toggle-chip"').should('have.length', 5).first().should('have.class', 'toggled');

      cy.get('div[data-test="reportingPeriodsDiv"] p[data-test="reportingPeriodErrorMessage"').should('not.exist');
    }

    /**
     * Choose frameworks by index
     * @param index The index of the framework to choose.
     */
    function chooseFrameworkByIndex(index: number): void {
      const numberOfFrameworks = Object.keys(FRAMEWORKS_WITH_VIEW_PAGE).length;
      cy.get('[data-test="selectFrameworkSelect"] .p-multiselect').should('exist').click();
      cy.get('.p-multiselect-panel ul.p-multiselect-items li.p-multiselect-item')
        .should('have.length', numberOfFrameworks)
        .eq(index)
        .click();
      cy.get('div[data-test="addedFrameworks"] span').should('have.length', 1);
    }

    /**
     * Checks whether identifiers are displayed correctly on boxes
     * @param interception request
     */
    function checkIfIdentifiersProperlyDisplayed(interception: Interception): void {
      if (interception.response === undefined || interception.response === null) {
        return;
      }
      const bulkDataRequestResponse = interception.response.body as BulkDataRequestResponse;
      const acceptedDataRequests = bulkDataRequestResponse.acceptedDataRequests;
      const alreadyExistingDatasets = bulkDataRequestResponse.alreadyExistingDatasets;
      const alreadyExistingNonFinalRequests = bulkDataRequestResponse.alreadyExistingNonFinalRequests;
      const rejectedCompanyIdentifiers = bulkDataRequestResponse.rejectedCompanyIdentifiers;

      cy.get('[data-test="acceptedDataRequestsHeader"]').find('.p-badge').contains(acceptedDataRequests.length);
      cy.get('[data-test="alreadyExistingDatasetsHeader"]')
        .should('exist')
        .find('.p-badge')
        .contains(alreadyExistingDatasets.length);
      cy.get('[data-test="alreadyExistingNonFinalRequestsHeader"]')
        .should('exist')
        .find('.p-badge')
        .contains(alreadyExistingNonFinalRequests.length);
      cy.get('[data-test="rejectedCompanyIdentifiersHeader"]')
        .find('.p-badge')
        .contains(rejectedCompanyIdentifiers.length)
        .click();

      cy.get('[data-test="acceptedDataRequestsContent"]').should('have.length', acceptedDataRequests.length);
      cy.get('[data-test="alreadyExistingDatasetsContent"]').should('have.length', alreadyExistingDatasets.length);
      cy.get('[data-test="alreadyExistingNonFinalRequestsContent"]').should(
        'have.length',
        alreadyExistingNonFinalRequests.length
      );
      cy.get('[data-test="rejectedCompanyIdentifiersContent"]')
        .should('contain.text', '')
        .within(($div) => {
          const identifiers: string[] = $div
            .text()
            .split(', ')
            .filter((identifier) => identifier != '');
          assert(identifiers.length == rejectedCompanyIdentifiers.length);
        });
    }

    /**
     * Checks basic validation
     */
    function checksBasicValidation(): void {
      cy.get('button[type="submit"]').should('exist').click();

      cy.get('div[data-test="reportingPeriodsDiv"] p[data-test="reportingPeriodErrorMessage"')
        .should('be.visible')
        .should('contain.text', 'Select at least one reporting period.');

      cy.get('div[data-test="selectFrameworkDiv"] li[data-message-type="validation"]')
        .should('be.visible')
        .should('contain.text', 'Select at least one framework');

      cy.get('div[data-test="selectIdentifiersDiv"] li[data-message-type="validation"]')
        .should('be.visible')
        .should('contain.text', 'Provide at least one identifier');
    }
  }
);
