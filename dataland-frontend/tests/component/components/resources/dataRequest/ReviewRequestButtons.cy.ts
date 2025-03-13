// @ts-nocheck
import ReviewRequestButtonsComponent from '@/components/resources/dataRequest/ReviewRequestButtons.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { RequestStatus, type StoredDataRequest } from '@clients/communitymanager';
import { checkEmailFieldsAndCheckBox } from '@ct/testUtils/EmailDetails';
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';

describe('Component tests for the data request review buttons', function (): void {
  const mockDataRequestId: string = 'Mock-DataRequest-Id';
  const parentComponentOfEmailDetails = 'updateRequestModal';
  const triggerComponentForEmailDetails = 'updateRequestButton';
  const messageHistory = [
    {
      contacts: ['Franz69@example.com'],
      message: 'navigate online bandwidth',
      creationTimestamp: 2710,
    },
  ];

  it('Check review functionality', function () {
    interceptUserRequestsOnMounted();
    interceptPatchRequestsOnMounted();

    mountReviewRequestButtons();
    checkForReviewButtonsPopUpModal('successText');
  });

  it('Check review functionality with error message', function () {
    interceptUserRequestsOnMounted();

    mountReviewRequestButtons();
    checkForReviewButtonsPopUpModal('noSuccessText');
  });

  /**
   * Checks for pop up modal
   * @param expectedPopUp expected pop up dialog
   */
  function checkForReviewButtonsPopUpModal(expectedPopUp: string): void {
    const popUpdataTestId = `[data-test="${expectedPopUp}"]`;
    cy.get('[data-test="resolveRequestButton"]').should('exist').click();
    cy.get(popUpdataTestId).should('exist');
    cy.get('button[aria-label="CLOSE"]').should('be.visible').click();

    cy.get('[data-test="reOpenRequestButton"]').should('exist').click();
    checkTheUpdateRequestModal();
    cy.get(popUpdataTestId).should('exist');
    cy.get('button[aria-label="CLOSE"]').should('be.visible').click();
  }

  /**
   * Mocks the community-manager answer for the request of the users data requests
   */
  function interceptUserRequestsOnMounted(): void {
    cy.intercept('GET', `**/community/requests/${mockDataRequestId}`, {
      body: {
        messageHistory: messageHistory,
      },
      status: 200,
    }).as('fetchSingleDataRequests');
  }
  /**
   * Mocks the answer for patching the request status
   */
  function interceptPatchRequestsOnMounted(): void {
    cy.intercept('PATCH', '**/community/requests/*', (request) => {
      if (request.body.contacts) {
        assert(request.body.contacts.length);
      }
      if (request.body.message) {
        assert(request.body.message.length);
      }
      if (request.body.requestStatus === 'Resolved') {
        request.alias = 'closeUserRequest';
        request.reply({
          body: {
            requestStatus: RequestStatus.Resolved,
          } as StoredDataRequest,
          status: 200,
        });
      } else if (request.body.requestStatus === 'Open') {
        request.alias = 'reOpenUserRequest';
        request.reply({
          body: {
            requestStatus: RequestStatus.Open,
          } as StoredDataRequest,
          status: 200,
        });
      }
    });
  }
  /**
   * Mount review request button component with given props
   * @param companyId companyId
   * @param framework framework
   * @param map mapOfReportingPeriodToActiveDataset
   */
  function mountReviewRequestButtons(): void {
    cy.mountWithPlugins(ReviewRequestButtonsComponent, {
      keycloak: minimalKeycloakMock({}),

      // @ts-ignore
      props: {
        dataRequestId: mockDataRequestId,
      },
    });
  }
  /**
   * Checks the update request modal tab menu and message history tab
   */
  function checkTheUpdateRequestModal(): void {
    cy.get('[data-test="updateRequestTabMenu"]')
      .should('exist')
      .within(() => {
        cy.contains('button', 'UPDATE REQUEST').should('exist');
        cy.contains('button', 'VIEW HISTORY').should('exist').click({ force: true });
      });
    cy.get('[data-test="viewHistoryModal"]')
      .should('exist')
      .should('be.visible')
      .within(() => {
        checkMessageHistory();
      });
    cy.get('[data-test="updateRequestTabMenu"]')
      .contains('button', 'UPDATE REQUEST')
      .should('exist')
      .click({ force: true });
    checkEmailFieldsAndCheckBox(parentComponentOfEmailDetails, triggerComponentForEmailDetails);
  }
  /**
   * Checks the message history
   */
  function checkMessageHistory(): void {
    messageHistory.forEach((message) => {
      message.contacts.forEach((contact) => {
        cy.contains(contact).should('exist');
      });
      cy.contains(message.message).should('exist');
      cy.contains(convertUnixTimeInMsToDateString(message.creationTimestamp)).should('exist');
    });
  }
});
