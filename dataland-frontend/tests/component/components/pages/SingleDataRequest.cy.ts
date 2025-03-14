import SingleDataRequestComponent from '@/components/pages/SingleDataRequest.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { singleDataRequestPage } from '@sharedUtils/components/SingleDataRequest';
import { type SingleDataRequest } from '@clients/communitymanager';
import router from '@/router';

describe('Component tests for the single data request page', function (): void {
  it('check submitting with message', function () {
    cy.mountWithPlugins(SingleDataRequestComponent, {
      keycloak: minimalKeycloakMock({}),
      router: router,
    }).then(() => {
      fillMandatoryFields();

      cy.get("[data-test='contactEmail']").should('exist').type('example@example.com,   , someone@example.com ');

      cy.get("[data-test='dataRequesterMessage']").type('test text');

      cy.get("[data-test='conditionsNotAcceptedErrorMessage']").should('not.be.visible');

      cy.get("button[type='submit']").should('exist').click();

      cy.get("[data-test='informationCompanyOwnership']").should('be.visible');

      cy.get("[data-test='conditionsNotAcceptedErrorMessage']").should('be.visible');
      cy.get("input[data-test='acceptConditionsCheckbox']").click();

      cy.intercept('**/single', (request) => {
        const singleDataRequest = assertDefined(request.body as SingleDataRequest);
        expect(singleDataRequest.contacts).to.deep.equal(['example@example.com', 'someone@example.com']);
        expect(singleDataRequest.message).to.deep.equal('test text');

        request.reply({
          statusCode: 200,
        });
      });

      cy.get("button[type='submit']").should('exist').click();
      cy.get("[data-test='requestStatusText']").should('contain.text', 'Submitting your data request was successful.');
    });
  });

  it('Check email notification toggle', () => {
    cy.mountWithPlugins(SingleDataRequestComponent, {
      keycloak: minimalKeycloakMock({}),
      router: router,
    }).then(() => {
      cy.get('[data-test="emailOnUpdate"]').within(() => {
        cy.get('[data-test="emailOnUpdateInput"]').scrollIntoView();
        cy.get('[data-test="emailOnUpdateInput"]').should('be.visible');
        cy.get('[data-test="emailOnUpdateText"]').should('contain.text', 'summary');
        cy.get('[data-test="emailOnUpdateInput"]').click();
        cy.get('[data-test="emailOnUpdateText"]').should('contain.text', 'immediate');
        cy.get('[data-test="emailOnUpdateInput"]').click();
        cy.get('[data-test="emailOnUpdateText"]').should('contain.text', 'summary');
      });
    });
  });

  it('check email validation', function (): void {
    cy.mountWithPlugins(SingleDataRequestComponent, {
      keycloak: minimalKeycloakMock({}),
      router: router,
    }).then(() => {
      fillMandatoryFields();

      checkContactsNotValid('example');
      checkContactsNotValid('example.com');
      checkContactsNotValid('@example.com');
      checkContactsNotValid('test@.com');
      checkContactsNotValid('test@@example.com');
      checkContactsNotValid('test@example');

      checkContactsValid('test@example.com');
      checkContactsNotValid('test@examplecom');
      checkContactsValid('test.jurgen@example.com');
      checkContactsValid('Test.tEsT@Example.cOm');

      checkContactsNotValid('test@example.com, hoho');

      checkContactsValid('test@example.com, test2@example.com');
      checkContactsValid('test@example.com, test2@example.com, ');
      checkContactsValid('test@example.com , test2@example.com , ');
    });
  });

  it('check submitting without message', function (): void {
    cy.mountWithPlugins(SingleDataRequestComponent, {
      keycloak: minimalKeycloakMock({}),
    }).then(() => {
      fillMandatoryFields();

      cy.intercept('**/single', {
        statusCode: 200,
        times: 1,
      });

      cy.get("button[type='submit']").should('exist').click();
      cy.get("[data-test='requestStatusText']").should('contain.text', 'Submitting your data request was successful.');
    });
  });

  it('check submitting unsuccessfully', function (): void {
    cy.mountWithPlugins(SingleDataRequestComponent, {
      keycloak: minimalKeycloakMock({}),
    }).then(() => {
      fillMandatoryFields();

      cy.intercept('**/single', {
        statusCode: 404,
        times: 1,
      });

      cy.get("button[type='submit']").should('exist').click();
      cy.get("[data-test='requestStatusText']").should(
        'contain.text',
        'The submission of your data request was unsuccessful.'
      );
    });
  });

  it('check quota surpassed modal opens', function (): void {
    cy.mountWithPlugins(SingleDataRequestComponent, {
      keycloak: minimalKeycloakMock({}),
    }).then(() => {
      fillMandatoryFields();

      cy.intercept('**/single', {
        statusCode: 403,
        times: 1,
      });

      cy.get("button[type='submit']").should('exist').click();

      cy.get("[data-test='quotaReachedModal']").should('be.visible');
      cy.get("[data-test='closeMaxRequestsReachedModalButton']").should('be.visible').click();
      cy.get("[data-test='quotaReachedModal']").should('not.exist');
    });
  });

  /**
   * Fills the mandatory fields on the single data request page
   */
  function fillMandatoryFields(): void {
    singleDataRequestPage.chooseReportingPeriod('2023');
    singleDataRequestPage.chooseFrameworkLksg();
  }

  /**
   * verify that the messagebox is disabled and that the accept terms checkbox is not visible (as the contacts are
   * not valid)
   */
  function verifyMessageboxAndCheckboxNotAccessible(): void {
    cy.get("[data-test='dataRequesterMessage']").should('be.disabled');
    cy.get("input[data-test='acceptConditionsCheckbox']").should('not.be.visible');
  }

  /**
   * verify that the messagebox is enabled and that the accept terms checkbox is visible (as the contacts are valid)
   */
  function verifyMessageboxAndCheckboxAccessible(): void {
    cy.get("[data-test='dataRequesterMessage']").should('be.enabled');
    cy.get("input[data-test='acceptConditionsCheckbox']").should('be.visible');
  }

  /**
   * deletes the previously typed contacts and types new contacts given as the argument
   * @param contacts the contacts to write as a sting
   */
  function typeContacts(contacts: string): void {
    cy.get("[data-test='contactEmail']").should('exist').clear();
    cy.get("[data-test='contactEmail']").should('exist').type(contacts);
  }

  /**
   * checks that the provided contacts are not valid
   * @param contacts the contacts to write as a sting
   */
  function checkContactsNotValid(contacts: string): void {
    typeContacts(contacts);
    verifyMessageboxAndCheckboxNotAccessible();
  }

  /**
   * checks that the provided contacts are valid
   * @param contacts the contacts to write as a sting
   */
  function checkContactsValid(contacts: string): void {
    typeContacts(contacts);
    verifyMessageboxAndCheckboxAccessible();
  }
});
