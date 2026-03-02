import ViewDataRequestPage from '@/components/pages/ViewDataRequestPage.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import {
  RequestState,
  RequestPriority,
  type ExtendedStoredRequest,
  type StoredDataSourcing,
  DataSourcingState,
  DisplayedState,
  type ExtendedRequestHistoryEntryData,
  type RequestHistoryEntryData,
} from '@clients/datasourcingservice';
import type { CompanyInformation } from '@clients/backend';
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { getMountingFunction } from '@ct/testUtils/Mount';
import router from '@/router';
import { getDisplayedStateWithSpaces } from '@/utils/RequestsOverviewPageUtils.ts';

/**
 * Utility function to create a minimal Keycloak mock
 *
 * @param userId the user ID to mock
 * @param roles optional array of roles (e.g. ['ROLE_ADMIN'])
 */
function getKeycloakMock(userId: string, roles: string[] = ['ROLE_USER']): ReturnType<typeof minimalKeycloakMock> {
  return minimalKeycloakMock({
    userId,
    roles,
  });
}

/**
 * Mocks company info endpoint for resolving UUIDs to names
 *
 * @param companyId the company UUID
 * @param companyName the company name to return
 */
function interceptCompanyInfo(companyId: string, companyName: string): void {
  cy.intercept(`**/api/companies/${companyId}/info`, {
    body: { companyName: companyName } as Partial<CompanyInformation>,
    status: 200,
  });
}

