import AdminAllRequestsOverview from '@/components/pages/AdminAllRequestsOverview.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { DataTypeEnum } from '@clients/backend';
import { KEYCLOAK_ROLE_ADMIN } from '@/utils/KeycloakUtils';
import { getMountingFunction } from '@ct/testUtils/Mount';
import { AccessStatus, type ExtendedStoredDataRequest, RequestStatus } from '@clients/communitymanager';
import { faker } from '@faker-js/faker';

describe('Component test for the admin-requests-overview page', () => {
  let mockRequests: ExtendedStoredDataRequest[];

  /**
   * Generates one ExtendedStoredDataRequest type object
   * @param userEmailAddress of the requesting user
   * @param framework for which data has been requested
   * @param requestStatus of the data request
   * @param accessStatus of the data request
   * @returns a mock company role assignment
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
    /* TODO Emanuel: passe den intercept exakt auf den korrekten call an (ohne wildcard am Ende)
    Der Call sollte defaultmäßig auch die ersten 100 gehen => sicherstellen in interception
     */
    cy.intercept('**/community/requests?**', { body: mockRequests, times: 1 }).as('fetchInitialUnfilteredRequests');
    getMountingFunction({
      keycloak: minimalKeycloakMock({
        authenticated: true,
        roles: [KEYCLOAK_ROLE_ADMIN],
        userId: crypto.randomUUID(),
      }),
    })(AdminAllRequestsOverview, undefined);
    assertNumberOfSearchResults(mockRequests.length);
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
    /* TODO Emanuel: passe den intercept exakt auf den korrekten call an
    Der Call sollte nach emails mit dem search term filtern => sicherstellen in interception
     */
    cy.intercept('**/community/requests?**', [mockRequests[0], mockRequests[3]]).as('fetchEmailFilteredRequests');

    const searchTerm = mailAlpha.substring(0, 3);
    cy.get(`input[data-test="requested-Datasets-searchbar"]`).type(searchTerm).type('{enter}');

    assertNumberOfSearchResults(2);
    assertEmailAddressExistsInSearchResults(mailAlpha);
    assertEmailAddressExistsInSearchResults(mailDelta);
  }

  /**
   * Validates if filtering via framework dropdown filter works as expected
   */
  function validateFrameworkFilter(): void {
    /* TODO Emanuel: passe den intercept exakt auf den korrekten call an
    Der Call sollte nach request mit p2p filtern => sicherstellen in interception
     */
    cy.intercept('**/community/requests?**', [mockRequests[1]]).as('fetchFrameworkFilteredRequests');

    // TODO select framework dropdown and select p2p
    assertNumberOfSearchResults(1);
    assertEmailAddressExistsInSearchResults(mailBeta);
  }

  /**
   * Validates if filtering via data request status dropdown filter works as expected
   */
  function validateRequestStatusFilter(): void {
    /* TODO Emanuel: passe den intercept exakt auf den korrekten call an
    Der Call sollte nach request mit dem request status "open" filtern => sicherstellen in interception
     */
    cy.intercept('**/community/requests?**', [mockRequests[0]]).as('fetchRequestStatusFilteredRequests');

    // TODO select request status dropdown "Open"
    assertNumberOfSearchResults(1);
    assertEmailAddressExistsInSearchResults(mailAlpha);
  }

  /**
   * Validates if combining two filters leads to a combined filter query
   */
  function validateCombinedFilter(): void {
    validateEmailAddressFilter();

    /* TODO Emanuel: passe den intercept exakt auf den korrekten call an
    Der Call sollte SOWOHL nach mails, als auch nach sfdr filtern => sicherstellen in interception
     */
    cy.intercept('**/community/requests?**', [mockRequests[3]]).as('fetchCombinedFilteredRequests');
    // TODO additionally filter on framework sfdr

    assertNumberOfSearchResults(1);
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

  it('A combined filter works as expected', () => {
    mountAdminAllRequestsPageWithMocks();
    validateCombinedFilter();
  });
});
