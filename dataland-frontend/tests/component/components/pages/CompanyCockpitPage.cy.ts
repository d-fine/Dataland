import CompanyCockpitPage from '@/components/pages/CompanyCockpitPage.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import {
  type AggregatedFrameworkDataSummary,
  type CompanyInformation,
  DataTypeEnum,
  type HeimathafenData,
} from '@clients/backend';
import { type FixtureData } from '@sharedUtils/Fixtures';
import { setMobileDeviceViewport } from '@sharedUtils/TestSetupUtils';
import { computed } from 'vue';
import { CompanyRole, type CompanyRoleAssignment } from '@clients/communitymanager';
import { getMountingFunction } from '@ct/testUtils/Mount';
import {
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
  const initiallyDisplayedFrameworks: Set<DataTypeEnum> = new Set([
    DataTypeEnum.EutaxonomyFinancials,
    DataTypeEnum.EutaxonomyNonFinancials,
    DataTypeEnum.NuclearAndGas,
    DataTypeEnum.Sfdr,
  ]);
  let allFrameworks: Set<DataTypeEnum>;

  before(function () {
    cy.fixture('CompanyInformationWithHeimathafenData').then(function (jsonContent) {
      const heimathafenFixtures = jsonContent as Array<FixtureData<HeimathafenData>>;
      companyInformationForTest = heimathafenFixtures[0].companyInformation;
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
  function generateCompanyRoleAssignment(companyRole: CompanyRole, companyId: string): CompanyRoleAssignment {
    return {
      companyRole: companyRole,
      companyId: companyId,
      userId: dummyUserId,
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
    cy.intercept('**/documents/**', (request) => {
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
   * @param isMobile determines if the mount shall happen from a mobie-users perspective
   * @param keycloakRoles defines the keycloak roles of the user if the mount happens from a logged-in users perspective
   * @param companyRoleAssignments defines the company role assignments that the current user shall have
   * @returns the mounted component
   */
  function mountCompanyCockpitWithAuthentication(
    isLoggedIn: boolean,
    isMobile: boolean,
    keycloakRoles?: string[],
    companyRoleAssignments?: CompanyRoleAssignment[]
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
          companyRoleAssignments: companyRoleAssignments,
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
   * @param isMobile determines if the validation shall be executed from a moble users perspective
   */
  function validateBackButtonExistence(isMobile: boolean): void {
    const backButtonSelector = `span[data-test="${isMobile ? 'back-button-mobile' : 'back-button'}"]`;
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
      cy.get(`${frameworkSummaryPanelSelector} a[data-test="${frameworkName}-provide-data-button"]`).should('exist');
    } else {
      cy.get(`${frameworkSummaryPanelSelector} a[data-test="${frameworkName}-provide-data-button"]`).should(
        'not.exist'
      );
    }
  }

  /**
   * Validates the framework summary panels by asserting their existence and checking for their contents
   * @param isProvideDataButtonExpected determines if a provide-data-button is expected to be found in the panels
   * @param frameworksToTest the frameworks that are tested for
   * @param isCompanyOwner is the current user company owner
   */
  function validateDisplayedFrameworkSummaryPanels(
    isProvideDataButtonExpected: boolean,
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
      if (frameworkName == 'vsme') {
        validateVsmeFrameworkSummaryPanel(isCompanyOwner);
        return;
      }
      if (isProvideDataButtonExpected) {
        if (frameworkName != 'heimathafen') {
          cy.get(`${frameworkSummaryPanelSelector} a[data-test="${frameworkName}-provide-data-button"]`).should(
            'exist'
          );
        }
      } else {
        cy.get(`${frameworkSummaryPanelSelector} a[data-test="${frameworkName}-provide-data-button"]`).should(
          'not.exist'
        );
      }
    });
  }

  /**
   * Validates if the mobile header of the company info sheet is currently fixed or not
   * @param isScrolled determines if the mobile page is currently scrolled or not
   */
  function validateMobileHeader(isScrolled: boolean): void {
    const sheetSelector = '[data-test=sheet]';
    const attachedSheetSelector = '[data-test=sheet-attached]';
    const mobileHeaderTitleSelector = '[data-test=mobile-header-title]';
    cy.get(mobileHeaderTitleSelector).should(
      'have.text',
      isScrolled ? companyInformationForTest.companyName : 'Company Overview'
    );
    cy.get(sheetSelector).should(isScrolled ? 'have.css' : 'not.have.css', 'visibility', 'hidden');
    cy.get(attachedSheetSelector).should(isScrolled ? 'have.not.css' : 'have.css', 'visibility', 'hidden');
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
  function validateFrameworkSummaryPanels(isProvideDataButtonExpected: boolean, isCompanyOwner: boolean = false): void {
    validateDisplayedFrameworkSummaryPanels(isProvideDataButtonExpected, initiallyDisplayedFrameworks, isCompanyOwner);
    cy.get('[data-test=summaryPanels] > .summary-panel').its('length').should('equal', 4);
    cy.get('[data-test=toggleShowAll]').contains('SHOW ALL').click();
    validateDisplayedFrameworkSummaryPanels(isProvideDataButtonExpected, allFrameworks, isCompanyOwner);
    cy.get('[data-test=toggleShowAll]').contains('SHOW LESS').click();
    validateDisplayedFrameworkSummaryPanels(isProvideDataButtonExpected, initiallyDisplayedFrameworks, isCompanyOwner);
    cy.get('[data-test=summaryPanels] > .summary-panel').its('length').should('equal', 4);
  }

  it('Checks the latest documents', () => {
    mockRequestsOnMounted(false);
    mountCompanyCockpitWithAuthentication(false, false, []);
    // For each category a request is made.
    Object.keys(DocumentMetaInfoDocumentCategoryEnum).forEach(() => {
      cy.wait('@fetchDocumentMetadata', { timeout: Cypress.env('medium_timeout_in_ms') as number });
    });
    for (const category of Object.keys(DocumentMetaInfoDocumentCategoryEnum)) {
      cy.get('[data-test="' + category + '"]')
        .should('exist')
        .and('contain', 'test_' + category)
        .find('div[class=text-primary]')
        .children('a')
        .then((children) => {
          expect(children[0]).to.contain('(2025-02-25)');
          expect(children[1]).to.contain('(2024-01-13)');
          expect(children[2]).not.to.contain('null');
        });

      for (let i = 1; i <= 3; i++) {
        cy.get('[data-test="download-link-test_' + category + `_${i}"]`)
          .should('exist')
          .and('have.attr', 'title', 'test_' + category + `_${i}`);
      }
    }
  });

  it('Check for expected elements for a non-logged-in user and for a company without company owner', () => {
    const hasCompanyAtLeastOneOwner = false;
    const isClaimOwnershipPanelExpected = true;
    const isProvideDataButtonExpected = false;
    mockRequestsOnMounted(hasCompanyAtLeastOneOwner);
    mountCompanyCockpitWithAuthentication(false, false, []);
    validateBackButtonExistence(false);
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
    validateBackButtonExistence(false);
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
    validateBackButtonExistence(false);
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
    validateBackButtonExistence(false);
    validateSearchBarExistence(true);
    validateCompanyInformationBanner(hasCompanyAtLeastOneOwner);
    validateClaimOwnershipPanel(isClaimOwnershipPanelExpected);
    validateFrameworkSummaryPanels(isProvideDataButtonExpected, true);
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
  it('Check the Vsme summary panel behaviour if the user is not company owner', () => {
    const hasCompanyAtLeastOneOwner = true;
    KEYCLOAK_ROLES.forEach((keycloakRole: string) => {
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

    cy.wait(3000);
    cy.scrollTo('bottom');
    validateMobileHeader(true);
    cy.scrollTo('top');
    validateMobileHeader(false);

    validateBackButtonExistence(true);
    validateSearchBarExistence(false);
    validateCompanyInformationBanner(hasCompanyAtLeastOneOwner);
    validateClaimOwnershipPanel(isClaimOwnershipPanelExpected);
    validateFrameworkSummaryPanels(isProvideDataButtonExpected);
  });
});
