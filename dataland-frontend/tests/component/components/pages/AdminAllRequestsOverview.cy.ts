import AdminAllRequestsOverview from '@/components/pages/AdminAllRequestsOverview.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { DataTypeEnum } from '@clients/backend';
import { getMountingFunction } from '@ct/testUtils/Mount';
import { type ExtendedStoredRequest, RequestPriority, RequestState } from '@clients/datasourcingservice';
import { faker } from '@faker-js/faker';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import router from '@/router';
import { KEYCLOAK_ROLE_ADMIN } from '@/utils/KeycloakRoles';

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

describe('Component test for the admin-requests-overview page', () => {
  let mockRequests: ExtendedStoredRequest[];
  let mockRequestsLarge: ExtendedStoredRequest[];
  const chunkSize = 100;

  /**
   * A collection of fields of extended stored data requests that can be filtered by.
   */
  type FilterParameters = {
    userEmailAddress: string;
    framework: DataTypeEnum;
    state: RequestState;
    adminComment: string;
    requestPriority: RequestPriority;
    companyName: string | undefined;
    reportingPeriod: string | undefined;
  };

  /**
   * Generates one ExtendedStoredRequest type object
   * @param filterParameters contains all parameters that can be set. If companyName and/or reportingPeriod
   * is undefined, a random value will be chosen using faker
   * @returns the generated ExtendedStoredRequest
   */
  function generateExtendedStoredRequest(filterParameters: FilterParameters): ExtendedStoredRequest {
    return {
      id: crypto.randomUUID(),
      userId: crypto.randomUUID(),
      userEmailAddress: filterParameters.userEmailAddress,
      creationTimeStamp: Date.now(),
      dataType: filterParameters.framework,
      reportingPeriod: filterParameters.reportingPeriod ?? faker.helpers.arrayElement(['2020', '2021', '2022', '2023']),
      companyId: crypto.randomUUID(),
      companyName: filterParameters.companyName ?? faker.company.name(),
      lastModifiedDate: Date.now(),
      state: filterParameters.state,
      adminComment: filterParameters.adminComment,
      requestPriority: filterParameters.requestPriority,
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
  const companyNameAlpha = 'Fun company';
  const companyNameBeta = 'Funny company';
  const companyNameGamma = 'Serious company';
  const companyNameDelta = 'Neutral company';
  const companyNameSearchTerm = companyNameAlpha.substring(0, 3);
  const reportingPeriodAlpha = '2020';
  const reportingPeriodBeta = '2021';
  const reportingPeriodGamma = '2022';
  const reportingPeriodDelta = '2023';

  before(function () {
    mockRequests = [
      generateExtendedStoredRequest({
        userEmailAddress: mailAlpha,
        framework: DataTypeEnum.Lksg,
        state: RequestState.Open,
        adminComment: commentAlpha,
        requestPriority: RequestPriority.Urgent,
        companyName: companyNameAlpha,
        reportingPeriod: reportingPeriodAlpha,
      }),
      generateExtendedStoredRequest({
        userEmailAddress: mailBeta,
        framework: DataTypeEnum.EutaxonomyFinancials,
        state: RequestState.Processing,
        adminComment: commentBeta,
        requestPriority: RequestPriority.High,
        companyName: companyNameBeta,
        reportingPeriod: reportingPeriodBeta,
      }),
      generateExtendedStoredRequest({
        userEmailAddress: mailGamma,
        framework: DataTypeEnum.Vsme,
        state: RequestState.Processed,
        adminComment: commentGamma,
        requestPriority: RequestPriority.High,
        companyName: companyNameGamma,
        reportingPeriod: reportingPeriodGamma,
      }),
      generateExtendedStoredRequest({
        userEmailAddress: mailDelta,
        framework: DataTypeEnum.Sfdr,
        state: RequestState.Withdrawn,
        adminComment: commentDelta,
        requestPriority: RequestPriority.Low,
        companyName: companyNameDelta,
        reportingPeriod: reportingPeriodDelta,
      }),
    ];
    mockRequestsLarge = [];
    for (let num = 1; num <= 104; num++) {
      const dataType = faker.helpers.arrayElement([DataTypeEnum.Lksg, DataTypeEnum.Vsme]);
      const email = faker.helpers.arrayElement([mailAlpha, mailBeta, mailGamma, mailDelta]);
      const requestState = faker.helpers.arrayElement([
        RequestState.Open,
        RequestState.Processing,
        RequestState.Processed,
        RequestState.Withdrawn,
      ]);
      const comment = faker.helpers.arrayElement([commentAlpha, commentBeta, commentGamma, commentDelta]);
      const requestPriority = faker.helpers.arrayElement([
        RequestPriority.Low,
        RequestPriority.High,
        RequestPriority.Urgent,
        RequestPriority.Baseline,
      ]);
      mockRequestsLarge.push(
        generateExtendedStoredRequest({
          userEmailAddress: email,
          framework: dataType,
          state: requestState,
          adminComment: comment,
          requestPriority: requestPriority,
          companyName: undefined,
          reportingPeriod: undefined,
        })
      );
    }
  });

  /**
   * Sets up the interceptions for the unfiltered initial requests and number of requests
   */
  function setUpUnfilteredInterceptions(): void {
    cy.intercept('POST', '**/data-sourcing/requests/search**', (req) => {
      if (Object.keys(req.body).length === 0) {
        req.reply(mockRequests);
      }
    });
    cy.intercept('POST', '**/data-sourcing/requests/count', (req) => {
      if (Object.keys(req.body).length === 0) {
        req.reply(mockRequests.length.toString());
      }
    });
  }

  /**
   * Mounts the page and asserts that the unfiltered list of all data requests is displayed
   * @returns mounted component as Chainable
   */
  function mountAdminAllRequestsPageWithMocks(): Cypress.Chainable {
    setUpUnfilteredInterceptions();
    const mountedComponent = getMountingFunction({
      keycloak: minimalKeycloakMock({
        authenticated: true,
        roles: [KEYCLOAK_ROLE_ADMIN],
        userId: crypto.randomUUID(),
      }),
      router: router,
    })(AdminAllRequestsOverview);

    assertNumberOfSearchResults(mockRequests.length);
    for (const extendedStoredRequest of mockRequests) {
      if (extendedStoredRequest.userEmailAddress) {
        assertEmailAddressExistsInSearchResults(extendedStoredRequest.userEmailAddress);
      }
    }
    return mountedComponent;
  }

  /**
   *
   */
  function mountAdminAllRequestsPageWithManyMocks(): void {
    const expectedNumberOfRequests = mockRequestsLarge.length;
    cy.intercept('POST', '**/data-sourcing/requests/search**', (req) => {
      if (Object.keys(req.body).length === 0) {
        req.reply(mockRequestsLarge);
      }
    });
    cy.intercept('POST', '**/data-sourcing/requests/count', (req) => {
      if (Object.keys(req.body).length === 0) {
        req.reply(expectedNumberOfRequests.toString());
      }
    });

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
   * Validates if filtering via email address substring works as expected
   */
  function validateEmailAddressFilter(): void {
    const mockResponse = [mockRequests[0], mockRequests[3]];
    const expectedNumberOfRequests = mockResponse.length;
    cy.intercept('POST', '**/data-sourcing/requests/search**', (req) => {
      if (req.body.emailAddress === mailSearchTerm) {
        req.reply(mockResponse);
      }
    });
    cy.intercept('POST', '**/data-sourcing/requests/count', (req) => {
      if (req.body.emailAddress === mailSearchTerm) {
        req.reply(expectedNumberOfRequests.toString());
      }
    });

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
    const frameworkToFilterFor = DataTypeEnum.Sfdr;
    const frameworkHumanReadableName = humanizeStringOrNumber(frameworkToFilterFor);
    const mockResponse = [mockRequests[1]];
    const expectedNumberOfRequests = mockResponse.length;
    cy.intercept('POST', '**/data-sourcing/requests/search**', (req) => {
      if (Array.isArray(req.body.dataTypes) && req.body.dataTypes.includes(frameworkToFilterFor)) {
        req.reply(mockResponse);
      }
    });
    cy.intercept('POST', '**/data-sourcing/requests/count', (req) => {
      if (Array.isArray(req.body.dataTypes) && req.body.dataTypes.includes(frameworkToFilterFor)) {
        req.reply(expectedNumberOfRequests.toString());
      }
    });

    cy.get(`div[data-test="framework-picker"]`).click();
    cy.get(`.p-multiselect-overlay`).invoke('attr', 'style', 'position: relative; z-index: 1');
    cy.get(`li[aria-label="${frameworkHumanReadableName}"]`).click();
    cy.get(`button[data-test="trigger-filtering-requests"]`).click();

    assertNumberOfSearchResults(expectedNumberOfRequests);
    assertEmailAddressExistsInSearchResults(mailBeta);
  }

  /**
   * Validates if filtering via data request state dropdown filter works as expected
   */
  function validateRequestStateFilter(): void {
    const requestStateToFilterFor = RequestState.Open;
    const mockResponse = [mockRequests[0]];
    const expectedNumberOfRequests = mockResponse.length;
    cy.intercept('POST', '**/data-sourcing/requests/search**', (req) => {
      if (Array.isArray(req.body.requestStates) && req.body.requestStates.includes(requestStateToFilterFor)) {
        req.reply(mockResponse);
      }
    });
    cy.intercept('POST', '**/data-sourcing/requests/count', (req) => {
      if (Array.isArray(req.body.requestStates) && req.body.requestStates.includes(requestStateToFilterFor)) {
        req.reply(expectedNumberOfRequests.toString());
      }
    });

    cy.get(`div[data-test="request-state-picker"]`).click();
    cy.get(`.p-multiselect-overlay`).invoke('attr', 'style', 'position: relative; z-index: 1');
    cy.get(`li[aria-label="${requestStateToFilterFor}"]`).click();
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
    cy.intercept('POST', '**/data-sourcing/requests/search**', (req) => {
      if (Array.isArray(req.body.requestPriorities) && req.body.requestPriorities.includes(priorityToFilterFor)) {
        req.reply(mockResponse);
      }
    });
    cy.intercept('POST', '**/data-sourcing/requests/count', (req) => {
      if (Array.isArray(req.body.requestPriorities) && req.body.requestPriorities.includes(priorityToFilterFor)) {
        req.reply(expectedNumberOfRequests.toString());
      }
    });

    cy.get(`div[data-test="request-priority-picker"]`).click();
    cy.get(`.p-multiselect-overlay`).invoke('attr', 'style', 'position: relative; z-index: 1');
    cy.get(`li[aria-label="${priorityToFilterFor}"]`).click();
    cy.get(`button[data-test="trigger-filtering-requests"]`).click();

    assertNumberOfSearchResults(expectedNumberOfRequests);
    assertEmailAddressExistsInSearchResults(mailAlpha);
    assertEmailAddressExistsInSearchResults(mailBeta);
  }

  /**
   * Validates if filtering via reporting period dropdown filter works as expected
   */
  function validateReportingPeriodFilter(): void {
    const reportingPeriodToFilterFor = '2023';
    const mockResponse = [mockRequests[3]];
    const expectedNumberOfRequests = mockResponse.length;
    cy.intercept('POST', '**/data-sourcing/requests/search**', (req) => {
      if (Array.isArray(req.body.reportingPeriods) && req.body.reportingPeriods.includes(reportingPeriodToFilterFor)) {
        req.reply(mockResponse);
      }
    });
    cy.intercept('POST', '**/data-sourcing/requests/count', (req) => {
      if (Array.isArray(req.body.reportingPeriods) && req.body.reportingPeriods.includes(reportingPeriodToFilterFor)) {
        req.reply(expectedNumberOfRequests.toString());
      }
    });

    cy.get(`div[data-test="reporting-period-picker"]`).click();
    cy.get(`.p-multiselect-overlay`).invoke('attr', 'style', 'position: relative; z-index: 1');
    cy.get(`li[aria-label="${reportingPeriodToFilterFor}"]`).click();
    cy.get(`button[data-test="trigger-filtering-requests"]`).click();

    assertNumberOfSearchResults(expectedNumberOfRequests);
    assertEmailAddressExistsInSearchResults(mailDelta);
  }

  /**
   * Validates if filtering via admin comment substring works as expected
   */
  function validateAdminCommentFilter(): void {
    const mockResponse = [mockRequests[1], mockRequests[3]];
    const expectedNumberOfRequests = mockResponse.length;
    cy.intercept('POST', '**/data-sourcing/requests/search**', (req) => {
      if (req.body.adminComment === commentSearchTerm) {
        req.reply(mockResponse);
      }
    });
    cy.intercept('POST', '**/data-sourcing/requests/count', (req) => {
      if (req.body.adminComment === commentSearchTerm) {
        req.reply(expectedNumberOfRequests.toString());
      }
    });

    cy.get(`input[data-test="comment-searchbar"]`).type(commentSearchTerm);
    cy.get(`button[data-test="trigger-filtering-requests"]`).click();
    assertNumberOfSearchResults(expectedNumberOfRequests);
    assertEmailAddressExistsInSearchResults(mailBeta);
    assertEmailAddressExistsInSearchResults(mailDelta);
  }

  /**
   * Validates if filtering via company search string works as expected
   */
  function validateCompanySearchStringFilter(): void {
    const mockResponse = [mockRequests[0], mockRequests[1]];
    const expectedNumberOfRequests = mockResponse.length;
    cy.intercept('POST', '**/data-sourcing/requests/search**', (req) => {
      if (req.body.companySearchString === companyNameSearchTerm) {
        req.reply(mockResponse);
      }
    });
    cy.intercept('POST', '**/data-sourcing/requests/count', (req) => {
      if (req.body.companySearchString === companyNameSearchTerm) {
        req.reply(expectedNumberOfRequests.toString());
      }
    });

    cy.get(`input[data-test="company-search-string-searchbar"]`).type(companyNameSearchTerm);
    cy.get(`button[data-test="trigger-filtering-requests"]`).click();
    assertNumberOfSearchResults(expectedNumberOfRequests);
    assertEmailAddressExistsInSearchResults(mailAlpha);
    assertEmailAddressExistsInSearchResults(mailBeta);
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
    cy.intercept('POST', '**/data-sourcing/requests/search**', (req) => {
      if (
        Array.isArray(req.body.dataTypes) &&
        req.body.dataTypes.includes(frameworkToFilterFor) &&
        req.body.emailAddress === mailSearchTerm
      ) {
        req.reply(mockResponse);
      }
    });
    cy.intercept('POST', '**/data-sourcing/requests/count', (req) => {
      if (
        Array.isArray(req.body.dataTypes) &&
        req.body.dataTypes.includes(frameworkToFilterFor) &&
        req.body.emailAddress === mailSearchTerm
      ) {
        req.reply(expectedNumberOfRequests.toString());
      }
    });

    cy.get(`div[data-test="framework-picker"]`).click();
    cy.get(`.p-multiselect-overlay`).invoke('attr', 'style', 'position: relative; z-index: 1');
    cy.get(`li[aria-label="${frameworkHumanReadableName}"]`).click();
    cy.get(`button[data-test="trigger-filtering-requests"]`).click();
    assertNumberOfSearchResults(expectedNumberOfRequests);
    assertEmailAddressExistsInSearchResults(mailDelta);
  }

  /**
   * Removes the combined filter and checks if all requests are shown again
   */
  function validateDeselectingCombinedFilter(): void {
    setUpUnfilteredInterceptions();
    const frameworkHumanReadableName = humanizeStringOrNumber(DataTypeEnum.Sfdr);
    cy.get(`li[aria-label="${frameworkHumanReadableName}"]`).click();
    cy.get(`input[data-test="email-searchbar"]`).clear().type('{enter}');
    cy.get(`button[data-test="trigger-filtering-requests"]`).click();

    assertNumberOfSearchResults(mockRequests.length);
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
    cy.intercept(`**/data-sourcing/requests/search?chunkSize=${chunkSize}&chunkIndex=1`, secondPageMockResponse).as(
      'fetchRequests'
    );
    cy.intercept(`**/data-sourcing/requests/count`, expectedNumberOfRequests.toString());

    cy.get(`button[aria-label="Page 2"]`).first().click();

    cy.wait('@fetchRequests');
    assertNumberOfSearchResults(expectedNumberOfRows);
  }

  /**
   * Removes the combined filter via resetButton and checks if all requests are shown again
   */
  function validateResetButton(): void {
    setUpUnfilteredInterceptions();
    cy.get(`[data-test=reset-filter]`).click();

    assertNumberOfSearchResults(mockRequests.length);
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

  it('Filtering for request state works as expected', () => {
    mountAdminAllRequestsPageWithMocks();
    validateRequestStateFilter();
  });

  it('Filtering for request priority works as expected', () => {
    mountAdminAllRequestsPageWithMocks();
    validateRequestPriorityFilter();
  });

  it('Filtering for reporting period works as expected', () => {
    mountAdminAllRequestsPageWithMocks();
    validateReportingPeriodFilter();
  });

  it('Filtering for an admin comment works as expected', () => {
    mountAdminAllRequestsPageWithMocks();
    validateAdminCommentFilter();
  });

  it('Filtering for a company search string works as expected', () => {
    mountAdminAllRequestsPageWithMocks();
    validateCompanySearchStringFilter();
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
      const lastMockRequest = mockRequests.at(-1);
      const dataRequestIdOfLastElement = lastMockRequest!.id;
      cy.get('[data-test=requests-datatable]').within(() => {
        cy.get('tr:last').click();
      });

      cy.get('@routerPush').should('have.been.calledWith', `/requests/${dataRequestIdOfLastElement}`);
    });
  });
});
