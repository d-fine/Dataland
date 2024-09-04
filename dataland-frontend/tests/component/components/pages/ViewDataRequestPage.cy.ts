// @ts-nocheck
import ViewDataRequestPage from '@/components/pages/ViewDataRequestPage.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { RequestStatus, type StoredDataRequest, type StoredDataRequestMessageObject } from '@clients/communitymanager';
import type { BasicCompanyInformation, DataMetaInformation } from '@clients/backend';
import { QaStatus } from '@clients/backend';
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { checkEmailFieldsAndCheckBox } from '@ct/testUtils/EmailDetails';

describe('Component tests for the view data request page', function (): void {
  const requestId = 'dummyRequestId';
  const dummyUserId = 'dummyUserId';
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
      userEmailAddress: 'dummy@mail.de',
      creationTimestamp: dummyCreationTime,
      dataType: dummyFramework,
      reportingPeriod: dummyReportingYear,
      datalandCompanyId: dummyCompanyId,
      messageHistory: messageHistory,
      dataRequestStatusHistory: [],
      lastModifiedDate: dummyLastModifiedDate,
      requestStatus: requestStatus,
      accessStatus: AccessStatus.Public,
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
    }).as('fetchSingleDataRequests');
  }
  /**
   * Mocks the api-manager answer for basic company information
   * @param qaStatus the desired active dataset
   */
  function interceptUserActiveDatasetOnMounted(qaStatus: QaStatus): void {
    cy.intercept(`**/api/metadata?**`, {
      body: [{ qaStatus: qaStatus }],
      status: 200,
    }).as('fetchActiveDatasets');
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
    }).as('fetchCompanyName');
  }
  /**
   * Mocks the community-manager answer for patching a data request
   */
  function interceptPatchRequest(): void {
    cy.intercept(`**/community/requests/${requestId}/requestStatus?**`, {
      status: 200,
    }).as('fetchCompanyName');
  }

  it('Check view data request page for resolved request with data renders as expected', function () {
    interceptUserAskForSingleDataRequestsOnMounted(
      createStoredDataRequest(RequestStatus.Resolved, [dummyMessageObject])
    );
    interceptUserAskForCompanyNameOnMounted();
    interceptUserActiveDatasetOnMounted(QaStatus.Accepted);
    interceptPatchRequest();
    cy.mountWithPlugins(ViewDataRequestPage, {
      keycloak: minimalKeycloakMock({}),
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      props: {
        requestId: requestId,
      },
    }).then((mounted) => {
      checkBasicPageElements(RequestStatus.Resolved);
      cy.get('[data-test="newMessage"]').should('exist').should('not.be.visible');
      cy.get('[data-test="card_withdrawn"]').should('exist').should('not.be.visible');
      cy.get('[data-test="resolveRequestButton"]').should('exist').should('not.be.visible');

      cy.get('[data-test="viewDataset"]').should('exist').click();
      cy.wrap(mounted.component)
        .its('$route.path')
        .should('eq', `/companies/${dummyCompanyId}/frameworks/${dummyFramework}`);
    });
  });
  it('Check view data request page for withdrawn request without data renders as expected', function () {
    interceptUserAskForSingleDataRequestsOnMounted(createStoredDataRequest(RequestStatus.Withdrawn, []));
    interceptUserAskForCompanyNameOnMounted();
    interceptUserActiveDatasetOnMounted(QaStatus.Rejected);
    interceptPatchRequest();
    cy.mountWithPlugins(ViewDataRequestPage, {
      keycloak: minimalKeycloakMock({}),
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      props: {
        requestId: requestId,
      },
    }).then(() => {
      checkBasicPageElements(RequestStatus.Withdrawn);
      cy.get('[data-test="newMessage"]').should('exist').should('not.be.visible');
      cy.get('[data-test="card_withdrawn"]').should('exist').should('not.be.visible');
      cy.get('[data-test="resolveRequestButton"]').should('exist').should('not.be.visible');
      cy.get('[data-test="viewDataset"]').should('exist').should('not.be.visible');
    });
  });
  /**
   * Checks the existence of basic elements of the page
   * @param requestStatus the request Status to check for
   */
  function checkBasicPageElements(requestStatus: RequestStatus): void {
    cy.contains('Data Request').should('exist');
    cy.contains('Request Details').should('exist').should('have.class', 'card__title');
    cy.contains('Provided Contact Details & Messages').should('exist').should('have.class', 'card__title');
    cy.contains('Request is').should('exist').should('have.class', 'card__title');
    cy.get('[data-test="status_history_toggle"]').should('exist');

    cy.get('[data-test="card_requestDetails"]')
      .should('exist')
      .within(() => {
        cy.contains('Company').should('exist');
        cy.contains(`${dummyCompanyName}`).should('exist');
        cy.contains('Framework').should('exist');
        cy.contains(`${humanizeStringOrNumber(dummyFramework)}`).should('exist');
        cy.contains('Reporting year').should('exist');
        cy.contains(`${dummyReportingYear}`).should('exist');
      });
    cy.get('[data-test="card_requestIs"]')
      .should('exist')
      .within(() => {
        cy.contains(`${requestStatus}`).should('exist');
        cy.contains(`${convertUnixTimeInMsToDateString(dummyLastModifiedDate)}`).should('exist');
      });
  }
  it('Check view data request page for open request without data and withdraw the data request', function () {
    interceptUserAskForSingleDataRequestsOnMounted(createStoredDataRequest(RequestStatus.Open, []));
    interceptUserAskForCompanyNameOnMounted();
    interceptUserActiveDatasetOnMounted(QaStatus.Pending);
    interceptPatchRequest();
    cy.mountWithPlugins(ViewDataRequestPage, {
      keycloak: minimalKeycloakMock({}),
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      props: {
        requestId: requestId,
      },
    }).then(() => {
      checkBasicPageElements(RequestStatus.Open);
      cy.get('[data-test="card_providedContactDetails"]')
        .should('exist')
        .get('[data-test="newMessage"]')
        .should('exist');

      cy.get('[data-test="resolveRequestButton"]').should('exist').should('not.be.visible');
      cy.get('[data-test="viewDataset"]').should('exist').should('not.be.visible');
      cy.get('[data-test="card_withdrawn"]')
        .should('exist')
        .within(() => {
          cy.contains(
            'Once a data request is withdrawn, it will be removed from your data request list.' +
              ' The company owner will not be notified anymore.'
          ).should('exist');
          cy.contains('Withdraw Request').should('exist');
          cy.contains('Withdraw request.').should('exist').click();
        });
      cy.get('[data-test="successModal"]').should('exist').should('be.visible').contains('CLOSE').click();
      cy.get('[data-test="successModal"]').should('not.exist');
    });
  });
  it('Check view data request page for open request with data and check the routing to data view page', function () {
    interceptUserAskForSingleDataRequestsOnMounted(createStoredDataRequest(RequestStatus.Open, []));
    interceptUserAskForCompanyNameOnMounted();
    interceptUserActiveDatasetOnMounted(QaStatus.Accepted);
    interceptPatchRequest();
    cy.mountWithPlugins(ViewDataRequestPage, {
      keycloak: minimalKeycloakMock({}),
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      props: {
        requestId: requestId,
      },
    }).then((mounted) => {
      checkBasicPageElements(RequestStatus.Open);
      cy.get('[data-test="viewDataset"]').should('exist').click();
      cy.wrap(mounted.component)
        .its('$route.path')
        .should('eq', `/companies/${dummyCompanyId}/frameworks/${dummyFramework}`);
    });
  });
  it(
    'Check view data request page for answered request and ' +
      'check the routing to data view page on resolve request click',
    function () {
      const dummyRequest = createStoredDataRequest(RequestStatus.Answered, []);
      interceptUserAskForSingleDataRequestsOnMounted(dummyRequest);
      interceptUserAskForCompanyNameOnMounted();
      interceptUserActiveDatasetOnMounted(QaStatus.Accepted);
      interceptPatchRequest();
      cy.mountWithPlugins(ViewDataRequestPage, {
        keycloak: minimalKeycloakMock({}),
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-ignore
        props: {
          requestId: requestId,
        },
      }).then((mounted) => {
        checkBasicPageElements(dummyRequest.requestStatus);
        cy.get('[data-test="resolveRequestButton"]').should('exist').click();
        cy.wrap(mounted.component)
          .its('$route.path')
          .should('eq', `/companies/${dummyCompanyId}/frameworks/${dummyFramework}`);
      });
    }
  );
  it(
    'Check view data request page for open request and check that the message history is displayed ' +
      'and that a user can add a new message',
    function () {
      interceptUserAskForSingleDataRequestsOnMounted(createStoredDataRequest(RequestStatus.Open, [dummyMessageObject]));
      interceptUserAskForCompanyNameOnMounted();
      interceptUserActiveDatasetOnMounted(QaStatus.Accepted);
      interceptPatchRequest();
      cy.mountWithPlugins(ViewDataRequestPage, {
        keycloak: minimalKeycloakMock({ userId: dummyUserId }),
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-ignore
        props: {
          requestId: requestId,
        },
      }).then(() => {
        checkBasicPageElements(RequestStatus.Open);
        cy.get('[data-test="newMessage"]').should('exist').click();
        checkEmailFieldsAndCheckBox('newMessageModal', 'addMessageButton');
      });
    }
  );
});
