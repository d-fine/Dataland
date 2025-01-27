import AdminAllRequestsOverview from '@/components/pages/AdminAllRequestsOverview.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { DataTypeEnum } from '@clients/backend';
import { KEYCLOAK_ROLE_ADMIN } from '@/utils/KeycloakUtils';
import { getMountingFunction } from '@ct/testUtils/Mount';
import {
  AccessStatus,
  type ExtendedStoredDataRequest,
  RequestPriority,
  RequestStatus,
} from '@clients/communitymanager';
import { faker } from '@faker-js/faker';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import router from '@/router';

describe('Component test for the admin-requests-overview page', () => {
  let mockRequests: ExtendedStoredDataRequest[];
  let mockRequestsLarge: ExtendedStoredDataRequest[];
  const chunkSize = 100;

  /**
   * Generates one ExtendedStoredDataRequest type object
   * @param userEmailAddress of the requesting user
   * @param framework for which data has been requested
   * @param requestStatus of the data request
   * @param accessStatus of the data request
   * @param adminComment of the data request
   * @param requestPriority of the data request
   * @returns the generated ExtendedStoredDataRequest
   */
  function generateExtendedStoredDataRequest(
    userEmailAddress: string,
    framework: DataTypeEnum,
    requestStatus: RequestStatus,
    accessStatus: AccessStatus,
    adminComment: string,
    requestPriority: RequestPriority
  ): ExtendedStoredDataRequest {
    return {
      dataRequestId: crypto.randomUUID(),
      userId: crypto.randomUUID(),
      userEmailAddress: userEmailAddress,
      creationTimestamp: Date.now(),
      dataType: framework,
      reportingPeriod: faker.helpers.arrayElement(['2020', '2021', '2022', '2023']),
      datalandCompanyId: crypto.randomUUID(),
      companyName: faker.company.name(),
      lastModifiedDate: Date.now(),
      requestStatus: requestStatus,
      accessStatus: accessStatus,
      adminComment: adminComment,
      requestPriority: requestPriority,
    };
  }

  const mailAlpha = 'stephanie@fake.com';
  const mailBeta = 'random@fake.com';
  const mailGamma = 'test123@fake.com';
  const mailDelta = 'steven@fake.com';
  const mailSearchTerm = mailAlpha.substring(0, 3);
  const commentAlpha = 'A test comment';
  const commentBeta = 'The second test comment';
  const commentGamma = 'Another test comment';
  const commentDelta = 'The last test comment';
  const commentSearchTerm = commentBeta.substring(0, 3);

  before(function () {
    mockRequests = [
      generateExtendedStoredDataRequest(
        mailAlpha,
        DataTypeEnum.Lksg,
        RequestStatus.Open,
        AccessStatus.Public,
        commentAlpha,
        RequestPriority.Urgent
      ),
      generateExtendedStoredDataRequest(
        mailBeta,
        DataTypeEnum.P2p,
        RequestStatus.Answered,
        AccessStatus.Public,
        commentBeta,
        RequestPriority.Urgent
      ),
      generateExtendedStoredDataRequest(
        mailGamma,
        DataTypeEnum.Vsme,
        RequestStatus.Answered,
        AccessStatus.Declined,
        commentGamma,
        RequestPriority.High
      ),
      generateExtendedStoredDataRequest(
        mailDelta,
        DataTypeEnum.Sfdr,
        RequestStatus.Resolved,
        AccessStatus.Public,
        commentDelta,
        RequestPriority.Low
      ),
    ];
    mockRequestsLarge = [];
    for (let num = 1; num <= 104; num++) {
      const dataType = faker.helpers.arrayElement([DataTypeEnum.Lksg, DataTypeEnum.P2p, DataTypeEnum.Vsme]);
      const email = faker.helpers.arrayElement([mailAlpha, mailBeta, mailGamma, mailDelta]);
      const requestStatus = faker.helpers.arrayElement([
        RequestStatus.Open,
        RequestStatus.Answered,
        RequestStatus.Withdrawn,
      ]);
      const comment = faker.helpers.arrayElement([commentAlpha, commentBeta, commentGamma, commentDelta]);
      const requestPriority = faker.helpers.arrayElement([
        RequestPriority.Low,
        RequestPriority.High,
        RequestPriority.Urgent,
        RequestPriority.Urgent,
      ]);
      mockRequestsLarge.push(
        generateExtendedStoredDataRequest(email, dataType, requestStatus, AccessStatus.Public, comment, requestPriority)
      );
    }
  });

  /**
   * Mounts the page and asserts that the unfiltered list of all data requests is displayed
   * @returns mounted component as Chainable
   */
  function mountAdminAllRequestsPageWithMocks(): Cypress.Chainable {
    const expectedNumberOfRequests = mockRequests.length;
    cy.intercept(`**/community/requests?chunkSize=${chunkSize}&chunkIndex=0`, mockRequests).as(
      'fetchInitialUnfilteredRequests'
    );
    cy.intercept('**/community/requests/numberOfRequests', expectedNumberOfRequests.toString()).as(
      'fetchInitialUnfilteredNumberOfRequests'
    );
    const mountedComponent = getMountingFunction({
      keycloak: minimalKeycloakMock({
        authenticated: true,
        roles: [KEYCLOAK_ROLE_ADMIN],
        userId: crypto.randomUUID(),
      }),
      router: router,
    })(AdminAllRequestsOverview);

    assertNumberOfSearchResults(expectedNumberOfRequests);
    mockRequests.forEach((extendedStoredDataRequest) => {
      if (extendedStoredDataRequest.userEmailAddress) {
        assertEmailAddressExistsInSearchResults(extendedStoredDataRequest.userEmailAddress);
      }
    });
    return mountedComponent;
  }

  /**
   *
   */
  function mountAdminAllRequestsPageWithManyMocks(): void {
    const expectedNumberOfRequests = mockRequestsLarge.length;
    cy.intercept(`**/community/requests?chunkSize=${chunkSize}&chunkIndex=0`, mockRequestsLarge.slice(0, chunkSize));
    cy.intercept('**/community/requests/numberOfRequests', expectedNumberOfRequests.toString());

    getMountingFunction({
      keycloak: minimalKeycloakMock({
        authenticated: true,
        roles: [KEYCLOAK_ROLE_ADMIN],
        userId: crypto.randomUUID(),
      }),
    })(AdminAllRequestsOverview);
    assertNumberOfSearchResults(chunkSize);
  }

  /**
   * Asserts the number of rows with actual data the result table has
   * @param expectedNumber represents the expectation
   */
  function assertNumberOfSearchResults(expectedNumber: number): void {
    cy.get('tr[data-pc-section="bodyrow"]').should('have.length', expectedNumber);
  }

  /**
   * Asserts that there is a search result with the expected email address as requester email address
   * @param emailAddress is the expected email address
   */
  function assertEmailAddressExistsInSearchResults(emailAddress: string): void {
    cy.contains('td', emailAddress);
  }

  /**
   * Validates if filtering via email address substring works as expected
   */
  function validateEmailAddressFilter(): void {
    const mockResponse = [mockRequests[0], mockRequests[3]];
    const expectedNumberOfRequests = mockResponse.length;
    cy.intercept(
      `**/community/requests?emailAddress=${mailSearchTerm}&chunkSize=${chunkSize}&chunkIndex=0`,
      mockResponse
    ).as('fetchEmailFilteredRequests');
    cy.intercept(`**/community/requests/numberOfRequests?emailAddress=ste`, expectedNumberOfRequests.toString()).as(
      'fetchEmailFilteredNumberOfRequests'
    );

    cy.get(`input[data-test="email-searchbar"]`).type(mailSearchTerm);
    cy.get(`button[data-test="trigger-filtering-requests"]`).click();
    assertNumberOfSearchResults(expectedNumberOfRequests);
    assertEmailAddressExistsInSearchResults(mailAlpha);
    assertEmailAddressExistsInSearchResults(mailDelta);
  }

  /**
   * Validates if filtering via framework dropdown filter works as expected
   */
  function validateFrameworkFilter(): void {
    const frameworkToFilterFor = DataTypeEnum.P2p;
    const frameworkHumanReadableName = humanizeStringOrNumber(frameworkToFilterFor);
    const mockResponse = [mockRequests[1]];
    const expectedNumberOfRequests = mockResponse.length;
    cy.intercept(
      `**/community/requests?dataType=${frameworkToFilterFor}&chunkSize=${chunkSize}&chunkIndex=0`,
      mockResponse
    ).as('fetchFrameworkFilteredRequests');
    cy.intercept(
      `**/community/requests/numberOfRequests?dataType=${frameworkToFilterFor}`,
      expectedNumberOfRequests.toString()
    ).as('fetchFrameworkFilteredNumberOfRequests');

    cy.get(`div[data-test="framework-picker"]`).click();
    cy.get(`li[aria-label="${frameworkHumanReadableName}"]`).click();
    cy.get(`button[data-test="trigger-filtering-requests"]`).click();

    assertNumberOfSearchResults(expectedNumberOfRequests);
    assertEmailAddressExistsInSearchResults(mailBeta);
  }

  /**
   * Validates if filtering via data request status dropdown filter works as expected
   */
  function validateRequestStatusFilter(): void {
    const requestStatusToFilterFor = RequestStatus.Open;
    const mockResponse = [mockRequests[0]];
    const expectedNumberOfRequests = mockResponse.length;
    cy.intercept(
      `**/community/requests?requestStatus=${requestStatusToFilterFor}&chunkSize=${chunkSize}&chunkIndex=0`,
      mockResponse
    ).as('fetchRequestStatusFilteredRequests');
    cy.intercept(
      `**/community/requests/numberOfRequests?requestStatus=${requestStatusToFilterFor}`,
      expectedNumberOfRequests.toString()
    ).as('fetchRequestStatusFilteredNumberOfRequests');

    cy.get(`div[data-test="request-status-picker"]`).click();
    cy.get(`li[aria-label="${requestStatusToFilterFor}"]`).click();
    cy.get(`button[data-test="trigger-filtering-requests"]`).click();
    assertNumberOfSearchResults(expectedNumberOfRequests);
    assertEmailAddressExistsInSearchResults(mailAlpha);
  }

  /**
   * Validates if filtering via priority dropdown filter works as expected
   */
  function validateRequestPriorityFilter(): void {
    const priorityToFilterFor = RequestPriority.Urgent;
    const mockResponse = [mockRequests[0], mockRequests[1]];
    const expectedNumberOfRequests = mockResponse.length;
    cy.intercept(
      `**/community/requests?requestPriority=${priorityToFilterFor}&chunkSize=${chunkSize}&chunkIndex=0`,
      mockResponse
    ).as('fetchPriorityFilteredRequests');
    cy.intercept(
      `**/community/requests/numberOfRequests?requestPriority=${priorityToFilterFor}`,
      expectedNumberOfRequests.toString()
    ).as('fetchFrameworkFilteredNumberOfRequests');

    cy.get(`div[data-test="request-priority-picker"]`).click();
    cy.get(`li[aria-label="${priorityToFilterFor}"]`).click();
    cy.get(`button[data-test="trigger-filtering-requests"]`).click();

    assertNumberOfSearchResults(expectedNumberOfRequests);
    assertEmailAddressExistsInSearchResults(mailAlpha);
    assertEmailAddressExistsInSearchResults(mailBeta);
  }

  /**
   * Validates if filtering via admin comment substring works as expected
   */
  function validateAdminCommentFilter(): void {
    const mockResponse = [mockRequests[1], mockRequests[3]];
    const expectedNumberOfRequests = mockResponse.length;
    cy.intercept(
      `**/community/requests?adminComment=${commentSearchTerm}&chunkSize=${chunkSize}&chunkIndex=0`,
      mockResponse
    ).as('fetchCommentFilteredRequests');
    cy.intercept(`**/community/requests/numberOfRequests?adminComment=last`, expectedNumberOfRequests.toString()).as(
      'fetchCommentFilteredNumberOfRequests'
    );

    cy.get(`input[data-test="comment-searchbar"]`).type(commentSearchTerm);
    cy.get(`button[data-test="trigger-filtering-requests"]`).click();
    assertNumberOfSearchResults(expectedNumberOfRequests);
    assertEmailAddressExistsInSearchResults(mailBeta);
    assertEmailAddressExistsInSearchResults(mailDelta);
  }

  /**
   * Validates if combining two filters leads to a combined filter query
   */
  function validateCombinedFilter(): void {
    validateEmailAddressFilter();
    const frameworkToFilterFor = DataTypeEnum.Sfdr;
    const frameworkHumanReadableName = humanizeStringOrNumber(frameworkToFilterFor);
    const mockResponse = [mockRequests[3]];
    const expectedNumberOfRequests = mockResponse.length;
    cy.intercept(
      `**/community/requests?dataType=${frameworkToFilterFor}&emailAddress=${mailSearchTerm}&chunkSize=${chunkSize}&chunkIndex=0`,
      mockResponse
    ).as('fetchCombinedFilteredRequests');
    cy.intercept(
      `**/community/requests/numberOfRequests?dataType=${frameworkToFilterFor}&emailAddress=${mailSearchTerm}`,
      expectedNumberOfRequests.toString()
    ).as('fetchCombinedFilteredNumberOfRequests');

    cy.get(`div[data-test="framework-picker"]`).click();
    cy.get(`li[aria-label="${frameworkHumanReadableName}"]`).click();
    cy.get(`button[data-test="trigger-filtering-requests"]`).click();
    assertNumberOfSearchResults(expectedNumberOfRequests);
    assertEmailAddressExistsInSearchResults(mailDelta);
  }

  /**
   * Removes the combined filter and checks if all requests are shown again
   */
  function validateDeselectingCombinedFilter(): void {
    const expectedNumberOfRequests = mockRequests.length;
    cy.intercept(`**/community/requests?chunkSize=${chunkSize}&chunkIndex=0`, mockRequests).as(
      'fetchInitialUnfilteredRequests'
    );
    cy.intercept('**/community/requests/numberOfRequests', expectedNumberOfRequests.toString()).as(
      'fetchInitialUnfilteredNumberOfRequests'
    );
    const frameworkHumanReadableName = humanizeStringOrNumber(DataTypeEnum.Sfdr);
    cy.get(`li[aria-label="${frameworkHumanReadableName}"]`).click();
    cy.get(`input[data-test="email-searchbar"]`).clear().type('{enter}');
    cy.get(`button[data-test="trigger-filtering-requests"]`).click();

    assertNumberOfSearchResults(expectedNumberOfRequests);
    assertEmailAddressExistsInSearchResults(mailAlpha);
    assertEmailAddressExistsInSearchResults(mailDelta);
  }

  /**
   * Validates onPage Event
   */
  function validateOnPageEvent(): void {
    const secondPageMockResponse = mockRequestsLarge.slice(chunkSize);
    const expectedNumberOfRequests = mockRequestsLarge.length;
    const expectedNumberOfRows = secondPageMockResponse.length;
    cy.intercept(`**/community/requests?chunkSize=${chunkSize}&chunkIndex=1`, secondPageMockResponse).as(
      'fetchRequests'
    );
    cy.intercept(`**/community/requests/numberOfRequests`, expectedNumberOfRequests.toString());

    cy.get(`button[aria-label="Page 2"]`).click();

    cy.wait('@fetchRequests');
    assertNumberOfSearchResults(expectedNumberOfRows);
  }

  /**
   * Removes the combined filter via resetButton and checks if all requests are shown again
   */
  function validateResetButton(): void {
    const expectedNumberOfRequests = mockRequests.length;
    cy.intercept(`**/community/requests?chunkSize=${chunkSize}&chunkIndex=0`, mockRequests).as(
      'fetchInitialUnfilteredRequests'
    );
    cy.intercept('**/community/requests/numberOfRequests', expectedNumberOfRequests.toString()).as(
      'fetchInitialUnfilteredNumberOfRequests'
    );
    cy.get(`[data-test=reset-filter]`).click();

    assertNumberOfSearchResults(expectedNumberOfRequests);
    assertEmailAddressExistsInSearchResults(mailAlpha);
    assertEmailAddressExistsInSearchResults(mailDelta);
  }

  it('Filtering for an email address works as expected', () => {
    mountAdminAllRequestsPageWithMocks();
    validateEmailAddressFilter();
  });

  it('Filtering for a framework works as expected', () => {
    mountAdminAllRequestsPageWithMocks();
    validateFrameworkFilter();
  });

  it('Filtering for request status works as expected', () => {
    mountAdminAllRequestsPageWithMocks();
    validateRequestStatusFilter();
  });

  it('Filtering for request priority works as exprected', () => {
    mountAdminAllRequestsPageWithMocks();
    validateRequestPriorityFilter();
  });

  it('Filtering for an admin comment works as expected', () => {
    mountAdminAllRequestsPageWithMocks();
    validateAdminCommentFilter();
  });

  it('A combined filter works as expected and is also de-selectable', () => {
    mountAdminAllRequestsPageWithMocks();
    validateCombinedFilter();
    validateDeselectingCombinedFilter();
  });

  it('A combined filter works as expected and reset Button works as expected', () => {
    mountAdminAllRequestsPageWithMocks();
    validateCombinedFilter();
    validateResetButton();
  });

  it('Check the functionality of the onPage event', () => {
    mountAdminAllRequestsPageWithManyMocks();
    validateOnPageEvent();
  });

  it('Check the functionality of the rowClick event', () => {
    cy.spy(router, 'push').as('routerPush');
    mountAdminAllRequestsPageWithMocks().then(() => {
      const dataRequestIdOfLastElement = mockRequests[mockRequests.length - 1].dataRequestId;

      cy.get('[data-test=requests-datatable]').within(() => {
        cy.get('tr:last').click();
      });

      cy.get('@routerPush').should('have.been.calledWith', `/requests/${dataRequestIdOfLastElement}`);
    });
  });
});
