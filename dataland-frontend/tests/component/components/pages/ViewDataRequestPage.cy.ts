import ViewDataRequestPage from '@/components/pages/ViewDataRequestPage.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import {
  RequestState,
  RequestPriority,
  type ExtendedStoredRequest,
  type DataSourcingWithoutReferences,
  type StoredRequest,
  type StoredDataSourcing,
  DataSourcingState,
} from '@clients/datasourcingservice';
import type { CompanyInformation } from '@clients/backend';
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { getMountingFunction } from '@ct/testUtils/Mount';
import router from '@/router';

/**
 * Utility function to create a minimal Keycloak mock
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
 * Mocks the data-sourcing-manager answer for data sourcing history
 * @param dataSourcingId the data sourcing entity ID
 * @param history the data sourcing history to return
 */
function interceptDataSourcingHistory(dataSourcingId: string, history: DataSourcingWithoutReferences[]): void {
  cy.intercept(`**/data-sourcing/${dataSourcingId}/history`, {
    body: history,
    status: 200,
  });
}

/**
 * Mocks the data-sourcing-manager answer for request history
 * @param requestId the request ID
 * @param history the request history to return
 */
function interceptRequestHistory(requestId: string, history: StoredRequest[]): void {
  cy.intercept(`**/data-sourcing/requests/${requestId}/history`, {
    body: history,
    status: 200,
  });
}

