import CompanyCockpitPage from '@/components/pages/CompanyCockpitPage.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import {
  type AggregatedFrameworkDataSummary,
  type CompanyInformation,
  DataTypeEnum,
  type LksgData,
} from '@clients/backend';
import { type FixtureData } from '@sharedUtils/Fixtures';
import { setMobileDeviceViewport } from '@sharedUtils/TestSetupUtils';
import { computed, ref } from 'vue';
import { CompanyRole, type CompanyRoleAssignmentExtended } from '@clients/communitymanager';
import { getMountingFunction } from '@ct/testUtils/Mount';
import {
  KEYCLOAK_ROLE_ADMIN,
  KEYCLOAK_ROLE_PREMIUM_USER,
  KEYCLOAK_ROLE_UPLOADER,
  KEYCLOAK_ROLE_USER,
  KEYCLOAK_ROLES,
} from '@/utils/KeycloakRoles';
import { DocumentMetaInfoDocumentCategoryEnum } from '@clients/documentmanager';

describe('Component test for the company cockpit', () => {
  let companyInformationForTest: CompanyInformation;
  let mockMapOfDataTypeToAggregatedFrameworkDataSummary: Map<DataTypeEnum, AggregatedFrameworkDataSummary>;
  const dummyCompanyId = '550e8400-e29b-11d4-a716-446655440000';
  const dummyDocumentIds = [
    'a12d2cd3014c2601e6d3e32a7f0ec92fe3e5a9a8519519d93b8bb7c56141849d',
    'e0dfbaf044f44cacbb304a4686d890205a9f1acc493a4eb290ca355fb9e56dcf',
    'e30cf2dfd5ef4a6fb347bcca75e79f316f7e79399139462491f31baf1fdfe4f7',
  ];
  const dummyPublicationDates = ['2025-02-25', '2024-01-13', undefined];
  const dummyReportingPeriods = ['2025', '2024', '2023'];
  const dummyUserId = 'mock-user-id';
  const dummyFirstName = 'mock-first-name';
  const dummyLastName = 'mock-last-name';
  const dummyEmail = 'mock@Company.com';
  const initiallyDisplayedFrameworks: Set<DataTypeEnum> = new Set([
    DataTypeEnum.EutaxonomyFinancials,
    DataTypeEnum.EutaxonomyNonFinancials,
    DataTypeEnum.NuclearAndGas,
    DataTypeEnum.Sfdr,
  ]);
  let allFrameworks: Set<DataTypeEnum>;

  before(function () {
    cy.clearLocalStorage();
    cy.fixture('CompanyInformationWithLksgData').then(function (jsonContent) {
      const lksgFixtures = jsonContent as Array<FixtureData<LksgData>>;
      companyInformationForTest = lksgFixtures[0].companyInformation;
    });
    cy.fixture('MapOfFrameworkNameToAggregatedFrameworkDataSummaryMock').then(function (jsonContent) {
      mockMapOfDataTypeToAggregatedFrameworkDataSummary = jsonContent as Map<
        DataTypeEnum,
        AggregatedFrameworkDataSummary
      >;
      allFrameworks = new Set(Object.keys(mockMapOfDataTypeToAggregatedFrameworkDataSummary)) as Set<DataTypeEnum>;
    });
  });

  /**
   * Generates a company role assignment
   * @param companyRole in the mock assignment
   * @param companyId of the company associated with the mock assignment
   * @returns a mock company role assignment
   */
  function generateCompanyRoleAssignment(companyRole: CompanyRole, companyId: string): CompanyRoleAssignmentExtended {
    return {
      companyRole: companyRole,
      companyId: companyId,
      userId: dummyUserId,
      firstName: dummyFirstName,
      email: dummyEmail,
    };
  }

  /**
   * Generates a dummy document metainformation in response to an HTTP query.
   * @param index ranges inclusively from 0 to 2
   * @param query the associated HTTP query
   */
  function generateDocumentMetaInformation(
    index: number,
    query: Record<string, string | number>
  ): {
    documentId: string;
    documentName: string;
    documentCategory: string;
    companyIds: string[];
    publicationDate: string | undefined;
    reportingPeriod: string;
  } {
    return {
      documentId: dummyDocumentIds[index],
      documentName: 'test_' + (query['documentCategories'] ?? 'document') + `_${index + 1}`,
      documentCategory: (query['documentCategories'] as string) ?? 'AnnualReport',
      companyIds: [(query['companyId'] as string) ?? '???'],
      publicationDate: dummyPublicationDates[index],
      reportingPeriod: dummyReportingPeriods[index],
    };
  }

  /**
   * Mocks the requests that happen when the company cockpit page is being mounted
   * @param hasCompanyAtLeastOneOwner has the company at least one company owner
   */
  function mockRequestsOnMounted(hasCompanyAtLeastOneOwner: boolean): void {
    cy.intercept(`**/api/companies/*/info`, {
      body: companyInformationForTest,
      times: 1,
    }).as('fetchCompanyInfo');
    cy.intercept('**/api/companies/*/aggregated-framework-data-summary', {
      body: mockMapOfDataTypeToAggregatedFrameworkDataSummary,
      times: 1,
    }).as('fetchAggregatedFrameworkMetaInfo');
    const hasCompanyAtLeastOneOwnerStatusCode = hasCompanyAtLeastOneOwner ? 200 : 404;
    cy.intercept('**/community/company-ownership/*', {
      statusCode: hasCompanyAtLeastOneOwnerStatusCode,
    }).as('fetchCompanyOwnershipExistence');
    cy.intercept('HEAD', `/community/company-role-assignments/CompanyOwner/${dummyCompanyId}/${dummyUserId}`, {
      statusCode: 200,
    }).as('checkUserCompanyOwnerRole');
    cy.intercept('GET', '**/documents/**', (request) => {
      request.reply({
        statusCode: 200,
        body: [
          generateDocumentMetaInformation(0, request.query),
          generateDocumentMetaInformation(1, request.query),
          generateDocumentMetaInformation(2, request.query),
        ],
      });
    }).as('fetchDocumentMetadata');
  }

  /**
   * Mounts the company cockpit page with a specific authentication
   * @param isLoggedIn determines if the mount shall happen from a logged-in users perspective
   * @param isMobile determines if the mount shall happen from a mobile-users perspective
   * @param keycloakRoles defines the keycloak roles of the user if the mount happens from a logged-in users perspective
   * @param companyRoleAssignments defines the company role assignments that the current user shall have
   * @returns the mounted component
   */
  function mountCompanyCockpitWithAuthentication(
    isLoggedIn: boolean,
    isMobile: boolean,
    keycloakRoles?: string[],
    companyRoleAssignments?: CompanyRoleAssignmentExtended[]
  ): Cypress.Chainable {
    const chainable = getMountingFunction({
      keycloak: minimalKeycloakMock({
        authenticated: isLoggedIn,
        roles: keycloakRoles,
        userId: dummyUserId,
      }),
    })(CompanyCockpitPage, {
      global: {
        provide: {
          useMobileView: computed((): boolean => isMobile),
          companyRoleAssignments: ref(companyRoleAssignments),
        },
      },
      props: {
        companyId: dummyCompanyId,
      },
    });
    cy.wait('@fetchCompanyInfo');
    cy.wait('@fetchAggregatedFrameworkMetaInfo');
    cy.wait('@fetchCompanyOwnershipExistence');
    return chainable;
  }

  /**
   * Validates the existence of the back-button
   */
  function validateBackButtonExistence(): void {
    const backButtonSelector = `[data-test="back-button"]`;
    cy.get(backButtonSelector).should('exist');
  }

  /**
   * Validates the existence of the company search bar
   * @param isSearchBarExpected determines if the existence of the search bar is expected
   */
  function validateSearchBarExistence(isSearchBarExpected: boolean): void {
    const searchBarSelector = 'input[type="text"]#company_search_bar_standard';
    cy.get(searchBarSelector).should(isSearchBarExpected ? 'exist' : 'not.exist');
  }

  /**
   * Validates the existence of the banner that shows info about the company
   * @param hasCompanyCompanyOwner has the mocked company at least one company owner?
   */
  function validateCompanyInformationBanner(hasCompanyCompanyOwner?: boolean): void {
    cy.contains('h1', companyInformationForTest.companyName);
    cy.get("[data-test='verifiedCompanyOwnerBadge']").should(hasCompanyCompanyOwner ? 'exist' : 'not.exist');
  }

  /**
   * Validates the existence of the panel that shows the offer to claim company ownership
   * @param isThisExpected is this panel expected
   */
  function validateClaimOwnershipPanel(isThisExpected: boolean): void {
    cy.get("[data-test='claimOwnershipPanelLink']").should(isThisExpected ? 'exist' : 'not.exist');
  }

  /**
   * Validates the vsme framework summary panel
   * @param isUserCompanyOwner is the current user company owner
   */
  function validateVsmeFrameworkSummaryPanel(isUserCompanyOwner: boolean): void {
    const frameworkName = 'vsme';
    const frameworkSummaryPanelSelector = `div[data-test="${frameworkName}-summary-panel"]`;
    if (isUserCompanyOwner) {
      cy.get(`${frameworkSummaryPanelSelector} [data-test="${frameworkName}-provide-data-button"]`).should('exist');
    } else {
      cy.get(`${frameworkSummaryPanelSelector} [data-test="${frameworkName}-provide-data-button"]`).should('not.exist');
    }
  }

  /**
   * Validates the framework summary panels by asserting their existence and checking for their contents
   * @param isProvideDataButtonExpected determines if a provide-data-button is expected to be found in the panels
   * @param isMobileViewActive determines whether the company cockpit page is in mobile view mode
   * @param frameworksToTest the frameworks that are tested for
   * @param isCompanyOwner is the current user company owner
   */
  function validateDisplayedFrameworkSummaryPanels(
    isProvideDataButtonExpected: boolean,
    isMobileViewActive: boolean,
    frameworksToTest: Set<DataTypeEnum>,
    isCompanyOwner: boolean = false
  ): void {
    frameworksToTest.forEach((frameworkName: DataTypeEnum) => {
      const frameworkSummaryPanelSelector = `div[data-test="${frameworkName}-summary-panel"]`;
      const frameworkDataSummary = new Map(Object.entries(mockMapOfDataTypeToAggregatedFrameworkDataSummary)).get(
        frameworkName
      );
      if (frameworkDataSummary === undefined) {
        throw new Error(frameworkDataSummary + ' missing in mockMapOfDataTypeToAggregatedFrameworkDataSummary.');
      }
      cy.get(frameworkSummaryPanelSelector).scrollIntoView();
      cy.get(frameworkSummaryPanelSelector).should('exist');
      cy.get(`${frameworkSummaryPanelSelector} span[data-test="${frameworkName}-panel-value"]`).should(
        'contain',
        frameworkDataSummary.numberOfProvidedReportingPeriods.toString()
      );
      if (frameworkDataSummary.numberOfProvidedReportingPeriods > 0 && !isMobileViewActive) {
        cy.get(`[data-test="${frameworkName}-view-data-button"]`).should('exist');
      } else {
        cy.get(`[data-test="${frameworkName}-view-data-button"]`).should('not.exist');
      }
      if (frameworkName == 'vsme') {
        validateVsmeFrameworkSummaryPanel(isCompanyOwner);
        return;
      }
      if (isProvideDataButtonExpected) {
        if (frameworkName != 'lksg') {
          cy.get(`${frameworkSummaryPanelSelector} [data-test="${frameworkName}-provide-data-button"]`).should('exist');
        }
      } else {
        cy.get(`${frameworkSummaryPanelSelector} [data-test="${frameworkName}-provide-data-button"]`).should(
          'not.exist'
        );
      }
    });
  }

  /**
   * Validates the existence or non-existence of the single data request button
   * @param isButtonExpected self explanatory
   */
  function validateSingleDataRequestButton(isButtonExpected: boolean): void {
    cy.get('[data-test="singleDataRequestButton"]').should(isButtonExpected ? 'exist' : 'not.exist');
  }

  /**
   * Validates whether the displayed framework summary panels are as expected before, during and
   * after extension from the initially displayed four panels to all.
   */
  function validateFrameworkSummaryPanels(
    isProvideDataButtonExpected: boolean,
    isMobileViewActive: boolean = false,
    isCompanyOwner: boolean = false
  ): void {
    validateDisplayedFrameworkSummaryPanels(
      isProvideDataButtonExpected,
      isMobileViewActive,
      initiallyDisplayedFrameworks,
      isCompanyOwner
    );
    cy.get('[data-test=summaryPanels] > .summary-panel').its('length').should('equal', 4);
    cy.get('[data-test=toggleShowAll]').contains('SHOW ALL').click();
    validateDisplayedFrameworkSummaryPanels(
      isProvideDataButtonExpected,
      isMobileViewActive,
      allFrameworks,
      isCompanyOwner
    );
    cy.get('[data-test=toggleShowAll]').contains('SHOW LESS').click();
    validateDisplayedFrameworkSummaryPanels(
      isProvideDataButtonExpected,
      isMobileViewActive,
      initiallyDisplayedFrameworks,
      isCompanyOwner
    );
    cy.get('[data-test=summaryPanels] > .summary-panel').its('length').should('equal', 4);
  }

  it('Checks the latest documents', () => {
    mockRequestsOnMounted(false);
    mountCompanyCockpitWithAuthentication(true, false, []);
    // For each category a request is made.
    Object.keys(DocumentMetaInfoDocumentCategoryEnum).forEach(() => {
      cy.wait('@fetchDocumentMetadata', { timeout: Cypress.env('medium_timeout_in_ms') as number });
    });
    for (const category of Object.keys(DocumentMetaInfoDocumentCategoryEnum)) {
      cy.get('[data-test="' + category + '"]')
        .should('exist')
        .and('contain', 'test_' + category)
        .find('div[data-test=download-link-component]')
        .children('button, span')
        .then((children) => {
          expect(children[0]).to.contain('(2025-02-25)');
          expect(children[1]).to.contain('(2024-01-13)');
          expect(children[2]).not.to.contain('null');
        });

      for (let i = 1; i <= 3; i++) {
        cy.get('[data-test="download-link-test_' + category + `_${i}"]`).should('exist');
      }
    }
  });

  it('Check for expected elements for a non-logged-in user and for a company without company owner', () => {
    const hasCompanyAtLeastOneOwner = false;
    const isClaimOwnershipPanelExpected = true;
    const isProvideDataButtonExpected = false;
    mockRequestsOnMounted(hasCompanyAtLeastOneOwner);
    mountCompanyCockpitWithAuthentication(false, false, []);
    validateBackButtonExistence();
    validateSearchBarExistence(true);
    validateCompanyInformationBanner(hasCompanyAtLeastOneOwner);
    validateClaimOwnershipPanel(isClaimOwnershipPanelExpected);
    validateFrameworkSummaryPanels(isProvideDataButtonExpected);
  });
  it('Check for expected company ownership elements for a non-logged-in user and for a company with a company owner', () => {
    const hasCompanyAtLeastOneOwner = true;
    const isClaimOwnershipPanelExpected = false;
    mockRequestsOnMounted(hasCompanyAtLeastOneOwner);
    mountCompanyCockpitWithAuthentication(false, false, []);
    validateCompanyInformationBanner(hasCompanyAtLeastOneOwner);
    validateClaimOwnershipPanel(isClaimOwnershipPanelExpected);
  });

  it('Check for all expected elements for a logged-in reader-user for a company with company owner', () => {
    const hasCompanyAtLeastOneOwner = true;
    const isClaimOwnershipPanelExpected = false;
    const isProvideDataButtonExpected = false;
    const isSingleDataRequestButtonExpected = true;
    mockRequestsOnMounted(hasCompanyAtLeastOneOwner);
    mountCompanyCockpitWithAuthentication(true, false, [KEYCLOAK_ROLE_USER], []);
    validateBackButtonExistence();
    validateSearchBarExistence(true);
    validateCompanyInformationBanner(hasCompanyAtLeastOneOwner);
    validateClaimOwnershipPanel(isClaimOwnershipPanelExpected);
    validateFrameworkSummaryPanels(isProvideDataButtonExpected);
    validateSingleDataRequestButton(isSingleDataRequestButtonExpected);
  });

  it('Check for all expected elements for a logged-in uploader-user and for a company without company owner', () => {
    const hasCompanyAtLeastOneOwner = false;
    const isClaimOwnershipPanelExpected = true;
    const isProvideDataButtonExpected = true;
    mockRequestsOnMounted(hasCompanyAtLeastOneOwner);
    mountCompanyCockpitWithAuthentication(true, false, [KEYCLOAK_ROLE_UPLOADER], []);
    validateBackButtonExistence();
    validateSearchBarExistence(true);
    validateCompanyInformationBanner(hasCompanyAtLeastOneOwner);
    validateClaimOwnershipPanel(isClaimOwnershipPanelExpected);
    validateFrameworkSummaryPanels(isProvideDataButtonExpected);
  });
  it('Check for all expected elements for a logged-in uploader-user with company ownership for a company with company owner', () => {
    const hasCompanyAtLeastOneOwner = true;
    const companyRoleAssignmentsOfUser = [generateCompanyRoleAssignment(CompanyRole.CompanyOwner, dummyCompanyId)];
    const isClaimOwnershipPanelExpected = false;
    const isProvideDataButtonExpected = true;
    const isSingleDataRequestButtonExpected = true;
    mockRequestsOnMounted(hasCompanyAtLeastOneOwner);
    mountCompanyCockpitWithAuthentication(true, false, [KEYCLOAK_ROLE_UPLOADER], companyRoleAssignmentsOfUser);
    validateBackButtonExistence();
    validateSearchBarExistence(true);
    validateCompanyInformationBanner(hasCompanyAtLeastOneOwner);
    validateClaimOwnershipPanel(isClaimOwnershipPanelExpected);
    validateFrameworkSummaryPanels(isProvideDataButtonExpected, false, true);
    validateSingleDataRequestButton(isSingleDataRequestButtonExpected);
  });
  it('Check for some expected elements for a logged-in premium-user and for a company without company owner', () => {
    const hasCompanyAtLeastOneOwner = false;
    const isSingleDataRequestButtonExpected = true;
    mockRequestsOnMounted(hasCompanyAtLeastOneOwner);
    mountCompanyCockpitWithAuthentication(true, false, [KEYCLOAK_ROLE_PREMIUM_USER], []);
    validateSingleDataRequestButton(isSingleDataRequestButtonExpected);
  });

  it('Check the Vsme summary panel behaviour if the user is company owner', () => {
    const companyRoleAssignmentsOfUser = [generateCompanyRoleAssignment(CompanyRole.CompanyOwner, dummyCompanyId)];
    const hasCompanyAtLeastOneOwner = true;
    KEYCLOAK_ROLES.forEach((keycloakRole: string) => {
      mockRequestsOnMounted(hasCompanyAtLeastOneOwner);
      mountCompanyCockpitWithAuthentication(true, false, [keycloakRole], companyRoleAssignmentsOfUser);
      cy.get('[data-test="toggleShowAll"]').contains('SHOW ALL').click();
      validateVsmeFrameworkSummaryPanel(true);
    });
  });

  KEYCLOAK_ROLES.forEach((keycloakRole: string) => {
    it(`Check the Vsme summary panel behaviour if the user is not company owner, Case: ${keycloakRole}`, () => {
      const hasCompanyAtLeastOneOwner = true;
      mockRequestsOnMounted(hasCompanyAtLeastOneOwner);
      mountCompanyCockpitWithAuthentication(true, false, [keycloakRole]);
      cy.get('[data-test="toggleShowAll"]').contains('SHOW ALL').click();
      validateVsmeFrameworkSummaryPanel(false);
    });
  });

  it('Check for all expected elements for an uploader-user on a mobile device for a company without company owner', () => {
    const hasCompanyAtLeastOneOwner = false;
    const isClaimOwnershipPanelExpected = true;
    const isProvideDataButtonExpected = false;
    setMobileDeviceViewport();
    mockRequestsOnMounted(hasCompanyAtLeastOneOwner);
    mountCompanyCockpitWithAuthentication(true, true, [KEYCLOAK_ROLE_UPLOADER]);

    validateBackButtonExistence();
    validateSearchBarExistence(false);
    validateCompanyInformationBanner(hasCompanyAtLeastOneOwner);
    validateClaimOwnershipPanel(isClaimOwnershipPanelExpected);
    validateFrameworkSummaryPanels(isProvideDataButtonExpected, true);
  });

  it('Users Page has to be visible for Dataland Admins', () => {
    mockRequestsOnMounted(true);
    mountCompanyCockpitWithAuthentication(true, false, [KEYCLOAK_ROLE_ADMIN], []);
    cy.get('[data-test="usersTab"]').should('be.visible').click();
  });

  it('Users Page is not visible for non Dataland Admins', () => {
    mockRequestsOnMounted(true);
    mountCompanyCockpitWithAuthentication(true, false, undefined, []);
    cy.get('[data-test="usersTab"]').should('not.exist');
  });

  it('Users Page is visible for a Company Member', () => {
    const companyRoleAssignmentsOfUser = [generateCompanyRoleAssignment(CompanyRole.Member, dummyCompanyId)];
    mockRequestsOnMounted(true);
    mountCompanyCockpitWithAuthentication(true, false, undefined, companyRoleAssignmentsOfUser);
    cy.get('[data-test=sfdr-summary-panel]').should('be.visible');
    cy.get('[data-test="company-roles-card"]').should('not.be.visible');
    cy.get('[data-test="usersTab"]').click();
    cy.get('[data-test=sfdr-summary-panel]').should('not.be.visible');
    cy.get('[data-test="company-roles-card"]').should('be.visible');
    cy.get('[data-test="datasetsTab"]').click();
    cy.get('[data-test=sfdr-summary-panel]').should('be.visible');
    cy.get('[data-test="company-roles-card"]').should('not.be.visible');
  });

  it('Users are being displayed correctly in the Users Page', () => {
    const companyRoleAssignmentsOfUser = [generateCompanyRoleAssignment(CompanyRole.Member, dummyCompanyId)];
    cy.intercept('GET', '**/community/company-role-assignments*', (req) => {
      const q = req.query as Record<string, string | undefined>;
      if (q.role === CompanyRole.Member) {
        req.reply({
          statusCode: 200,
          body: [
            {
              companyRole: 'Member',
              companyId: dummyCompanyId,
              userId: dummyUserId,
              email: dummyEmail,
              firstName: dummyFirstName,
              lastName: dummyLastName,
            },
          ],
        });
      } else {
        req.reply({ statusCode: 200, body: [] });
      }
    }).as('roleFetch');
    mockRequestsOnMounted(true);
    mountCompanyCockpitWithAuthentication(true, false, undefined, companyRoleAssignmentsOfUser);
    cy.wait('@roleFetch');
    cy.get('[data-test="usersTab"]').click();
    cy.wait('@roleFetch');
    cy.get('[data-test="company-roles-card"]', { timeout: 10000 }).should('exist');
    cy.contains('[data-test="company-roles-card"]', 'Members').within(() => {
      cy.get('td', { timeout: 10000 }).should('exist');
      cy.get('td').contains(dummyFirstName).should('exist');
      cy.get('td').contains(dummyLastName).should('exist');
      cy.get('td').contains(dummyEmail).should('exist');
      cy.get('td').contains(dummyUserId).should('exist');
    });
    cy.contains('[data-test="company-roles-card"]', 'Admins').within(() => {
      cy.get('td').contains(dummyFirstName).should('not.exist');
      cy.get('td').contains(dummyLastName).should('not.exist');
      cy.get('td').contains(dummyEmail).should('not.exist');
      cy.get('td').contains(dummyUserId).should('not.exist');
    });
  });
});
