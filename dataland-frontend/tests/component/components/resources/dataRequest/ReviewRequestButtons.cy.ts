// @ts-nocheck
import ReviewRequestButtonsComponent from '@/components/resources/dataRequest/ReviewRequestButtons.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { type DataMetaInformation, DataTypeEnum } from '@clients/backend';
import { RequestStatus, type StoredDataRequest } from '@clients/communitymanager';
import { checkEmailFieldsAndCheckBox } from '@ct/testUtils/EmailDetails';
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';

describe('Component tests for the data request review buttons', function (): void {
  const mockCompanyId: string = 'Mock-Company-Id';
  const parentComponentOfEmailDetails = 'updateRequestModal';
  const triggerComponentForEmailDetails = 'updateRequestButton';
  const messageHistory = [
    {
      contacts: ['Franz69@example.com'],
      message: 'navigate online bandwidth',
      creationTimestamp: 2710,
    },
  ];
  let mockedRequests: StoredDataRequest[];
  before(() => {
    cy.fixture('DataRequestsMock').then((jsonContent) => {
      mockedRequests = jsonContent as Array<StoredDataRequest>;
    });
  });

  it('Check review functionality', function () {
    interceptUserRequestsOnMounted();
    interceptPatchRequestsOnMounted();

    const mockMapOfReportingPeriodToActiveDataset = new Map<string, DataMetaInformation>([
      ['2022', {} as DataMetaInformation],
    ]);
    mountReviewRequestButtonsWithProps(mockCompanyId, DataTypeEnum.Lksg, mockMapOfReportingPeriodToActiveDataset);
    checkForReviewButtonsPopUpModal('successText');
  });

  it('Check review functionality with error message', function () {
    interceptUserRequestsOnMounted();
    const mockMapOfReportingPeriodToActiveDataset = new Map<string, DataMetaInformation>([
      ['2022', {} as DataMetaInformation],
    ]);
    mountReviewRequestButtonsWithProps(mockCompanyId, DataTypeEnum.Lksg, mockMapOfReportingPeriodToActiveDataset);
    checkForReviewButtonsPopUpModal('noSuccessText');
  });

  it('Check review functionality with multiple reporting periods', function () {
    interceptUserRequestsOnMounted();
    interceptPatchRequestsOnMounted();

    const mockMapOfReportingPeriodToActiveDataset = new Map<string, DataMetaInformation>([
      ['2020', {} as DataMetaInformation],
      ['2021', {} as DataMetaInformation],
      ['2022', {} as DataMetaInformation],
    ]);
    mountReviewRequestButtonsWithProps(mockCompanyId, DataTypeEnum.Lksg, mockMapOfReportingPeriodToActiveDataset);

    checkForReviewButtonsAndClickOnDropDownReportingPeriod('resolveRequestButton', 'reOpenRequestButton');

    checkForReviewButtonsAndClickOnDropDownReportingPeriod('reOpenRequestButton', 'resolveRequestButton');
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
   * Checks dropdown functionality of request review button
   * @param buttonToClick desired dialog
   * @param buttonNotToClick if false, display error message
   */
  function checkForReviewButtonsAndClickOnDropDownReportingPeriod(
    buttonToClick: string,
    buttonNotToClick: string
  ): void {
    cy.get(`[data-test="${buttonNotToClick}"]`).should('exist');
    cy.get(`[data-test="${buttonToClick}"]`).should('exist').click();

    cy.get('[data-test="reporting-periods"] a').contains('2024').should('not.exist');
    cy.get('[data-test="reporting-periods"] a').contains('2020').should('not.have.class', 'link');
    cy.get('[data-test="reporting-periods"] a').contains('2021').should('not.have.class', 'link');
    cy.get('[data-test="reporting-periods"] a').contains('2022').should('have.class', 'link').click();
    if (buttonToClick == 'reOpenRequestButton') checkTheUpdateRequestModal();
    cy.get('button[aria-label="CLOSE"]').should('be.visible').click();
  }
  /**
   * Mocks the community-manager answer for the request of the users data requests
   */
  function interceptUserRequestsOnMounted(): void {
    const requestFor2022 = mockedRequests.find(
      (it) => it.reportingPeriod == '2022' && it.datalandCompanyId == 'Mock-Company-Id'
    );
    assert(requestFor2022 !== undefined);

    cy.intercept('GET', `**/community/requests/${requestFor2022!.dataRequestId}`, {
      body: {
        messageHistory: messageHistory,
      },
      status: 200,
    }).as('fetchSingleDataRequests');
    cy.intercept('GET', `**/community/requests/user`, {
      body: mockedRequests,
    }).as('fetchUserRequests');
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
  function mountReviewRequestButtonsWithProps(
    companyId: string,
    framework: DataTypeEnum,
    map: Map<string, DataMetaInformation>
  ): void {
    cy.mountWithPlugins(ReviewRequestButtonsComponent, {
      keycloak: minimalKeycloakMock({}),

      // @ts-ignore
      props: {
        companyId: companyId,
        framework: framework,
        mapOfReportingPeriodToActiveDataset: map,
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
