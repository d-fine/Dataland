import ViewDataRequestPage from '@/components/pages/ViewDataRequestPage.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import {
  AccessStatus,
  RequestPriority,
  RequestStatus,
  type StoredDataRequest,
  type StoredDataRequestMessageObject,
} from '@clients/communitymanager';
import type { CompanyInformation } from '@clients/backend';
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { checkEmailFieldsAndCheckBox } from '@ct/testUtils/EmailDetails';
import { getMountingFunction } from '@ct/testUtils/Mount';
import router from '@/router';
import { getRequestStatusLabel } from '@/utils/RequestUtils.ts';
import { KEYCLOAK_ROLE_ADMIN } from '@/utils/KeycloakRoles';

describe('Component tests for the view data request page', function (): void {
  const requestId = 'dummyRequestId';
  const dummyUserId = 'dummyUserId';
  const dummyEmail = 'dummy@mail.de';
  const emailOnUpdate = true;
  const dummyCompanyId = 'dummyCompanyId';
  const dummyCompanyName = 'dummyCompanyName';
  const dummyFramework = 'dummyFramework';
  const dummyReportingYear = 'dummyReportingYear';
  const dummyLastModifiedDate = 1709204495770;
  const dummyCreationTime = 1709104495770;
  const dummyMessageObject: StoredDataRequestMessageObject = {
    contacts: new Set<string>(['test@example.com', 'test2@example.com']),
    message: 'test message',
    creationTimestamp: dummyCreationTime,
  };
  /**
   * Return a stored data request
   * @param requestStatus the request status
   * @param messageHistory the message history
   * @returns stored data request
   */
  function createStoredDataRequest(
    requestStatus: RequestStatus,
    messageHistory: Array<StoredDataRequestMessageObject>
  ): StoredDataRequest {
    return {
      dataRequestId: requestId,
      userId: dummyUserId,
      userEmailAddress: dummyEmail,
      emailOnUpdate: emailOnUpdate,
      creationTimestamp: dummyCreationTime,
      dataType: dummyFramework,
      reportingPeriod: dummyReportingYear,
      datalandCompanyId: dummyCompanyId,
      messageHistory: messageHistory,
      dataRequestStatusHistory: [
        { status: requestStatus, creationTimestamp: dummyCreationTime, accessStatus: AccessStatus.Public },
      ],
      lastModifiedDate: dummyLastModifiedDate,
      requestStatus: requestStatus,
      accessStatus: AccessStatus.Public,
      requestPriority: RequestPriority.Low,
    };
  }
  /**
   * Mocks the community-manager answer for single data request of the users
   * @param request the request to mock
   */
  function interceptUserAskForSingleDataRequestsOnMounted(request: StoredDataRequest): void {
    cy.intercept(`**/community/requests/${requestId}`, {
      body: request,
      status: 200,
    });
  }
  /**
   * Mocks the api-manager answer for basic company information
   */
  function interceptUserActiveDatasetOnMounted(hasActiveDataSet: boolean): void {
    const dummyMetaData = {
      companyId: dummyCompanyId,
      dataType: dummyFramework,
    };
    cy.intercept(`**/api/metadata?**`, {
      body: hasActiveDataSet ? [dummyMetaData] : [],
      status: 200,
    });
  }
  /**
   * Mocks the api-manager answer for basic company information
   */
  function interceptUserAskForCompanyNameOnMounted(): void {
    const mockCompanyInfo: CompanyInformation = {
      companyName: dummyCompanyName,
      headquarters: 'Berlin',
      identifiers: {},
      countryCode: 'IT',
    };
    cy.intercept(`**/companies/dummyCompanyId/info`, {
      body: mockCompanyInfo,
      status: 200,
    });
  }
  /**
   * Mocks the community-manager answer for patching a data request
   */
  function interceptPatchRequest(): void {
    cy.intercept(`**/community/requests/${requestId}/requestStatus?**`, {
      status: 200,
    });
  }

  /**
   * Checks the existence of basic elements of the page
   * @param requestStatus the request Status to check for
   */
  function checkBasicPageElementsAsUser(requestStatus: RequestStatus): void {
    cy.contains('Data Request').should('exist');
    cy.contains('Request Details').should('exist').should('have.class', 'card__title');
    cy.contains('Provided Contact Details and Messages').should('exist').should('have.class', 'card__title');
    cy.contains('Request is').should('exist').should('have.class', 'card__title');
    cy.get('[data-test="status_history_toggle"]').should('exist');

    cy.get('[data-test="card_requestDetails"]').should('exist');
    cy.get('[data-test="card_requestDetails"]').within(() => {
      cy.contains('Company').should('exist');
      cy.contains(`${dummyCompanyName}`).should('exist');
      cy.contains('Framework').should('exist');
      cy.contains(`${humanizeStringOrNumber(dummyFramework)}`).should('exist');
      cy.contains('Reporting year').should('exist');
      cy.contains(`${dummyReportingYear}`).should('exist');
    });
    cy.get('[data-test="card_requestIs"]').should('exist');
    cy.get('[data-test="card_requestIs"]').within(() => {
      cy.contains(`${getRequestStatusLabel(requestStatus)}`).should('exist');
      cy.contains(`${convertUnixTimeInMsToDateString(dummyLastModifiedDate)}`).should('exist');
    });
  }

  /**
   * Checks the existence of basic elements of the page when admin is visiting
   * @param requestStatus the request Status to check for
   */
  function checkBasicPageElementsAsAdmin(requestStatus: RequestStatus): void {
    cy.contains('Data Request').should('exist');
    cy.contains('Request Details').should('exist').should('have.class', 'card__title');
    cy.contains('Provided Contact Details and Messages').should('not.exist');
    cy.contains('Request is').should('exist').should('have.class', 'card__title');
    cy.get('[data-test="status_history_toggle"]').should('exist');

    cy.get('[data-test="card_requestDetails"]').should('exist');
    cy.get('[data-test="card_requestDetails"]').within(() => {
      cy.contains('Requester').should('exist');
      cy.contains(`${dummyEmail}`).should('exist');
      cy.contains('Company').should('exist');
      cy.contains(`${dummyCompanyName}`).should('exist');
      cy.contains('Framework').should('exist');
      cy.contains(`${humanizeStringOrNumber(dummyFramework)}`).should('exist');
      cy.contains('Reporting year').should('exist');
      cy.contains(`${dummyReportingYear}`).should('exist');
    });
    cy.get('[data-test="card_requestIs"]').should('exist');
    cy.get('[data-test="card_requestIs"]').within(() => {
      cy.contains(`${requestStatus}`).should('exist');
      cy.contains(`${convertUnixTimeInMsToDateString(dummyLastModifiedDate)}`).should('exist');
    });
  }

  it('Check view data request page for resolved request with data renders as expected', function () {
    interceptUserAskForSingleDataRequestsOnMounted(
      createStoredDataRequest(RequestStatus.Resolved, [dummyMessageObject])
    );
    interceptUserAskForCompanyNameOnMounted();
    interceptUserActiveDatasetOnMounted(true);
    interceptPatchRequest();
    cy.spy(router, 'push').as('routerPush');
    getMountingFunction({ keycloak: minimalKeycloakMock({ userId: dummyUserId }), router: router })(
      ViewDataRequestPage,
      {
        props: {
          requestId: requestId,
        },
      }
    ).then(() => {
      checkBasicPageElementsAsUser(RequestStatus.Resolved);
      cy.get('[data-test="newMessage"]').should('exist').should('not.be.visible');
      cy.get('[data-test="card_withdrawn"]').should('exist').should('not.be.visible');
      cy.get('[data-test="resolveRequestButton"]').should('not.exist');

      cy.get('[data-test="viewDataset"]').should('exist').click();
      cy.get('@routerPush').should('have.been.calledWith', `/companies/${dummyCompanyId}/frameworks/${dummyFramework}`);
    });
  });

  it('Check view data request page for withdrawn request without data renders as expected', function () {
    interceptUserAskForSingleDataRequestsOnMounted(createStoredDataRequest(RequestStatus.Withdrawn, []));
    interceptUserAskForCompanyNameOnMounted();
    interceptUserActiveDatasetOnMounted(false);
    interceptPatchRequest();
    getMountingFunction({ keycloak: minimalKeycloakMock({ userId: dummyUserId }) })(ViewDataRequestPage, {
      props: {
        requestId: requestId,
      },
    });
    checkBasicPageElementsAsUser(RequestStatus.Withdrawn);
    cy.get('[data-test="newMessage"]').should('exist').should('not.be.visible');
    cy.get('[data-test="card_withdrawn"]').should('exist').should('not.be.visible');
    cy.get('[data-test="resolveRequestButton"]').should('not.exist');
    cy.get('[data-test="viewDataset"]').should('exist').should('not.be.visible');
  });

  it('Check viewDataRequest page for nonSourceable request renders as expected and reopen data request', function () {
    interceptUserAskForSingleDataRequestsOnMounted(
      createStoredDataRequest(RequestStatus.NonSourceable, [dummyMessageObject])
    );
    interceptUserAskForCompanyNameOnMounted();
    interceptUserActiveDatasetOnMounted(false);
    interceptPatchRequest();
    getMountingFunction({ keycloak: minimalKeycloakMock({ userId: dummyUserId }) })(ViewDataRequestPage, {
      props: {
        requestId: requestId,
      },
    });
    checkBasicPageElementsAsUser(RequestStatus.NonSourceable);
    cy.get('[data-test="newMessage"]').should('exist').should('not.be.visible');
    cy.get('[data-test="viewDataset"]').should('exist').should('not.be.visible');
    cy.get('[data-test="card_withdrawn"]').should('exist').should('be.visible');
    cy.get('[data-test="card_reopen"]').should('exist');
    cy.get('[data-test="card_reopen"]').within(() => {
      cy.contains('Currently, your request has the status non-sourceable.').should('exist');
      cy.contains('Reopen Request').should('exist');
      cy.contains('Reopen request').click();
    });
    cy.get('[data-test="reopenModal"]').should('exist').should('be.visible').contains('REOPEN REQUEST').click();
    cy.get('[data-test="reopenModal"]').should('exist').should('be.visible');
    cy.get('[data-test="reopenModal"]').should('exist');
    cy.get('[data-test="reopenModal"]').within(() => {
      cy.get('[data-test="reopenMessage"]').should('be.visible');
      cy.get('[data-test="reopenMessage"]').type('Make the test work, please!');
      cy.get('[data-test="reopenButton"]').should('be.visible');
      cy.get('[data-test="reopenButton"]').contains('REOPEN REQUEST').click();
    });
    cy.get('[data-test="reopenModal"]').should('not.exist');
    cy.get('[data-test="reopenedModal"]').should('exist').should('be.visible').contains('CLOSE').click();
    cy.get('[data-test="reopenedModal"]').should('not.exist');
  });

  it('Check view data request page for open request without data and withdraw the data request', function () {
    interceptUserAskForSingleDataRequestsOnMounted(createStoredDataRequest(RequestStatus.Open, []));
    interceptUserAskForCompanyNameOnMounted();
    interceptUserActiveDatasetOnMounted(false);
    interceptPatchRequest();
    getMountingFunction({ keycloak: minimalKeycloakMock({ userId: dummyUserId }) })(ViewDataRequestPage, {
      props: { requestId: requestId },
    });
    checkBasicPageElementsAsUser(RequestStatus.Open);
    cy.get('[data-test="card_providedContactDetails"]').should('exist').get('[data-test="newMessage"]').should('exist');

    cy.get('[data-test="resolveRequestButton"]').should('not.exist');
    cy.get('[data-test="viewDataset"]').should('exist').should('not.be.visible');
    cy.get('[data-test="card_withdrawn"]').should('exist');
    cy.get('[data-test="card_withdrawn"]').within(() => {
      cy.contains(
        'Once a data request is withdrawn, it will be removed from your data request list.' +
          ' The company owner will not be notified anymore.'
      ).should('exist');
      cy.contains('Withdraw Request').should('exist');
      cy.contains('Withdraw request').click();
    });
    cy.get('[data-test="successModal"]').should('exist').should('be.visible').contains('CLOSE').click();
    cy.get('[data-test="successModal"]').should('not.exist');
  });

  it('Check view data request page for open request with data and check the routing to data view page', function () {
    interceptUserAskForSingleDataRequestsOnMounted(createStoredDataRequest(RequestStatus.Open, []));
    interceptUserAskForCompanyNameOnMounted();
    interceptUserActiveDatasetOnMounted(true);
    interceptPatchRequest();
    cy.spy(router, 'push').as('routerPush');
    getMountingFunction({ keycloak: minimalKeycloakMock({ userId: dummyUserId }), router: router })(
      ViewDataRequestPage,
      {
        props: { requestId: requestId },
      }
    ).then(() => {
      checkBasicPageElementsAsUser(RequestStatus.Open);
      cy.get('[data-test="viewDataset"]').should('exist').click();
      cy.get('@routerPush').should('have.been.calledWith', `/companies/${dummyCompanyId}/frameworks/${dummyFramework}`);
    });
  });

  it(
    'Check view data request page for answered request and ' + 'check resolve and reopen buttons vanish on resolve',
    function () {
      const dummyRequest = createStoredDataRequest(RequestStatus.Answered, []);
      interceptUserAskForSingleDataRequestsOnMounted(dummyRequest);
      interceptUserAskForCompanyNameOnMounted();
      interceptUserActiveDatasetOnMounted(true);
      interceptPatchRequest();
      getMountingFunction({ keycloak: minimalKeycloakMock({ userId: dummyUserId }), router })(ViewDataRequestPage, {
        props: { requestId: requestId },
      }).then(() => {
        checkBasicPageElementsAsUser(dummyRequest.requestStatus);
        cy.get('[data-test="resolveRequestButton"]').should('exist').click();
        cy.get('[data-test="successText"]').should('exist');
        interceptUserAskForSingleDataRequestsOnMounted(createStoredDataRequest(RequestStatus.Resolved, []));
        cy.get('button[aria-label="CLOSE"]').should('be.visible').click();
        cy.get('[data-test="resolveRequestButton"]').should('not.exist');
      });
    }
  );

  it(
    'Check view data request page for answered request and ' + 'check resolve and reopen buttons vanish on reopen',
    function () {
      const dummyRequest = createStoredDataRequest(RequestStatus.Answered, []);
      interceptUserAskForSingleDataRequestsOnMounted(dummyRequest);
      interceptUserAskForCompanyNameOnMounted();
      interceptUserActiveDatasetOnMounted(true);
      interceptPatchRequest();
      getMountingFunction({ keycloak: minimalKeycloakMock({ userId: dummyUserId }), router })(ViewDataRequestPage, {
        props: { requestId: requestId },
      }).then(() => {
        checkBasicPageElementsAsUser(dummyRequest.requestStatus);
        cy.get('[data-test="reOpenRequestButton"]').should('exist').click();
        cy.get('[data-test="updateRequestButton"]').should('exist').click({ force: true });
        cy.get('[data-test="successText"]').should('exist');
        interceptUserAskForSingleDataRequestsOnMounted(createStoredDataRequest(RequestStatus.Open, []));
        cy.get('button[aria-label="CLOSE"]').should('be.visible').click();
        cy.get('[data-test="reOpenRequestButton"]').should('not.exist');
      });
    }
  );

  it(
    'Check view data request page for open request and check that the message history is displayed ' +
      'and that a user can add a new message',
    function () {
      interceptUserAskForSingleDataRequestsOnMounted(createStoredDataRequest(RequestStatus.Open, [dummyMessageObject]));
      interceptUserAskForCompanyNameOnMounted();
      interceptUserActiveDatasetOnMounted(true);
      interceptPatchRequest();
      getMountingFunction({ keycloak: minimalKeycloakMock({ userId: dummyUserId }) })(ViewDataRequestPage, {
        props: { requestId: requestId },
      });
      checkBasicPageElementsAsUser(RequestStatus.Open);
      cy.get('[data-test="newMessage"]').should('exist').click();
      checkEmailFieldsAndCheckBox('newMessageModal', 'addMessageButton');
    }
  );

  it('Check view data request page for answered request as admin with data renders as expected', function () {
    interceptUserAskForSingleDataRequestsOnMounted(
      createStoredDataRequest(RequestStatus.Answered, [dummyMessageObject])
    );
    interceptUserAskForCompanyNameOnMounted();
    interceptUserActiveDatasetOnMounted(true);
    interceptPatchRequest();
    getMountingFunction({
      keycloak: minimalKeycloakMock({
        authenticated: true,
        roles: [KEYCLOAK_ROLE_ADMIN],
        userId: crypto.randomUUID(),
      }),
    })(ViewDataRequestPage, {
      props: { requestId: requestId },
    });

    checkBasicPageElementsAsAdmin(RequestStatus.Answered);
    cy.get('[data-test="resolveRequestButton"]').should('not.exist');
  });

  it('Check email notification toggle', () => {
    interceptUserAskForSingleDataRequestsOnMounted(createStoredDataRequest(RequestStatus.Open, []));
    getMountingFunction({ keycloak: minimalKeycloakMock({ userId: dummyUserId }) })(ViewDataRequestPage, {
      props: { requestId: requestId },
    });
    cy.intercept('PATCH', `**/community/requests/${requestId}`, {
      status: 200,
    }).as('patchRequest');
    cy.get('[data-test="emailOnUpdate"] [data-test="emailOnUpdateInput"]').scrollIntoView();
    cy.get('[data-test="emailOnUpdate"] [data-test="emailOnUpdateInput"]').should('be.visible');
    cy.get('[data-test="emailOnUpdate"] [data-test="emailOnUpdateInput"]').click();
    cy.wait('@patchRequest').its('request.body.emailOnUpdate').should('be.equal', true);
    cy.get('[data-test="emailOnUpdate"] [data-test="emailOnUpdateInput"]').click();
    cy.wait('@patchRequest').its('request.body.emailOnUpdate').should('be.equal', false);
  });
});
