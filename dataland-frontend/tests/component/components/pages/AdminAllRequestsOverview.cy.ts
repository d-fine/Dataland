import AdminAllRequestsOverview from '@/components/pages/AdminAllRequestsOverview.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { DataTypeEnum } from '@clients/backend';
import { KEYCLOAK_ROLE_ADMIN } from '@/utils/KeycloakUtils';
import { getMountingFunction } from '@ct/testUtils/Mount';
import { AccessStatus, type ExtendedStoredDataRequest, RequestStatus } from '@clients/communitymanager';
import { faker } from '@faker-js/faker';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';

describe('Component test for the admin-requests-overview page', () => {
  let mockRequests: ExtendedStoredDataRequest[];

  /**
   * Generates one ExtendedStoredDataRequest type object
   * @param userEmailAddress of the requesting user
   * @param framework for which data has been requested
   * @param requestStatus of the data request
   * @param accessStatus of the data request
   * @returns the generated ExtendedStoredDataRequest
   */
  function generateExtendedStoredDataRequest(
    userEmailAddress: string,
    framework: DataTypeEnum,
    requestStatus: RequestStatus,
    accessStatus: AccessStatus
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
    };
  }

  const mailAlpha = 'stephanie@fake.com';
  const mailSearchTerm = mailAlpha.substring(0, 3);
  const mailBeta = 'random@fake.com';
  const mailGamma = 'test123@fake.com';
  const mailDelta = 'steven@fake.com';

  before(function () {
    mockRequests = [
      generateExtendedStoredDataRequest(mailAlpha, DataTypeEnum.Lksg, RequestStatus.Open, AccessStatus.Public),
      generateExtendedStoredDataRequest(mailBeta, DataTypeEnum.P2p, RequestStatus.Answered, AccessStatus.Public),
      generateExtendedStoredDataRequest(mailGamma, DataTypeEnum.Vsme, RequestStatus.Answered, AccessStatus.Declined),
      generateExtendedStoredDataRequest(mailDelta, DataTypeEnum.Sfdr, RequestStatus.Resolved, AccessStatus.Public),
    ];
  });

  /**
   * Mounts the page and asserts that the unfiltered list of all data requests is displayed
   */
  function mountAdminAllRequestsPageWithMocks(): void {
    const expectedNumberOfRequests = mockRequests.length;
    cy.intercept('**/community/requests?chunkSize=100&chunkIndex=0', mockRequests).as('fetchInitialUnfilteredRequests');
    cy.intercept('**/community/requests/numberOfRequests', expectedNumberOfRequests.toString()).as(
      'fetchInitialUnfilteredNumberOfRequests'
    );
    getMountingFunction({
      keycloak: minimalKeycloakMock({
        authenticated: true,
        roles: [KEYCLOAK_ROLE_ADMIN],
        userId: crypto.randomUUID(),
      }),
    })(AdminAllRequestsOverview);
    assertNumberOfSearchResults(expectedNumberOfRequests);
    mockRequests.forEach((extendedStoredDataRequest) => {
      if (extendedStoredDataRequest.userEmailAddress) {
        assertEmailAddressExistsInSearchResults(extendedStoredDataRequest.userEmailAddress);
      }
    });
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
    const mockResposne = [mockRequests[0], mockRequests[3]];
    const expectedNumberOfRequests = mockResposne.length;
    cy.intercept(`**/community/requests?emailAddress=${mailSearchTerm}&chunkSize=100&chunkIndex=0`, mockResposne).as(
      'fetchEmailFilteredRequests'
    );
    cy.intercept(`**/community/requests/numberOfRequests?emailAddress=ste`, expectedNumberOfRequests.toString()).as(
      'fetchEmailFilteredNumberOfRequests'
    );

    cy.get(`input[data-test="email-searchbar"]`).type(mailSearchTerm).type('{enter}');
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
    cy.intercept(`**/community/requests?dataType=${frameworkToFilterFor}&chunkSize=100&chunkIndex=0`, mockResponse).as(
      'fetchFrameworkFilteredRequests'
    );
    cy.intercept(
      `**/community/requests/numberOfRequests?dataType=${frameworkToFilterFor}`,
      expectedNumberOfRequests.toString()
    ).as('fetchFrameworkFilteredNumberOfRequests');

    cy.get(`div[data-test="framework-picker"]`).click();
    cy.get(`li[aria-label="${frameworkHumanReadableName}"]`).click();

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
      `**/community/requests?requestStatus=${requestStatusToFilterFor}&chunkSize=100&chunkIndex=0`,
      mockResponse
    ).as('fetchRequestStatusFilteredRequests');
    cy.intercept(
      `**/community/requests/numberOfRequests?requestStatus=${requestStatusToFilterFor}`,
      expectedNumberOfRequests.toString()
    ).as('fetchRequestStatusFilteredNumberOfRequests');

    cy.get(`div[data-test="request-status-picker"]`).click();
    cy.get(`li[aria-label="${requestStatusToFilterFor}"]`).click();
    assertNumberOfSearchResults(expectedNumberOfRequests);
    assertEmailAddressExistsInSearchResults(mailAlpha);
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
      `**/community/requests?dataType=${frameworkToFilterFor}&emailAddress=${mailSearchTerm}&chunkSize=100&chunkIndex=0`,
      mockResponse
    ).as('fetchCombinedFilteredRequests');
    cy.intercept(
      `**/community/requests/numberOfRequests?dataType=sfdr&emailAddress=ste`,
      expectedNumberOfRequests.toString()
    ).as('fetchCombinedFilteredNumberOfRequests');

    cy.get(`div[data-test="framework-picker"]`).click();
    cy.get(`li[aria-label="${frameworkHumanReadableName}"]`).click();
    assertNumberOfSearchResults(expectedNumberOfRequests);
    assertEmailAddressExistsInSearchResults(mailDelta);
  }

  /**
   * Removes the combined filter and checks if all requests are shown again
   */
  function validateDeselectingCombinedFilter(): void {
    const expectedNumberOfRequests = mockRequests.length;
    cy.intercept('**/community/requests?chunkSize=100&chunkIndex=0', mockRequests).as('fetchInitialUnfilteredRequests');
    cy.intercept('**/community/requests/numberOfRequests', expectedNumberOfRequests.toString()).as(
      'fetchInitialUnfilteredNumberOfRequests'
    );
    const frameworkHumanReadableName = humanizeStringOrNumber(DataTypeEnum.Sfdr);
    cy.get(`li[aria-label="${frameworkHumanReadableName}"]`).click();
    cy.get(`input[data-test="email-searchbar"]`).clear().type('{enter}');

    assertNumberOfSearchResults(expectedNumberOfRequests);
    assertEmailAddressExistsInSearchResults(mailAlpha);
    assertEmailAddressExistsInSearchResults(mailDelta);
  }

  /**
   * Removes the combined filter via resetButton and checks if all requests are shown again
   */
  function validateResetButton(): void {
    const expectedNumberOfRequests = mockRequests.length;
    cy.intercept('**/community/requests?chunkSize=100&chunkIndex=0', mockRequests).as('fetchInitialUnfilteredRequests');
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
});
