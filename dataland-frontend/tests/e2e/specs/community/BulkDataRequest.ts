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
    let testCompanyName: string;
    before(() => {
      getKeycloakToken(admin_name, admin_pw).then(async (token) => {
        const companyToUpload = generateDummyCompanyInformation(`Test Co. ${new Date().getTime()}`);
        permIdOfExistingCompany = assertDefined(companyToUpload.identifiers[IdentifierType.PermId][0]);
        testCompanyName = companyToUpload.companyName;
        await uploadCompanyViaApi(token, companyToUpload);
      });
    });

    beforeEach(() => {
      cy.ensureLoggedIn(admin_name, admin_pw);
      cy.visitAndCheckAppMount('/bulkdatarequest');
      cy.closeCookieBannerIfItExists();
    });

    it('When identifiers are accepted and rejected', () => {
      cy.intercept('POST', '**/community/requests/bulk').as('postRequestData');
      checksBasicValidation();
      chooseFirstReportingPeriod();
      chooseFrameworkByIndex(1);

      // Create a request
      cy.get('[data-test="emailOnUpdateInput"]').scrollIntoView();
      cy.get('[data-test="emailOnUpdateInput"]').should('not.have.class', 'p-inputswitch-checked');
      cy.get('[data-test="emailOnUpdateInput"]').click();
      cy.get('[data-test="emailOnUpdateInput"]').should('have.class', 'p-inputswitch-checked');
      cy.get('textarea[name="listOfCompanyIdentifiers"]').type(`${permIdOfExistingCompany}, 12345incorrectNumber`);
      cy.get('button[type="submit"]').click();

      cy.wait('@postRequestData', { timeout: Cypress.env('short_timeout_in_ms') as number }).then((interception) => {
        checkIfIdentifiersProperlyDisplayed(interception);
      });

      cy.get('[data-test="reportingPeriodsHeading"]').contains('1 REPORTING PERIOD');
      cy.get('[data-test="frameworksHeading"]').contains('1 FRAMEWORK');
      verifyOnRequestPage(true);
    });

    it('When identifiers are accepted', () => {
      cy.intercept('POST', '**/community/requests/bulk').as('postRequestData');
      checksBasicValidation();
      chooseFirstReportingPeriod();
      chooseFrameworkByIndex(2);

      cy.get('textarea[name="listOfCompanyIdentifiers"]').type(permIdOfExistingCompany);
      cy.get('button[type="submit"]').click();

      cy.wait('@postRequestData', { timeout: Cypress.env('short_timeout_in_ms') as number }).then((interception) => {
        checkIfIdentifiersProperlyDisplayed(interception);
      });

      // Verify on the request page, that the request was created successfully
      cy.get('[data-test="requestStatusText"]').contains('Success');
      verifyOnRequestPage(false);
    });

    it('When request already exists', () => {
      checksBasicValidation();
      chooseFirstReportingPeriod();
      chooseFrameworkByIndex(3);
      cy.get('textarea[name="listOfCompanyIdentifiers"]').type(permIdOfExistingCompany);
      cy.get('button[type="submit"]').click();

      cy.visit('bulkdatarequest');
      cy.intercept('POST', '**/community/requests/bulk').as('postRequestData');
      checksBasicValidation();
      chooseFirstReportingPeriod();
      chooseFrameworkByIndex(3);

      cy.get('textarea[name="listOfCompanyIdentifiers"]').type(permIdOfExistingCompany);
      cy.get('button[type="submit"]').click();

      cy.wait('@postRequestData', { timeout: Cypress.env('short_timeout_in_ms') as number }).then((interception) => {
        checkIfIdentifiersProperlyDisplayed(interception);
      });

      cy.get('[data-test="requestStatusText"]').contains('Success');
      cy.get('button[type="button"]').should('be.visible');
      cy.get('button[type="button"]').click();
      cy.url().should('not.include', '/bulkdatarequest');
      cy.url().should('include', '/requests');
    });

    it('When identifiers are rejected', () => {
      cy.intercept('POST', '**/community/requests/bulk').as('postRequestData');
      checksBasicValidation();
      chooseFirstReportingPeriod();
      chooseFrameworkByIndex(1);

      cy.get('textarea[name="listOfCompanyIdentifiers"]').type('12345incorrectNumber, 54321incorrectnumber');
      cy.get('button[type="submit"]').click();

      cy.wait('@postRequestData', { timeout: Cypress.env('short_timeout_in_ms') as number }).then((interception) => {
        checkIfIdentifiersProperlyDisplayed(interception);
      });

      cy.get('[data-test="requestStatusText"]').contains('Request Unsuccessful');
    });

    /**
     * Choose reporting periods
     */
    function chooseFirstReportingPeriod(): void {
      cy.get('[data-test="reportingPeriodsDiv"] div[data-test="toggleChipsFormInput"]').should('exist');
      cy.get('[data-test="toggle-chip"]').should('have.length', 5);
      cy.get('[data-test="toggle-chip"]').first().click();
      cy.get('[data-test="toggle-chip"]').first().should('have.class', 'toggled');

      cy.get('div[data-test="reportingPeriodsDiv"] p[data-test="reportingPeriodErrorMessage"]').should('not.exist');
    }

    /**
     * Choose frameworks by index
     * @param index The index of the framework to choose.
     */
    function chooseFrameworkByIndex(index: number): void {
      const numberOfFrameworks = Object.keys(FRAMEWORKS_WITH_VIEW_PAGE).length;
      cy.get('[data-test="selectFrameworkSelect"] .p-multiselect').click();
      cy.get('.p-multiselect-panel ul.p-multiselect-items li.p-multiselect-item').should(
        'have.length',
        numberOfFrameworks
      );
      cy.get('.p-multiselect-panel ul.p-multiselect-items li.p-multiselect-item').eq(index).click();
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
      cy.get('[data-test="alreadyExistingDatasetsHeader"]').find('.p-badge').contains(alreadyExistingDatasets.length);
      cy.get('[data-test="alreadyExistingNonFinalRequestsHeader"]')
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
      cy.get('[data-test="rejectedCompanyIdentifiersContent"]').should('contain.text', '');
      cy.get('[data-test="rejectedCompanyIdentifiersContent"]').within(($div) => {
        const identifiers: string[] = $div
          .text()
          .split(', ')
          .filter((identifier) => identifier != '');
        assert(identifiers.length == rejectedCompanyIdentifiers.length);
      });
    }

    /**
     * Verifies the successful creation of the request on the (single) request page
     * @param emailChecked true if the field should be checked, false otherwise
     */
    function verifyOnRequestPage(emailChecked: boolean): void {
      cy.get('button[type="button"]').should('be.visible');
      cy.get('button[type="button"]').click();
      cy.url().should('not.include', '/bulkdatarequest');
      cy.url().should('include', '/requests');
      cy.get(`td:contains("${testCompanyName}")`).first().scrollIntoView();
      cy.get(`td:contains("${testCompanyName}")`).first().click();

      // Inspect the page for a single request
      cy.url({ timeout: Cypress.env('long_timeout_in_ms') as number }).should('contain', '/requests/');
      cy.get(`div.card__data:contains("${testCompanyName}")`).should('be.visible');
      cy.get('[data-test="card_requestIs"]').should('contain.text', 'Request is:Openand Access is:Public since');
      cy.get('[data-test="emailOnUpdateInput"]').should(
        (emailChecked ? '' : 'not.') + 'have.class',
        'p-inputswitch-checked'
      );
    }

    /**
     * Checks basic validation
     */
    function checksBasicValidation(): void {
      cy.get('button[type="submit"]').click();

      cy.get('div[data-test="reportingPeriodsDiv"] p[data-test="reportingPeriodErrorMessage"]')
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