/**
 * Mocks company info endpoint for resolving UUIDs to names
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
  const dummyLastModifiedDate = 1709204495770;
  const dummyCreationTime = 1709104495770;

  /**
   * Creates a DataSourcingWithoutReferences object with defaults
   * @param overrides optional fields to override
   */
  function createDataSourcingHistoryEntry(
    overrides: Partial<DataSourcingWithoutReferences> = {}
  ): DataSourcingWithoutReferences {
    return {
      dataSourcingId: 'dummyDataSourcingId',
      companyId: dummyCompanyId,
      reportingPeriod: dummyReportingYear,
      dataType: dummyFramework,
      state: DataSourcingState.Initialized,
      lastModifiedDate: dummyLastModifiedDate,
      ...overrides,
    };
  }

  /**
   * Creates a StoredRequest history entry with defaults
   * @param overrides optional fields to override
   */
  function createRequestHistoryEntry(overrides: Partial<StoredRequest> = {}): StoredRequest {
    return {
      id: requestId,
      companyId: dummyCompanyId,
      reportingPeriod: dummyReportingYear,
      dataType: dummyFramework,
      userId: dummyUserId,
      creationTimestamp: dummyCreationTime,
      lastModifiedDate: dummyLastModifiedDate,
      state: RequestState.Open,
      requestPriority: RequestPriority.Low,
      ...overrides,
    };
  }

  /**
   * Mocks the data-sourcing-manager answer for single data request of the users
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
    };
    cy.intercept(`**/data-sourcing/requests/${request.id}`, {
      body: request,
      status: 200,
    });
  }

  /**
   * Mocks the api-manager answer for basic company information
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
   * Creates a request object with data sourcing entity ID
   * @param dataSourcingEntityId the data sourcing entity ID
   */
  function createRequestWithDataSourcing(dataSourcingEntityId: string): ExtendedStoredRequest {
    return {
      id: requestId,
      userId: dummyUserId,
      creationTimestamp: dummyCreationTime,
      dataType: dummyFramework,
      reportingPeriod: dummyReportingYear,
      companyId: dummyCompanyId,
      lastModifiedDate: dummyLastModifiedDate,
      state: RequestState.Processing,
      requestPriority: RequestPriority.Low,
      companyName: dummyCompanyName,
      userEmailAddress: dummyEmail,
      dataSourcingEntityId: dataSourcingEntityId,
    };
  }

  /**
   * Mocks the data-sourcing-manager answer for data sourcing details
   * @param dataSourcingId the data sourcing entity ID
   * @param collectorId the collector company UUID
   * @param extractorId the extractor company UUID
   */
  function interceptDataSourcingDetails(dataSourcingId: string, collectorId: string, extractorId: string): void {
    const dataSourcing: Partial<StoredDataSourcing> = {
      dataSourcingId: dataSourcingId,
      companyId: dummyCompanyId,
      reportingPeriod: dummyReportingYear,
      dataType: dummyFramework,
      state: DataSourcingState.Initialized,
      documentCollector: collectorId,
      dataExtractor: extractorId,
    };
    cy.intercept(`**/data-sourcing/${dataSourcingId}`, {
      body: dataSourcing,
      status: 200,
    });
  }

  /**
   * Sets up all necessary interceptions for a request with data sourcing
   * @param request the request object with data sourcing entity ID
   * @param requestHistory the request history to return
   * @param dataSourcingHistory the data sourcing history to return
   * @param collectorId the collector company UUID
   * @param collectorName the collector company name
   * @param extractorId the extractor company UUID
   * @param extractorName the extractor company name
   */
  function setupDataSourcingInterceptions(
    request: ExtendedStoredRequest,
    requestHistory: StoredRequest[],
    dataSourcingHistory: DataSourcingWithoutReferences[],
    collectorId?: string,
    collectorName?: string,
    extractorId?: string,
    extractorName?: string
  ): void {
    cy.intercept(`**/data-sourcing/requests/${requestId}`, {
      body: request,
      status: 200,
    });
    interceptUserAskForCompanyNameOnMounted();
    interceptUserActiveDatasetOnMounted(false);
    interceptRequestHistory(requestId, requestHistory);
    if (request.dataSourcingEntityId) {
      interceptDataSourcingHistory(request.dataSourcingEntityId, dataSourcingHistory);
      if (collectorId && extractorId) {
        interceptDataSourcingDetails(request.dataSourcingEntityId, collectorId, extractorId);
      }
      if (collectorId && collectorName) {
        interceptCompanyInfo(collectorId, collectorName);
      }
      if (extractorId && extractorName) {
        interceptCompanyInfo(extractorId, extractorName);
      }
    }
  }

  /**
   * Checks the existence of basic elements of the page
   * @param requestState the request state to check for
   */
  function checkBasicPageElementsAsUser(requestState: RequestState): void {
    cy.contains('Data Request').should('exist');
    cy.contains('Request Details').should('exist');
    cy.contains('Request is').should('exist');

    cy.get('[data-test="card_requestDetails"]').should('exist');
    cy.get('[data-test="card_requestDetails"]').within(() => {
      cy.contains('Requester').should('not.exist');
      cy.contains('Company').should('exist');
      cy.contains(`${dummyCompanyName}`).should('exist');
      cy.contains('Framework').should('exist');
      cy.contains(`${humanizeStringOrNumber(dummyFramework)}`).should('exist');
      cy.contains('Reporting year').should('exist');
      cy.contains(`${dummyReportingYear}`).should('exist');
    });
    cy.get('[data-test="card_requestIs"]').should('exist');
    cy.get('[data-test="card_requestIs"]').within(() => {
      cy.contains(requestState).should('exist');
      cy.contains(`${convertUnixTimeInMsToDateString(dummyLastModifiedDate)}`).should('exist');
    });
    cy.get('[data-test="card_withdrawn"]').should('exist').should('not.be.visible');
  }

  /**
   * Checks the existence of basic elements of the page when admin is visiting
   * @param requestState the request state to check for
   */
  function checkBasicPageElementsAsAdmin(requestState: RequestState): void {
    cy.contains('Data Request').should('exist');
    cy.contains('Request Details').should('exist');
    cy.contains('Request is').should('exist');

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
      cy.contains(`${requestState}`).should('exist');
      cy.contains(`${convertUnixTimeInMsToDateString(dummyLastModifiedDate)}`).should('exist');
    });
    if (requestState !== RequestState.Withdrawn) {
      cy.get('[data-test="card_withdrawn"]').should('be.visible');
    }
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
   * @param requestState the request state to check for
   * @param options mounting options (keycloak, router, etc.)
   */
  function mountAndCheckBasicPageElementsAsUser(
    requestState: RequestState,
    options: {
      keycloak?: ReturnType<typeof minimalKeycloakMock>;
      router?: typeof router;
    }
  ): Cypress.Chainable {
    return getMountingFunction(options)(ViewDataRequestPage, {
      props: { requestId: requestId },
    }).then(() => {
      checkBasicPageElementsAsUser(requestState);
    });
  }

  it('Check view data request page for Processed request with data renders as expected', function () {
    setupRequestInterceptions(RequestState.Processed, true);
    cy.spy(router, 'push').as('routerPush');
    mountAndCheckBasicPageElementsAsUser(RequestState.Processed, {
      keycloak: getKeycloakMock(dummyUserId),
      router,
    }).then(() => {
      cy.get('[data-test="resubmit-request-button"]').should('be.visible');
      cy.get('[data-test="view-dataset-button"]').should('exist').click();
      cy.get('@routerPush').should('have.been.calledWith', `/companies/${dummyCompanyId}/frameworks/${dummyFramework}`);
    });
  });

  it('Check view data request page for Withdrawn request without data renders as expected', function () {
    setupRequestInterceptions(RequestState.Withdrawn, false);
    mountAndCheckBasicPageElementsAsUser(RequestState.Withdrawn, {
      keycloak: getKeycloakMock(dummyUserId, ['ROLE_ADMIN']),
    });
    cy.get('[data-test="resubmit-request-button"]').should('be.visible');
    cy.get('[data-test="view-dataset-button"]').should('not.exist');
  });

  it('Check view data request page as non-admin for Open request without data and verify withdraw button is absent', function () {
    setupRequestInterceptions(RequestState.Open, false);
    mountAndCheckBasicPageElementsAsUser(RequestState.Open, {
      keycloak: getKeycloakMock(dummyUserId, ['ROLE_USER']),
    });
    cy.get('[data-test="resubmit-request-button"]').should('not.be.visible');
    cy.get('[data-test="view-dataset-button"]').should('not.exist');
  });

  it('Check view data request page as admin for Open request without data and withdraw the data request', function () {
    setupRequestInterceptions(RequestState.Open, false);
    mountAndCheckBasicPageElementsAsUser(RequestState.Open, {
      keycloak: getKeycloakMock(dummyUserId, ['ROLE_ADMIN']),
    }).then(() => {
      cy.get('[data-test="resubmit-request-button"]').should('not.be.visible');
      cy.get('[data-test="view-dataset-button"]').should('not.exist');
      interceptUserAskForSingleDataRequestsOnMounted(RequestState.Withdrawn);
      cy.get('[data-test="card_withdrawn"]').within(() => {
        cy.contains(
          'If you want to stop the processing of this request, you can withdraw it. The data provider will no longer process this request.'
        ).should('be.visible');
        cy.get('[data-test="withdraw-request-button"]').should('exist').click();
      });
      cy.get('[data-test="success-modal"]').should('exist').should('be.visible').contains('OK').click();
      cy.get('[data-test="success-modal"]').should('not.exist');
      checkBasicPageElementsAsAdmin(RequestState.Withdrawn);
    });
  });

  it('Check view data request page for Open request with data and check the routing to data view page', function () {
    setupRequestInterceptions(RequestState.Open, true);
    cy.spy(router, 'push').as('routerPush');
    mountAndCheckBasicPageElementsAsUser(RequestState.Open, {
      keycloak: getKeycloakMock(dummyUserId),
      router,
    }).then(() => {
      cy.get('[data-test="resubmit-request-button"]').should('not.be.visible');
      cy.get('[data-test="view-dataset-button"]').should('exist').click();
      cy.get('@routerPush').should('have.been.calledWith', `/companies/${dummyCompanyId}/frameworks/${dummyFramework}`);
    });
  });

  it('Check view data request page for Processed request and check resubmitting the request works as expected', function () {
    setupRequestInterceptions(RequestState.Processed, true);
    mountAndCheckBasicPageElementsAsUser(RequestState.Processed, {
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

  it('Check data sourcing details display collector and extractor names when dataSourcingDetails is present', function () {
    const dataSourcingEntityId = 'dummyDataSourcingId';
    const collectorId = 'collector-company-uuid';
    const collectorName = 'Collector Company Ltd.';
    const extractorId = 'extractor-company-uuid';
    const extractorName = 'Extractor GmbH';
    const request = createRequestWithDataSourcing(dataSourcingEntityId);

    setupDataSourcingInterceptions(request, [], [], collectorId, collectorName, extractorId, extractorName);

    getMountingFunction({ keycloak: getKeycloakMock(dummyUserId) })(ViewDataRequestPage, {
      props: { requestId: requestId },
    }).then(() => {
      cy.get('[data-test="card_requestDetails"]').within(() => {
        cy.get('[data-test="data-sourcing-collector"]').should('contain', collectorName);
        cy.get('[data-test="data-sourcing-extractor"]').should('contain', extractorName);
      });
    });
  });

  it('Check data sourcing details are hidden when dataSourcingDetails is not present', function () {
    setupRequestInterceptions(RequestState.Open, false);
    getMountingFunction({ keycloak: getKeycloakMock(dummyUserId) })(ViewDataRequestPage, {
      props: { requestId: requestId },
    }).then(() => {
      cy.get('[data-test="data-sourcing-collector"]').should('not.exist');
      cy.get('[data-test="data-sourcing-extractor"]').should('not.exist');
    });
  });

  it('Check combined history displays both request and data sourcing history sorted by timestamp', function () {
    const dataSourcingEntityId = 'dummyDataSourcingId';
    const oldRequestTimestamp = 1709104495770;
    const request = createRequestWithDataSourcing(dataSourcingEntityId);

    const requestHistory = [
      createRequestHistoryEntry({
        creationTimestamp: oldRequestTimestamp,
        lastModifiedDate: oldRequestTimestamp,
        adminComment: 'Request created',
      }),
    ];

    const dataSourcingHistory = [
      createDataSourcingHistoryEntry({
        dataSourcingId: dataSourcingEntityId,
        state: DataSourcingState.DataExtraction,
        adminComment: 'Data extraction started',
      }),
    ];

    setupDataSourcingInterceptions(
      request,
      requestHistory,
      dataSourcingHistory,
      'collector-uuid',
      'Collector Ltd.',
      'extractor-uuid',
      'Extractor GmbH'
    );

    getMountingFunction({ keycloak: getKeycloakMock(dummyUserId, ['ROLE_ADMIN']) })(ViewDataRequestPage, {
      props: { requestId: requestId },
    }).then(() => {
      cy.get('[data-test="stateHistoryTable"]').should('exist');
      cy.get('[data-test="stateHistoryTable"]').within(() => {
        cy.get('tbody tr').should('have.length', 2);
        cy.contains('Request').should('exist');
        cy.contains('Data Sourcing').should('exist');
        cy.contains(RequestState.Open).should('exist');
        cy.contains(DataSourcingState.DataExtraction).should('exist');
      });
    });
  });
});