describe('Component tests for the view data request page', function (): void {
  const requestId = 'dummyRequestId';
  const dummyUserId = 'dummyUserId';
  const dummyEmail = 'dummy@mail.de';
  const dummyCompanyId = 'dummyCompanyId';
  const dummyCompanyName = 'dummyCompanyName';
  const dummyFramework = 'dummyFramework';
  const dummyReportingYear = 'dummyReportingYear';
  const dummyCreationTime = 1709104495770;
  const dummyLastModifiedDate = dummyCreationTime + 600000;

  const dataSourcingEntityId = 'dummyDataSourcingId';
  const collectorId = 'collector-company-uuid';
  const collectorName = 'Collector Company Ltd.';
  const extractorId = 'extractor-company-uuid';
  const extractorName = 'Extractor GmbH';

  const dummyProcessedExtendedRequestHistoryEntry: ExtendedRequestHistoryEntryData[] = [
    {
      modificationDate: dummyLastModifiedDate + 600000,
      displayedState: DisplayedState.NonSourceable,
      requestState: RequestState.Processed,
      dataSourcingState: DataSourcingState.NonSourceable,
      adminComment: 'Request processed',
    },
  ];

  const dummyWithdrawnExtendedRequestHistoryEntry: ExtendedRequestHistoryEntryData[] = [
    {
      modificationDate: dummyLastModifiedDate,
      displayedState: DisplayedState.Withdrawn,
      requestState: RequestState.Withdrawn,
      dataSourcingState: DataSourcingState.NonSourceable,
      adminComment: 'Request processed',
    },
  ];

  const dummyOpenExtendedRequestHistoryEntry: ExtendedRequestHistoryEntryData[] = [
    { modificationDate: dummyLastModifiedDate, displayedState: DisplayedState.Open, requestState: RequestState.Open },
  ];

  const dummyOpenAndWithdrawnExtendedRequestHistoryEntry: ExtendedRequestHistoryEntryData[] = [
    { modificationDate: dummyLastModifiedDate, displayedState: DisplayedState.Open, requestState: RequestState.Open },
    {
      modificationDate: dummyLastModifiedDate + 600000,
      displayedState: DisplayedState.Withdrawn,
      requestState: RequestState.Withdrawn,
    },
  ];

  const dummyOpenRequestHistoryEntry: RequestHistoryEntryData[] = [
    { modificationDate: dummyLastModifiedDate, displayedState: DisplayedState.Open },
  ];

  const dummyProcessedRequestHistoryEntry: RequestHistoryEntryData[] = [
    { modificationDate: dummyLastModifiedDate, displayedState: DisplayedState.Done },
  ];

  /**
   * Checks the presence and content of basic page elements on the view data request page, with conditional checks based on admin status and expected state/time
   *
   * @param isAdminUser boolean indicating if the user is an admin (affects visibility of certain elements)
   * @param expectedState the expected state to check for in the "Request is" card
   * @param expectedTime the expected time to check for in the "Request is" card
   */
  function checkBasicPageElements(isAdminUser: boolean, expectedState: DisplayedState, expectedTime: number): void {
    cy.contains('Data Request').should('exist');
    cy.contains('Request Details').should('exist');
    cy.contains('Request is').should('exist');
    cy.contains('Document Collector').should(isAdminUser ? 'exist' : 'not.exist');
    cy.contains('Data Extractor').should(isAdminUser ? 'exist' : 'not.exist');

    cy.get('[data-test="card_requestDetails"]').should('exist');
    cy.get('[data-test="card_requestDetails"]').within(() => {
      cy.contains('Requester').should(isAdminUser ? 'exist' : 'not.exist');
      cy.contains(`${dummyEmail}`).should(isAdminUser ? 'exist' : 'not.exist');
      cy.contains('Company').should('exist');
      cy.contains(`${dummyCompanyName}`).should('exist');
      cy.contains('Framework').should('exist');
      cy.contains(`${humanizeStringOrNumber(dummyFramework)}`).should('exist');
      cy.contains('Reporting year').should('exist');
      cy.contains(`${dummyReportingYear}`).should('exist');
    });
    cy.get('[data-test="card_requestIs"]').should('exist');
    cy.get('[data-test="card_requestIs"]').within(() => {
      const searchString =
        getDisplayedStateWithSpaces(expectedState) + ' since ' + convertUnixTimeInMsToDateString(expectedTime);
      cy.contains(searchString).should('exist');
    });
    if (expectedState !== DisplayedState.Withdrawn && isAdminUser) {
      cy.get('[data-test="card_withdrawn"]').should('be.visible');
    } else {
      cy.get('[data-test="card_withdrawn"]').should('exist').should('not.be.visible');
    }
  }

  /**
   * Mocks the data-sourcing-manager answer for single data request of the users
   *
   * @param requestState the request state
   */
  function interceptUserAskForSingleDataRequestsOnMounted(requestState: RequestState): void {
    const request: ExtendedStoredRequest = {
      id: requestId,
      userId: dummyUserId,
      creationTimestamp: dummyCreationTime,
      dataType: dummyFramework,
      reportingPeriod: dummyReportingYear,
      companyId: dummyCompanyId,
      lastModifiedDate: dummyLastModifiedDate,
      state: requestState,
      requestPriority: RequestPriority.Low,
      companyName: dummyCompanyName,
      userEmailAddress: dummyEmail,
      dataSourcingEntityId: dataSourcingEntityId,
    };
    cy.intercept(`**/data-sourcing/requests/${request.id}`, {
      body: request,
      status: 200,
    });
  }

  /**
   * Mocks the data-sourcing-manager answer for data sourcing details, with optional parameters to customize the response
   *
   * @param customCollectorId optional custom collector company ID to include in the response
   * @param customExtractorId optional custom extractor company ID to include in the response
   * @param nextDataSourcingDate optional custom date for the "date of next sourcing attempt" field in the response
   */
  function interceptDataSourcingDetails(
    customCollectorId?: string,
    customExtractorId?: string,
    nextDataSourcingDate?: string
  ): void {
    const dataSourcing: Partial<StoredDataSourcing> = {
      dataSourcingId: dataSourcingEntityId,
      companyId: dummyCompanyId,
      reportingPeriod: dummyReportingYear,
      dataType: dummyFramework,
      documentCollector: customCollectorId,
      dataExtractor: customExtractorId,
      dateOfNextDocumentSourcingAttempt: nextDataSourcingDate,
    };
    cy.intercept(`**/data-sourcing/${dataSourcingEntityId}`, {
      body: dataSourcing,
      status: 200,
    });
  }

  /**
   * Mocks the data-sourcing-manager answer for active datasets of the user
   *
   * @param hasActiveDataset boolean indicating whether to return an active dataset or an empty array
   */
  function interceptUserActiveDatasetOnMounted(hasActiveDataset: boolean): void {
    const dummyMetaData = {
      companyId: dummyCompanyId,
      dataType: dummyFramework,
    };
    cy.intercept(`**/api/metadata?**`, {
      body: hasActiveDataset ? [dummyMetaData] : [],
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
   * Mocks the data-sourcing-manager answer for patching a data request
   */
  function interceptPatchRequest(): void {
    cy.intercept(`**/data-sourcing/requests/${requestId}/state?**`, {
      status: 200,
    });
  }

  /**
   * Mocks the data-sourcing-manager answer for the extended request history of a data request, with customizable
   * history entries
   *
   * @param requestHistory the request history entries to return in the mocked response
   */
  function interceptExtendedRequestHistory(requestHistory: RequestHistoryEntryData[]): void {
    cy.intercept(`**/data-sourcing/requests/${requestId}/extended-history`, {
      body: requestHistory,
      status: 200,
    });
  }

  /**
   * Mocks the data-sourcing-manager answer for the request history of a data request, with customizable history entries
   *
   * @param requestHistory the request history entries to return in the mocked response
   */
  function interceptRequestHistory(requestHistory: RequestHistoryEntryData[]): void {
    cy.intercept(`**/data-sourcing/requests/${requestId}/history`, {
      body: requestHistory,
      status: 200,
    });
  }

  /**
   * Sets up interceptions for data sourcing details and mocks company info for collector and extractor.
   */
  function setupDataSourcingDetailsInterceptions(): void {
    interceptDataSourcingDetails(collectorId, extractorId);
    interceptCompanyInfo(collectorId, collectorName);
    interceptCompanyInfo(extractorId, extractorName);
  }

  /**
   * Sets up all necessary interceptions for a given request state and dataset presence
   */
  function setupRequestInterceptions(requestState: RequestState, hasActiveDataset: boolean): void {
    interceptUserAskForSingleDataRequestsOnMounted(requestState);
    interceptUserAskForCompanyNameOnMounted();
    interceptUserActiveDatasetOnMounted(hasActiveDataset);
    interceptPatchRequest();
  }

  /**
   * Mounts the ViewDataRequestPage and checks basic page elements as user
   *
   * @param expectedState the request state to check for
   * @param expectedTime the expected time to check for
   * @param options mounting options (keycloak, router, etc.)
   */
  function mountAndCheckBasicPageElements(
    expectedState: DisplayedState,
    expectedTime: number,
    options: {
      keycloak: ReturnType<typeof minimalKeycloakMock>;
      router?: typeof router;
    }
  ): Cypress.Chainable {
    return getMountingFunction(options)(ViewDataRequestPage, {
      props: { requestId: requestId },
    }).then(() => {
      if (options.keycloak.hasRealmRole('ROLE_ADMIN')) {
        checkBasicPageElements(true, expectedState, expectedTime);
      } else {
        checkBasicPageElements(false, expectedState, expectedTime);
      }
    });
  }

  it('Check view data request page for Processed request with data renders as expected', function () {
    setupRequestInterceptions(RequestState.Processed, true);

    setupDataSourcingDetailsInterceptions();

    interceptExtendedRequestHistory(dummyProcessedExtendedRequestHistoryEntry);

    cy.spy(router, 'push').as('routerPush');
    mountAndCheckBasicPageElements(DisplayedState.NonSourceable, dummyLastModifiedDate + 600000, {
      keycloak: getKeycloakMock(dummyUserId, ['ROLE_ADMIN']),
      router,
    }).then(() => {
      cy.get('[data-test="resubmit-request-button"]').should('be.visible');
      cy.get('[data-test="view-dataset-button"]').should('exist').click();
      cy.get('@routerPush').should('have.been.calledWith', `/companies/${dummyCompanyId}/frameworks/${dummyFramework}`);
    });
  });

  it('Check view data request page for Withdrawn request without data renders as expected (=no view dataset button)', function () {
    setupRequestInterceptions(RequestState.Withdrawn, false);
    interceptExtendedRequestHistory(dummyWithdrawnExtendedRequestHistoryEntry);

    mountAndCheckBasicPageElements(DisplayedState.Withdrawn, dummyLastModifiedDate, {
      keycloak: getKeycloakMock(dummyUserId, ['ROLE_ADMIN']),
    });
    cy.get('[data-test="resubmit-request-button"]').should('be.visible');
    cy.get('[data-test="view-dataset-button"]').should('not.exist');
  });

  it('Check view data request page as non-admin for Open request without data and verify withdraw button is absent', function () {
    setupRequestInterceptions(RequestState.Open, false);
    interceptRequestHistory(dummyOpenRequestHistoryEntry);
    mountAndCheckBasicPageElements(DisplayedState.Open, dummyLastModifiedDate, {
      keycloak: getKeycloakMock(dummyUserId, ['ROLE_USER']),
    });
    cy.get('[data-test="resubmit-request-button"]').should('not.be.visible');
    cy.get('[data-test="view-dataset-button"]').should('not.exist');
  });

  it('Check view data request page as admin for Open request without data and withdraw the data request', function () {
    setupRequestInterceptions(RequestState.Open, false);
    interceptExtendedRequestHistory(dummyOpenExtendedRequestHistoryEntry);
    mountAndCheckBasicPageElements(DisplayedState.Open, dummyLastModifiedDate, {
      keycloak: getKeycloakMock(dummyUserId, ['ROLE_ADMIN']),
    }).then(() => {
      cy.get('[data-test="resubmit-request-button"]').should('not.be.visible');
      cy.get('[data-test="view-dataset-button"]').should('not.exist');
      interceptUserAskForSingleDataRequestsOnMounted(RequestState.Withdrawn);
      interceptExtendedRequestHistory(dummyOpenAndWithdrawnExtendedRequestHistoryEntry);
      cy.get('[data-test="card_withdrawn"]').within(() => {
        cy.contains(
          'If you want to stop the processing of this request, you can withdraw it. The data provider will no longer process this request.'
        ).should('be.visible');
        cy.get('[data-test="withdraw-request-button"]').should('exist').click();
      });
      cy.get('[data-test="success-modal"]').should('exist').should('be.visible').contains('OK').click();
      cy.get('[data-test="success-modal"]').should('not.exist');
      checkBasicPageElements(true, DisplayedState.Withdrawn, dummyLastModifiedDate + 600000);
    });
  });

  it('Check view data request page for Open request with data and check the routing to data view page', function () {
    setupRequestInterceptions(RequestState.Open, true);
    interceptRequestHistory(dummyOpenRequestHistoryEntry);
    cy.spy(router, 'push').as('routerPush');
    mountAndCheckBasicPageElements(DisplayedState.Open, dummyLastModifiedDate, {
      keycloak: getKeycloakMock(dummyUserId, ['ROLE_USER']),
      router,
    }).then(() => {
      cy.get('[data-test="resubmit-request-button"]').should('not.be.visible');
      cy.get('[data-test="view-dataset-button"]').should('exist').click();
      cy.get('@routerPush').should('have.been.calledWith', `/companies/${dummyCompanyId}/frameworks/${dummyFramework}`);
    });
  });

  it('Check view data request page for Processed request and check resubmitting the request works as expected', function () {
    setupRequestInterceptions(RequestState.Processed, true);
    interceptRequestHistory(dummyProcessedRequestHistoryEntry);
    mountAndCheckBasicPageElements(DisplayedState.Done, dummyLastModifiedDate, {
      keycloak: getKeycloakMock(dummyUserId),
      router,
    }).then(() => {
      cy.contains('Currently, your request has the state Processed.').should('be.visible');
      cy.get('[data-test="view-dataset-button"]').should('exist');
      cy.get('[data-test="resubmit-request-button"]').should('be.visible').click();
      cy.get('[data-test="resubmit-modal"]').should('be.visible');
      cy.get('[data-test="resubmit-message"]').should('exist').type('Need');
      cy.get('[data-test="resubmit-confirmation-button"]').should('be.visible').click();
      cy.get('[data-test="noMessageErrorMessage"]').should('be.visible');
      cy.get('[data-test="resubmit-message"]').should('exist').type(' updated data.');
      cy.get('[data-test="noMessageErrorMessage"]').should('not.exist');
      cy.intercept('POST', '**/data-sourcing/requests**', { body: { requestId: 'newId' } }).as('createRequest');
      cy.get('[data-test="resubmit-confirmation-button"]').click();
      cy.wait('@createRequest');
    });
  });

  it('Check display of collector and extractor names when present and not null', function () {
    interceptUserAskForSingleDataRequestsOnMounted(RequestState.Processing);
    interceptDataSourcingDetails(collectorId, extractorId);
    interceptCompanyInfo(collectorId, collectorName);
    interceptCompanyInfo(extractorId, extractorName);
    getMountingFunction({ keycloak: getKeycloakMock(dummyUserId, ['ROLE_ADMIN']) })(ViewDataRequestPage, {
      props: { requestId: requestId },
    }).then(() => {
      cy.get('[data-test="card_requestDetails"]').within(() => {
        cy.get('[data-test="data-sourcing-collector"]').should('contain', collectorName);
        cy.get('[data-test="data-sourcing-extractor"]').should('contain', extractorName);
      });
    });
  });

  it('Check display of collector and extractor names when null', function () {
    interceptUserAskForSingleDataRequestsOnMounted(RequestState.Processing);
    interceptDataSourcingDetails();
    interceptCompanyInfo(collectorId, collectorName);
    interceptCompanyInfo(extractorId, extractorName);

    getMountingFunction({ keycloak: getKeycloakMock(dummyUserId, ['ROLE_ADMIN']) })(ViewDataRequestPage, {
      props: { requestId: requestId },
    }).then(() => {
      cy.get('[data-test="card_requestDetails"]').within(() => {
        cy.get('[data-test="data-sourcing-collector"]').should('contain', '—');
        cy.get('[data-test="data-sourcing-extractor"]').should('contain', '—');
      });
    });
  });

  it('Check that "Date of next sourcing attempt" does not show when null', function (): void {
    interceptUserAskForSingleDataRequestsOnMounted(RequestState.Processing);
    interceptDataSourcingDetails();

    getMountingFunction({ keycloak: getKeycloakMock(dummyUserId, ['ROLE_ADMIN']) })(ViewDataRequestPage, {
      props: { requestId: requestId },
    }).then(() => {
      cy.get('[data-test="card_requestDetails"]').within(() => {
        cy.get('[data-test="date-next-sourcing-attempt"]').should('not.exist');
        cy.contains('Date of next sourcing attempt').should('not.exist');
      });
    });
  });

  it('Check that "Date of next sourcing attempt" shows correctly when not null', function (): void {
    interceptUserAskForSingleDataRequestsOnMounted(RequestState.Processing);
    interceptDataSourcingDetails(undefined, undefined, '2026-02-06');

    getMountingFunction({ keycloak: getKeycloakMock(dummyUserId, ['ROLE_ADMIN']) })(ViewDataRequestPage, {
      props: { requestId: requestId },
    }).then(() => {
      cy.get('[data-test="card_requestDetails"]').within(() => {
        cy.get('[data-test="date-next-sourcing-attempt"]').should('exist');
        cy.contains('Date of next sourcing attempt').should('exist');
        cy.get('[data-test="date-next-sourcing-attempt"]').should('contain', 'Fri, 6 Feb 2026');
      });
    });
  });
});
