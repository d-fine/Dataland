import AdminAllRequestsOverview from '@/components/pages/AdminAllRequestsOverview.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { DataTypeEnum } from '@clients/backend';
import { getMountingFunction } from '@ct/testUtils/Mount';
import {
  type DataSourcingEnhancedRequest,
  DataSourcingState,
  RequestPriority,
  RequestState,
} from '@clients/datasourcingservice';
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

/**
 * Selects an option from a dropdown filter and triggers the filter request.
 * @param pickerDataTest the data-test attribute of the dropdown picker
 * @param label the aria-label of the option to select
 */
function selectFromDropdownAndFilter(pickerDataTest: string, label: string): void {
  cy.get(`div[data-test="${pickerDataTest}"]`).click();
  cy.get('.p-multiselect-overlay').invoke('attr', 'style', 'position: relative; z-index: 1');
  cy.get(`li[aria-label="${label}"]`).click();
  cy.get('button[data-test="trigger-filtering-requests"]').click();
}

describe('Component test for the admin-requests-overview page', () => {
  let mockRequests: DataSourcingEnhancedRequest[];
  let mockRequestsLarge: DataSourcingEnhancedRequest[];
  const chunkSize = 100;

  /**
   * Creates a keycloak mock configured for an authenticated admin user.
   * @returns keycloak mock instance
   */
  const adminKeycloakMock = (): ReturnType<typeof minimalKeycloakMock> =>
    minimalKeycloakMock({
      authenticated: true,
      roles: [KEYCLOAK_ROLE_ADMIN],
      userId: crypto.randomUUID(),
    });

  /**
   * Sets up intercepts for both search and count endpoints with a custom body matcher.
   * @param mockResponse the mock data to return when the matcher succeeds
   * @param bodyMatcher predicate function to match request body
   */
  function setupFilterIntercepts(
    mockResponse: DataSourcingEnhancedRequest[],
    bodyMatcher: (body: Record<string, unknown>) => boolean
  ): void {
    cy.intercept('POST', '**/data-sourcing/enhanced-requests/search**', (req) => {
      if (bodyMatcher(req.body)) req.reply(mockResponse);
    });
    cy.intercept('POST', '**/data-sourcing/enhanced-requests/search/count', (req) => {
      if (bodyMatcher(req.body)) req.reply(mockResponse.length.toString());
    });
  }

  /**
   * A collection of fields of extended stored data requests that can be filtered by.
   */
  type FilterParameters = {
    userEmailAddress: string;
    framework: DataTypeEnum;
    state: RequestState;
    dataSourcingState?: DataSourcingState;
    documentCollectorName?: string;
    dataExtractorName?: string;
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
  function generateExtendedStoredRequest(filterParameters: FilterParameters): DataSourcingEnhancedRequest {
    return {
      id: crypto.randomUUID(),
      userId: crypto.randomUUID(),
      userEmailAddress: filterParameters.userEmailAddress,
      creationTimestamp: Date.now(),
      dataType: filterParameters.framework,
      reportingPeriod: filterParameters.reportingPeriod ?? faker.helpers.arrayElement(['2020', '2021', '2022', '2023']),
      companyId: crypto.randomUUID(),
      companyName: filterParameters.companyName ?? faker.company.name(),
      lastModifiedDate: Date.now(),
      state: filterParameters.state,
      dataSourcingDetails: {
        dataSourcingEntityId: '12345678-1234-1234-1234-123456789012',
        dataSourcingState: filterParameters.dataSourcingState,
        dateOfNextDocumentSourcingAttempt: '2024-01-01T00:00:00Z',
        documentCollectorName: filterParameters.documentCollectorName ?? 'Default Document Collector',
        dataExtractorName: filterParameters.dataExtractorName ?? 'Default Data Extractor',
      },
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
        dataSourcingState: DataSourcingState.DataExtraction,
        documentCollectorName: 'Document Collector Alpha',
        dataExtractorName: 'Data Extractor Alpha',
        adminComment: commentAlpha,
        requestPriority: RequestPriority.Urgent,
        companyName: companyNameAlpha,
        reportingPeriod: reportingPeriodAlpha,
      }),
      generateExtendedStoredRequest({
        userEmailAddress: mailBeta,
        framework: DataTypeEnum.EutaxonomyFinancials,
        state: RequestState.Processing,
        dataSourcingState: DataSourcingState.Initialized,
        adminComment: commentBeta,
        requestPriority: RequestPriority.High,
        companyName: companyNameBeta,
        reportingPeriod: reportingPeriodBeta,
      }),
      generateExtendedStoredRequest({
        userEmailAddress: mailGamma,
        framework: DataTypeEnum.Vsme,
        state: RequestState.Processed,
        dataSourcingState: DataSourcingState.DataExtraction,
        adminComment: commentGamma,
        requestPriority: RequestPriority.High,
        companyName: companyNameGamma,
        reportingPeriod: reportingPeriodGamma,
      }),
      generateExtendedStoredRequest({
        userEmailAddress: mailDelta,
        framework: DataTypeEnum.Sfdr,
        state: RequestState.Withdrawn,
        dataSourcingState: DataSourcingState.DataExtraction,
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
    cy.intercept('POST', '**/data-sourcing/enhanced-requests/search**', (req) => {
      if (Object.keys(req.body).length === 0) {
        req.reply(mockRequests);
      }
    });
    cy.intercept('POST', '**/data-sourcing/enhanced-requests/search/count', (req) => {
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
      keycloak: adminKeycloakMock(),
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
   * Mounts the page with a large mock dataset for pagination testing.
   */
  function mountAdminAllRequestsPageWithManyMocks(): void {
    setupFilterIntercepts(mockRequestsLarge, (body) => Object.keys(body).length === 0);
    getMountingFunction({ keycloak: adminKeycloakMock() })(AdminAllRequestsOverview);
    assertNumberOfSearchResults(chunkSize);
  }

  /**
   * Validates if filtering via email address substring works as expected.
   */
  function validateEmailAddressFilter(): void {
    const mockResponse = [mockRequests[0]!, mockRequests[3]!];
    setupFilterIntercepts(mockResponse, (body) => body.emailAddress === mailSearchTerm);

    cy.get('input[data-test="email-searchbar"]').type(mailSearchTerm);
    cy.get('button[data-test="trigger-filtering-requests"]').click();
    assertNumberOfSearchResults(mockResponse.length);
    assertEmailAddressExistsInSearchResults(mailAlpha);
    assertEmailAddressExistsInSearchResults(mailDelta);
  }

  /**
   * Validates if filtering via framework dropdown filter works as expected.
   */
  function validateFrameworkFilter(): void {
    const frameworkToFilterFor = DataTypeEnum.Sfdr;
    const mockResponse = [mockRequests[1]!];
    setupFilterIntercepts(
      mockResponse,
      (body) => Array.isArray(body.dataTypes) && body.dataTypes.includes(frameworkToFilterFor)
    );

    selectFromDropdownAndFilter('framework-picker', humanizeStringOrNumber(frameworkToFilterFor));
    assertNumberOfSearchResults(mockResponse.length);
    assertEmailAddressExistsInSearchResults(mailBeta);
  }

  /**
   * Validates if filtering via status dropdown filter works as expected for RequestState (Open).
   */
  function validateStatusFilterForRequestState(): void {
    const mockResponse = [mockRequests[0]!];
    setupFilterIntercepts(
      mockResponse,
      (body) => Array.isArray(body.requestStates) && body.requestStates.includes(RequestState.Open)
    );

    selectFromDropdownAndFilter('state-picker', 'Open');
    assertNumberOfSearchResults(mockResponse.length);
    assertEmailAddressExistsInSearchResults(mailAlpha);
  }

  /**
   * Validates if filtering via status dropdown filter works as expected for DataSourcingState (Validated).
   */
  function validateStatusFilterForDataSourcingState(): void {
    const mockResponse = [mockRequests[1]!];
    setupFilterIntercepts(
      mockResponse,
      (body) =>
        Array.isArray(body.dataSourcingStates) && body.dataSourcingStates.includes(DataSourcingState.Initialized)
    );

    selectFromDropdownAndFilter('state-picker', 'Validated');
    assertNumberOfSearchResults(mockResponse.length);
    assertEmailAddressExistsInSearchResults(mailBeta);
  }

  /**
   * Validates if filtering via priority dropdown filter works as expected.
   */
  function validateRequestPriorityFilter(): void {
    const mockResponse = [mockRequests[0]!, mockRequests[1]!];
    setupFilterIntercepts(
      mockResponse,
      (body) => Array.isArray(body.requestPriorities) && body.requestPriorities.includes(RequestPriority.Urgent)
    );

    selectFromDropdownAndFilter('request-priority-picker', RequestPriority.Urgent);
    assertNumberOfSearchResults(mockResponse.length);
    assertEmailAddressExistsInSearchResults(mailAlpha);
    assertEmailAddressExistsInSearchResults(mailBeta);
  }

  /**
   * Validates if filtering via reporting period dropdown filter works as expected.
   */
  function validateReportingPeriodFilter(): void {
    const reportingPeriodToFilterFor = '2023';
    const mockResponse = [mockRequests[3]!];
    setupFilterIntercepts(
      mockResponse,
      (body) => Array.isArray(body.reportingPeriods) && body.reportingPeriods.includes(reportingPeriodToFilterFor)
    );

    selectFromDropdownAndFilter('reporting-period-picker', reportingPeriodToFilterFor);
    assertNumberOfSearchResults(mockResponse.length);
    assertEmailAddressExistsInSearchResults(mailDelta);
  }

  /**
   * Validates if filtering via admin comment substring works as expected.
   */
  function validateAdminCommentFilter(): void {
    const mockResponse = [mockRequests[1]!, mockRequests[3]!];
    setupFilterIntercepts(mockResponse, (body) => body.adminComment === commentSearchTerm);

    cy.get('input[data-test="comment-searchbar"]').type(commentSearchTerm);
    cy.get('button[data-test="trigger-filtering-requests"]').click();
    assertNumberOfSearchResults(mockResponse.length);
    assertEmailAddressExistsInSearchResults(mailBeta);
    assertEmailAddressExistsInSearchResults(mailDelta);
  }

  /**
   * Validates if filtering via company search string works as expected.
   */
  function validateCompanySearchStringFilter(): void {
    const mockResponse = [mockRequests[0]!, mockRequests[1]!];
    setupFilterIntercepts(mockResponse, (body) => body.companySearchString === companyNameSearchTerm);

    cy.get('input[data-test="company-search-string-searchbar"]').type(companyNameSearchTerm);
    cy.get('button[data-test="trigger-filtering-requests"]').click();
    assertNumberOfSearchResults(mockResponse.length);
    assertEmailAddressExistsInSearchResults(mailAlpha);
    assertEmailAddressExistsInSearchResults(mailBeta);
  }

  /**
   * Validates if combining two filters leads to a combined filter query.
   */
  function validateCombinedFilter(): void {
    validateEmailAddressFilter();
    const frameworkToFilterFor = DataTypeEnum.Sfdr;
    const mockResponse = [mockRequests[3]!];
    setupFilterIntercepts(
      mockResponse,
      (body) =>
        Array.isArray(body.dataTypes) &&
        body.dataTypes.includes(frameworkToFilterFor) &&
        body.emailAddress === mailSearchTerm
    );

    selectFromDropdownAndFilter('framework-picker', humanizeStringOrNumber(frameworkToFilterFor));
    assertNumberOfSearchResults(mockResponse.length);
    assertEmailAddressExistsInSearchResults(mailDelta);
  }

  /**
   * Removes the combined filter and checks if all requests are shown again.
   */
  function validateDeselectingCombinedFilter(): void {
    setUpUnfilteredInterceptions();
    cy.get(`li[aria-label="${humanizeStringOrNumber(DataTypeEnum.Sfdr)}"]`).click();
    cy.get('input[data-test="email-searchbar"]').clear().type('{enter}');
    cy.get('button[data-test="trigger-filtering-requests"]').click();

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
    cy.intercept(
      `**/data-sourcing/enhanced-requests/search?chunkSize=${chunkSize}&chunkIndex=1`,
      secondPageMockResponse
    ).as('fetchRequests');
    cy.intercept(`**/data-sourcing/enhanced-requests/search/count`, expectedNumberOfRequests.toString());

    cy.get(`button[aria-label="Page 2"]`).first().click();

    cy.wait('@fetchRequests');
    assertNumberOfSearchResults(expectedNumberOfRows);
  }

  /**
   * Removes the combined filter via reset button and checks if all requests are shown again.
   */
  function validateResetButton(): void {
    setUpUnfilteredInterceptions();
    cy.get('[data-test=reset-filter]').click();

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

  it('Filtering for status with request state (Open) works as expected', () => {
    mountAdminAllRequestsPageWithMocks();
    validateStatusFilterForRequestState();
  });

  it('Filtering for status with data sourcing state (Validated) works as expected', () => {
    mountAdminAllRequestsPageWithMocks();
    validateStatusFilterForDataSourcingState();
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

  it('Check existence and entries of Data Extractor column including fallback', () => {
    mountAdminAllRequestsPageWithMocks();
    cy.contains('th', 'DATA EXTRACTOR');
    cy.contains('td', 'Data Extractor Alpha');
    cy.get('td').contains('-').should('exist');
  });

  it('Check existence and entries of Document collector column including fallback', () => {
    mountAdminAllRequestsPageWithMocks();
    cy.contains('th', 'DOCUMENT COLLECTOR');
    cy.contains('td', 'Document Collector Alpha');
    cy.get('td').contains('-').should('exist');
  });

  it('Check existence and entries of STATE column with mixed state labels', () => {
    mountAdminAllRequestsPageWithMocks();
    cy.contains('th', 'STATE');
    cy.contains('td', 'Open');
    cy.contains('td', 'Data Extraction');
    cy.contains('td', 'Validated');
  });

  it('Check existence and entries of NEXT SOURCING ATTEMPT column', () => {
    mountAdminAllRequestsPageWithMocks();
    cy.contains('th', 'NEXT SOURCING ATTEMPT');
    cy.contains('td', '1 Jan 2024');
  });

  describe('Column selector functionality', () => {
    const COLUMN_SELECTION_STORAGE_KEY = 'adminAllRequestsOverview.selectedColumns';

    beforeEach(() => {
      localStorage.removeItem(COLUMN_SELECTION_STORAGE_KEY);
    });

    it('opens the column selector popover when clicking the gear icon', () => {
      mountAdminAllRequestsPageWithMocks();
      cy.get('.column-selector-container').first().click();
      cy.get('[data-test="column-selector-popover"]').should('be.visible');
      cy.get('[data-test="column-selector-popover"]').within(() => {
        cy.contains('label', 'Requester').should('exist');
        cy.contains('label', 'Company').should('exist');
        cy.contains('label', 'Framework').should('exist');
      });
    });

    it('hides a column when unchecking it in the column selector', () => {
      mountAdminAllRequestsPageWithMocks();
      cy.contains('th', 'REQUEST ID').should('exist');

      cy.get('.column-selector-container').first().click();
      cy.get('[data-test="column-checkbox-id"]').click();
      cy.get('body').click(0, 0);

      cy.contains('th', 'REQUEST ID').should('not.exist');
    });

    it('shows a column when checking it in the column selector after it was hidden', () => {
      mountAdminAllRequestsPageWithMocks();

      cy.get('.column-selector-container').first().click();
      cy.get('[data-test="column-checkbox-requester"]').click();
      cy.get('body').click(0, 0);
      cy.contains('th', 'REQUESTER').should('not.exist');

      cy.get('.column-selector-container').first().click();
      cy.get('[data-test="column-checkbox-requester"]').click();
      cy.get('body').click(0, 0);
      cy.contains('th', 'REQUESTER').should('exist');
    });

    it('persists column selection to localStorage', () => {
      mountAdminAllRequestsPageWithMocks();

      cy.get('.column-selector-container').first().click();
      cy.get('[data-test="column-checkbox-adminComment"]').click();
      cy.get('body').click(0, 0);

      cy.then(() => {
        const saved = localStorage.getItem(COLUMN_SELECTION_STORAGE_KEY);
        expect(saved).to.not.be.null;
        const savedFields: string[] = JSON.parse(saved!);
        expect(savedFields).to.not.include('adminComment');
      });
    });

    it('loads column selection from localStorage on mount', () => {
      const savedSelection = ['requester', 'company', 'state'];
      localStorage.setItem(COLUMN_SELECTION_STORAGE_KEY, JSON.stringify(savedSelection));

      mountAdminAllRequestsPageWithMocks();

      cy.contains('th', 'REQUESTER').should('exist');
      cy.contains('th', 'COMPANY').should('exist');
      cy.contains('th', 'STATE').should('exist');
      cy.contains('th', 'FRAMEWORK').should('not.exist');
      cy.contains('th', 'REQUEST ID').should('not.exist');
    });
  });

  describe('Data fallback handling', () => {
    it('displays dash for missing dataSourcingDetails fields', () => {
      const requestWithoutDetails: DataSourcingEnhancedRequest = {
        id: crypto.randomUUID(),
        userId: crypto.randomUUID(),
        userEmailAddress: 'nodetails@test.com',
        creationTimestamp: Date.now(),
        dataType: DataTypeEnum.Lksg,
        reportingPeriod: '2023',
        companyId: crypto.randomUUID(),
        companyName: 'No Details Company',
        lastModifiedDate: Date.now(),
        state: RequestState.Open,
        dataSourcingDetails: undefined,
        adminComment: 'Test comment',
        requestPriority: RequestPriority.Low,
      };

      cy.intercept('POST', '**/data-sourcing/enhanced-requests/search**', [requestWithoutDetails]);
      cy.intercept('POST', '**/data-sourcing/enhanced-requests/search/count', '1');

      getMountingFunction({
        keycloak: adminKeycloakMock(),
        router: router,
      })(AdminAllRequestsOverview);

      assertNumberOfSearchResults(1);
      cy.get('td').filter(':contains("-")').should('have.length.at.least', 3);
    });
  });

  describe('Error handling', () => {
    it('handles API error gracefully', () => {
      cy.intercept('POST', '**/data-sourcing/enhanced-requests/search**', {
        statusCode: 500,
        body: 'Internal Server Error',
      });
      cy.intercept('POST', '**/data-sourcing/enhanced-requests/search/count', {
        statusCode: 500,
        body: 'Internal Server Error',
      });

      cy.window().then((win) => {
        cy.spy(win.console, 'error').as('consoleError');
      });

      getMountingFunction({
        keycloak: adminKeycloakMock(),
        router: router,
      })(AdminAllRequestsOverview);

      cy.get('@consoleError').should('have.been.called');
    });
  });
});
