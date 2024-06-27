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
  'As a user I want to be able to conduct a bulk request request',
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
      chooseReportingPeriod();
      chooseFrameworks();

      cy.get("textarea[name='listOfCompanyIdentifiers']").type(`${permIdOfExistingCompany}, 12345incorrectNumber`);
      cy.get("button[type='submit']").should('exist').click();

      cy.wait('@postRequestData', { timeout: Cypress.env('short_timeout_in_ms') as number }).then((interception) => {
        checkIfIdentifiersProperlyDisplayed(interception);
      });

      cy.get('[data-test="acceptedIdentifiers"] [data-test="identifiersHeading"]').contains('1 REQUESTED IDENTIFIER');
      cy.get('[data-test="rejectedIdentifiers"] [data-test="identifiersHeading"]').contains('1 REJECTED IDENTIFIER');
    });

    it('When identifiers are accepted', () => {
      cy.intercept('POST', '**/community/requests/bulk').as('postRequestData');

      checksBasicValidation();
      chooseReportingPeriod();
      chooseFrameworks();

      cy.get("textarea[name='listOfCompanyIdentifiers']").type(permIdOfExistingCompany);
      cy.get("button[type='submit']").should('exist').click();

      cy.wait('@postRequestData', { timeout: Cypress.env('short_timeout_in_ms') as number }).then((interception) => {
        checkIfIdentifiersProperlyDisplayed(interception);
      });

      cy.get('[data-test="acceptedIdentifiers"]')
        .should('exist')
        .get('[data-test="identifiersHeading"')
        .contains('1 REQUESTED IDENTIFIER');

      cy.get('[data-test="requestStatusText"]').should('exist').contains('Success');
      cy.get("button[type='button']").should('exist').should('be.visible').click();
      cy.url().should('not.include', '/bulkdatarequest');
      cy.url().should('include', '/requests');
    });

    it('When identifiers are rejected', () => {
      checksBasicValidation();
      chooseFrameworks();

      cy.get("textarea[name='listOfCompanyIdentifiers']").type('12345incorrectNumber');
      cy.get("button[type='submit']").should('exist').click();

      cy.get('[data-test="selectedIdentifiersUnsuccessfulSubmit"]')
        .should('exist')
        .get('[data-test="identifiersHeading"')
        .contains('SELECTED IDENTIFIERS');

      cy.get('[data-test="requestStatusText"]').should('exist').contains('Request Unsuccessful');
    });

    /**
     * Choose reporting periods
     */
    function chooseReportingPeriod(): void {
      cy.get('[data-test="reportingPeriodsDiv"] div[data-test="toggleChipsFormInput"]').should('exist');
      cy.get('[data-test="toggle-chip"').should('have.length', 5).first().click();
      cy.get('[data-test="toggle-chip"').should('have.length', 5).first().should('have.class', 'toggled');

      cy.get("div[data-test='reportingPeriodsDiv'] p[data-test='reportingPeriodErrorMessage'").should('not.exist');
    }

    /**
     * Chose frameworks
     */
    function chooseFrameworks(): void {
      const numberOfFrameworks = Object.keys(FRAMEWORKS_WITH_VIEW_PAGE).length;
      cy.get('[data-test="selectFrameworkSelect"] .p-multiselect').should('exist').click();
      cy.get('.p-multiselect-panel ul.p-multiselect-items li.p-multiselect-item')
        .should('have.length', numberOfFrameworks)
        .eq(3)
        .click();
      cy.get("div[data-test='addedFrameworks'] span").should('have.length', 1);
    }

    /**
     * Checks whether identifiers are displayed correctly on boxes
     * @param interception request
     */
    function checkIfIdentifiersProperlyDisplayed(interception: Interception): void {
      if (interception.response !== undefined) {
        const rejectedIdentifiers = (interception.response.body as BulkDataRequestResponse).rejectedCompanyIdentifiers;
        const acceptedIdentifiers = (interception.response.body as BulkDataRequestResponse).acceptedCompanyIdentifiers;
        if (rejectedIdentifiers.length > 0) {
          cy.get('[data-test="rejectedIdentifiers"] [data-test="identifiersList"]')
            .children()
            .should('have.length', rejectedIdentifiers.length);
        }
        if (acceptedIdentifiers.length > 0) {
          cy.get('[data-test="acceptedIdentifiers"] [data-test="identifiersList"]').should(
            'have.length',
            acceptedIdentifiers.length
          );
        }
      }
    }

    /**
     * Checks basic validation
     */
    function checksBasicValidation(): void {
      cy.get("button[type='submit']").should('exist').click();

      cy.get("div[data-test='reportingPeriodsDiv'] p[data-test='reportingPeriodErrorMessage'")
        .should('be.visible')
        .should('contain.text', 'Select at least one reporting period.');

      cy.get("div[data-test='selectFrameworkDiv'] li[data-message-type='validation']")
        .should('be.visible')
        .should('contain.text', 'Select at least one framework');

      cy.get("div[data-test='selectIdentifiersDiv'] li[data-message-type='validation']")
        .should('be.visible')
        .should('contain.text', 'Provide at least one identifier');
    }
  }
);
