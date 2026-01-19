import SingleDataRequestComponent from '@/components/pages/SingleDataRequest.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { singleDataRequestPage } from '@sharedUtils/components/SingleDataRequest';

/**
 * Fills the mandatory fields on the single data request page
 */
function fillMandatoryFields(): void {
  singleDataRequestPage.chooseReportingPeriod('2023');
  singleDataRequestPage.chooseFrameworkLksg();
}

/**
 * Asserts that the given reporting period is either visible or not present depending on shouldExist
 * @param reportingPeriod
 * @param shouldExist
 */
function assertWhetherReportingPeriodExists(reportingPeriod: string, shouldExist: boolean): void {
  const chainer = shouldExist ? 'be.visible' : 'not.exist';
  cy.get("[data-test='toggle-chip']").contains(reportingPeriod).should(chainer);
}

describe('Component tests for the single data request page', function (): void {
  it('check submitting successfully', function (): void {
    cy.mountWithPlugins(SingleDataRequestComponent, {
      keycloak: minimalKeycloakMock({}),
    }).then(() => {
      fillMandatoryFields();

      cy.intercept('**/data-sourcing/requests', {
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

      cy.intercept('**/data-sourcing/requests', {
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

      cy.intercept('**/data-sourcing/requests', {
        statusCode: 403,
        times: 1,
      });

      cy.get("button[type='submit']").should('exist').click();

      cy.get("[data-test='quotaReachedModal']").should('be.visible');
      cy.get("[data-test='closeMaxRequestsReachedModalButton']").should('be.visible').click();
      cy.get("[data-test='quotaReachedModal']").should('not.exist');
    });
  });

  it('check available reporting periods', function (): void {
    cy.mountWithPlugins(SingleDataRequestComponent, {
      keycloak: minimalKeycloakMock({}),
    }).then(() => {
      assertWhetherReportingPeriodExists('2026', false);
      assertWhetherReportingPeriodExists('2025', true);
      assertWhetherReportingPeriodExists('2024', true);
      assertWhetherReportingPeriodExists('2023', true);
      assertWhetherReportingPeriodExists('2022', true);
      assertWhetherReportingPeriodExists('2021', true);
      assertWhetherReportingPeriodExists('2020', true);
      assertWhetherReportingPeriodExists('2019', false);
    });
  });
});
