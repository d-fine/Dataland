import ViewDataRequestPage from '@/components/pages/ViewDataRequestPage.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { RequestState, RequestPriority, type StoredRequest } from '@clients/datasourcingservice';
import type { CompanyInformation } from '@clients/backend';
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { getMountingFunction } from '@ct/testUtils/Mount';
import router from '@/router';
import { KEYCLOAK_ROLE_ADMIN } from '@/utils/KeycloakRoles';

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
   * Return a stored data request
   * @param requestState the request state
   * @returns stored data request
   */
  function createStoredDataRequest(requestState: RequestState): StoredRequest {
    return {
      id: requestId,
      userId: dummyUserId,
      creationTimeStamp: dummyCreationTime,
      dataType: dummyFramework,
      reportingPeriod: dummyReportingYear,
      companyId: dummyCompanyId,
      lastModifiedDate: dummyLastModifiedDate,
      state: requestState,
      requestPriority: RequestPriority.Low,
    };
  }
  /**
   * Mocks the data-sourcing-manager answer for single data request of the users
   * @param request the request to mock
   */
  function interceptUserAskForSingleDataRequestsOnMounted(request: StoredRequest): void {
    cy.intercept(`**/data-sourcing/requests/${requestId}`, {
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
   * Mocks the data-sourcing-manager answer for patching a data request
   */
  function interceptPatchRequest(): void {
    cy.intercept(`**/data-sourcing/requests/${requestId}/state?**`, {
      status: 200,
    });
  }

  /**
   * Checks the existence of basic elements of the page
   * @param requestState the request state to check for
   */
  function checkBasicPageElementsAsUser(requestState: RequestState): void {
    cy.contains('Data Request').should('exist');
    cy.contains('Request Details').should('exist').should('have.class', 'card__title');
    cy.contains('Request is').should('exist').should('have.class', 'card__title');

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
    cy.contains('Request Details').should('exist').should('have.class', 'card__title');
    cy.contains('Request is').should('exist').should('have.class', 'card__title');

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

  it('Check view data request page for Processed request with data renders as expected', function () {
    interceptUserAskForSingleDataRequestsOnMounted(
      createStoredDataRequest(RequestState.Processed)
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
      checkBasicPageElementsAsUser(RequestState.Processed);
      cy.get('[data-test="resubmit-request-button"]').should('be.visible');
      cy.get('[data-test="view-dataset-button"]').should('exist').click();
      cy.get('@routerPush').should('have.been.calledWith', `/companies/${dummyCompanyId}/frameworks/${dummyFramework}`);
    });
  });

  it('Check view data request page for Withdrawn request without data renders as expected', function () {
    interceptUserAskForSingleDataRequestsOnMounted(createStoredDataRequest(RequestState.Withdrawn));
    interceptUserAskForCompanyNameOnMounted();
    interceptUserActiveDatasetOnMounted(false);
    interceptPatchRequest();
    getMountingFunction({ keycloak: minimalKeycloakMock({ userId: dummyUserId, roles: ["ROLE_ADMIN"] }) })(ViewDataRequestPage, {
      props: {
        requestId: requestId,
      },
    });
    checkBasicPageElementsAsUser(RequestState.Withdrawn);
    cy.get('[data-test="resubmit-request-button"]').should('be.visible');
    cy.get('[data-test="view-dataset-button"]').should('not.exist');
  });

    it('Check view data request page as non-admin for Open request without data and verify withdraw button is absent', function () {
      interceptUserAskForSingleDataRequestsOnMounted(createStoredDataRequest(RequestState.Open));
      interceptUserAskForCompanyNameOnMounted();
      interceptUserActiveDatasetOnMounted(false);
      interceptPatchRequest();
      getMountingFunction({
        keycloak: minimalKeycloakMock({ userId: dummyUserId, roles: ["ROLE_USER"] }) })
      (ViewDataRequestPage, {
        props: { requestId: requestId },
      });
      checkBasicPageElementsAsUser(RequestState.Open);
      cy.get('[data-test="resubmit-request-button"]').should('not.be.visible');
      cy.get('[data-test="view-dataset-button"]').should('not.exist');
    });


    it('Check view data request page as admin for Open request without data and withdraw the data request', function () {
    interceptUserAskForSingleDataRequestsOnMounted(createStoredDataRequest(RequestState.Open));
    interceptUserAskForCompanyNameOnMounted();
    interceptUserActiveDatasetOnMounted(false);
    interceptPatchRequest();
      const keyCloakMock = minimalKeycloakMock({ userId: dummyUserId, roles: [KEYCLOAK_ROLE_ADMIN] });
      keyCloakMock.tokenParsed = keyCloakMock.tokenParsed || {};
      keyCloakMock.tokenParsed.email = dummyEmail;
    getMountingFunction({
      keycloak: keyCloakMock })
    (ViewDataRequestPage, {
      props: { requestId: requestId },
    });
    checkBasicPageElementsAsAdmin(RequestState.Open);
    cy.get('[data-test="resubmit-request-button"]').should('not.be.visible');
    cy.get('[data-test="view-dataset-button"]').should('not.exist');
    interceptUserAskForSingleDataRequestsOnMounted(createStoredDataRequest(RequestState.Withdrawn));
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

  it('Check view data request page for Open request with data and check the routing to data view page', function () {
    interceptUserAskForSingleDataRequestsOnMounted(createStoredDataRequest(RequestState.Open));
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
      checkBasicPageElementsAsUser(RequestState.Open);
      cy.get('[data-test="resubmit-request-button"]').should('not.be.visible');
      cy.get('[data-test="view-dataset-button"]').should('exist').click();
      cy.get('@routerPush').should('have.been.calledWith', `/companies/${dummyCompanyId}/frameworks/${dummyFramework}`);
    });
  });

  it(
    'Check view data request page for Processed request and check resubmitting the request works as expected',
    function () {
      const dummyRequest = createStoredDataRequest(RequestState.Processed);
      interceptUserAskForSingleDataRequestsOnMounted(dummyRequest);
      interceptUserAskForCompanyNameOnMounted();
      interceptUserActiveDatasetOnMounted(true);
      getMountingFunction({ keycloak: minimalKeycloakMock({ userId: dummyUserId }), router })(ViewDataRequestPage, {
        props: { requestId: requestId },
      }).then(() => {
        checkBasicPageElementsAsUser(RequestState.Processed);
        cy.contains(
            'Currently, your request has the state Processed.'
        ).should('be.visible');
        cy.get('[data-test="view-dataset-button"]').should('exist');
        cy.get('[data-test="resubmit-request-button"]').should('be.visible').click();
        cy.get('[data-test="resubmit-modal"]').should('be.visible');
        cy.get('[data-test="resubmit-message"]').should('exist').type('Need');
        cy.get('[data-test="resubmit-confirmation-button"]').should('be.visible').click();
        cy.get('[data-test="noMessageErrorMessage"]').should('be.visible');
        cy.get('[data-test="resubmit-message"]').should('exist').type(' updated data.');
        cy.get('[data-test="noMessageErrorMessage"]').should('not.exist');
        cy.get('[data-test="resubmit-confirmation-button"]').should('be.visible');
      });
    }
  );
});
