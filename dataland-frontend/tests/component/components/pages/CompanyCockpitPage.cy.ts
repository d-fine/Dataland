// @ts-nocheck
import CompanyCockpitPage from '@/components/pages/CompanyCockpitPage.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import {
  type AggregatedFrameworkDataSummary,
  type CompanyInformation,
  type DataTypeEnum,
  type HeimathafenData,
} from '@clients/backend';
import { type FixtureData } from '@sharedUtils/Fixtures';
import {
  KEYCLOAK_ROLE_PREMIUM_USER,
  KEYCLOAK_ROLE_UPLOADER,
  KEYCLOAK_ROLE_USER,
  KEYCLOAK_ROLES,
} from '@/utils/KeycloakUtils';
import { setMobileDeviceViewport } from '@sharedUtils/TestSetupUtils';
import { computed } from 'vue';
import { CompanyRole, type CompanyRoleAssignment } from '@clients/communitymanager';

describe('Component test for the company cockpit', () => {
  let companyInformationForTest: CompanyInformation;
  let mockMapOfDataTypeToAggregatedFrameworkDataSummary: Map<DataTypeEnum, AggregatedFrameworkDataSummary>;
  const dummyCompanyId = '550e8400-e29b-11d4-a716-446655440000';
  const dummyUserId = 'mock-user-id';

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
    });
  }

  /**
   * Waits for the two requests that happen when the company cockpit page is being mounted
   */
  function waitForRequestsOnMounted(): void {
    cy.wait('@fetchCompanyInfo');
    cy.wait('@fetchAggregatedFrameworkMetaInfo');
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
    return cy.mountWithPlugins(CompanyCockpitPage, {
      keycloak: minimalKeycloakMock({
        authenticated: isLoggedIn,
        roles: keycloakRoles,
        userId: dummyUserId,
      }),
      global: {
        provide: {
          useMobileView: computed((): boolean => isMobile),
          companyRoleAssignments: companyRoleAssignments,
        },
      },
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      props: {
        companyId: dummyCompanyId,
      },
    });
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
   * @param isCompanyOwner is the current user company owner
   */
  function validateFrameworkSummaryPanels(isProvideDataButtonExpected: boolean, isCompanyOwner: boolean = false): void {
    Object.entries(mockMapOfDataTypeToAggregatedFrameworkDataSummary).forEach(
      ([frameworkName, aggregatedFrameworkDataSummary]: [string, AggregatedFrameworkDataSummary]) => {
        const frameworkSummaryPanelSelector = `div[data-test="${frameworkName}-summary-panel"]`;
        cy.get(frameworkSummaryPanelSelector).should('exist');
        cy.get(`${frameworkSummaryPanelSelector} span[data-test="${frameworkName}-panel-value"]`).should(
          'contain',
          aggregatedFrameworkDataSummary.numberOfProvidedReportingPeriods.toString()
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
      }
    );
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

  it('Check for expected elements for a non-logged-in user and for a company without company owner', () => {
    const hasCompanyAtLeastOneOwner = false;
    const isClaimOwnershipPanelExpected = true;
    const isProvideDataButtonExpected = false;
    mockRequestsOnMounted(hasCompanyAtLeastOneOwner);
    mountCompanyCockpitWithAuthentication(false, false, []).then(() => {
      waitForRequestsOnMounted();
      validateBackButtonExistence(false);
      validateSearchBarExistence(true);
      validateCompanyInformationBanner(hasCompanyAtLeastOneOwner);
      validateClaimOwnershipPanel(isClaimOwnershipPanelExpected);
      validateFrameworkSummaryPanels(isProvideDataButtonExpected);
    });
  });
  it('Check for expected company ownership elements for a non-logged-in users and for a company with a company owner', () => {
    const hasCompanyAtLeastOneOwner = true;
    const isClaimOwnershipPanelExpected = false;
    mockRequestsOnMounted(hasCompanyAtLeastOneOwner);
    mountCompanyCockpitWithAuthentication(false, false, []).then(() => {
      waitForRequestsOnMounted();
      validateCompanyInformationBanner(hasCompanyAtLeastOneOwner);
      validateClaimOwnershipPanel(isClaimOwnershipPanelExpected);
    });
  });

  it('Check for all expected elements for a logged-in reader-user for a company with company owner', () => {
    const hasCompanyAtLeastOneOwner = true;
    const isClaimOwnershipPanelExpected = false;
    const isProvideDataButtonExpected = false;
    const isSingleDataRequestButtonExpected = true;
    mockRequestsOnMounted(hasCompanyAtLeastOneOwner);
    mountCompanyCockpitWithAuthentication(true, false, [KEYCLOAK_ROLE_USER], []).then(() => {
      waitForRequestsOnMounted();
      validateBackButtonExistence(false);
      validateSearchBarExistence(true);
      validateCompanyInformationBanner(hasCompanyAtLeastOneOwner);
      validateClaimOwnershipPanel(isClaimOwnershipPanelExpected);
      validateFrameworkSummaryPanels(isProvideDataButtonExpected);
      validateSingleDataRequestButton(isSingleDataRequestButtonExpected);
    });
  });

  it('Check for all expected elements for a logged-in uploader-user and for a company without company owner', () => {
    const hasCompanyAtLeastOneOwner = false;
    const isClaimOwnershipPanelExpected = true;
    const isProvideDataButtonExpected = true;
    mockRequestsOnMounted(hasCompanyAtLeastOneOwner);
    mountCompanyCockpitWithAuthentication(true, false, [KEYCLOAK_ROLE_UPLOADER], []).then(() => {
      waitForRequestsOnMounted();
      validateBackButtonExistence(false);
      validateSearchBarExistence(true);
      validateCompanyInformationBanner(hasCompanyAtLeastOneOwner);
      validateClaimOwnershipPanel(isClaimOwnershipPanelExpected);
      validateFrameworkSummaryPanels(isProvideDataButtonExpected);
    });
  });
  it('Check for all expected elements for a logged-in uploader-user with company ownership for a company with company owner', () => {
    const hasCompanyAtLeastOneOwner = true;
    const companyRoleAssignmentsOfUser = [
      generateCompanyRoleAssignment(CompanyRole.CompanyOwner, dummyCompanyId, dummyUserId),
    ];
    const isClaimOwnershipPanelExpected = false;
    const isProvideDataButtonExpected = true;
    const isSingleDataRequestButtonExpected = true;
    mockRequestsOnMounted(hasCompanyAtLeastOneOwner);
    mountCompanyCockpitWithAuthentication(
      true,
      false,
      [KEYCLOAK_ROLE_UPLOADER],
      companyRoleAssignmentsOfUser,
      dummyUserId
    ).then(() => {
      waitForRequestsOnMounted();
      validateBackButtonExistence(false);
      validateSearchBarExistence(true);
      validateCompanyInformationBanner(hasCompanyAtLeastOneOwner);
      validateClaimOwnershipPanel(isClaimOwnershipPanelExpected);
      validateFrameworkSummaryPanels(isProvideDataButtonExpected, true);
      validateSingleDataRequestButton(isSingleDataRequestButtonExpected);
    });
  });
  it('Check for some expected elements for a logged-in premium-user and for a company without company owner', () => {
    const hasCompanyAtLeastOneOwner = false;
    const isSingleDataRequestButtonExpected = true;
    mockRequestsOnMounted(hasCompanyAtLeastOneOwner);
    mountCompanyCockpitWithAuthentication(true, false, [KEYCLOAK_ROLE_PREMIUM_USER], []).then(() => {
      waitForRequestsOnMounted();
      validateSingleDataRequestButton(isSingleDataRequestButtonExpected);
    });
  });

  it('Check the Vsme summary panel behaviour if the user is company owner', () => {
    const companyRoleAssignmentsOfUser = [
      generateCompanyRoleAssignment(CompanyRole.CompanyOwner, dummyCompanyId, dummyUserId),
    ];
    const hasCompanyAtLeastOneOwner = true;
    KEYCLOAK_ROLES.forEach((keycloakRole: string) => {
      mockRequestsOnMounted(hasCompanyAtLeastOneOwner);
      mountCompanyCockpitWithAuthentication(true, false, [keycloakRole], companyRoleAssignmentsOfUser).then(() => {
        waitForRequestsOnMounted();
        validateVsmeFrameworkSummaryPanel(true);
      });
    });
  });
  it('Check the Vsme summary panel behaviour if the user is not company owner', () => {
    const hasCompanyAtLeastOneOwner = true;
    KEYCLOAK_ROLES.forEach((keycloakRole: string) => {
      mockRequestsOnMounted(hasCompanyAtLeastOneOwner);
      mountCompanyCockpitWithAuthentication(true, false, [keycloakRole]).then(() => {
        waitForRequestsOnMounted();
        validateVsmeFrameworkSummaryPanel(false);
      });
    });
  });

  it('Check for all expected elements for a uploader-user on a mobile device for a company without company owner', () => {
    const scrollDurationInMs = 300;
    setMobileDeviceViewport();
    const hasCompanyAtLeastOneOwner = false;
    const isClaimOwnershipPanelExpected = true;
    const isProvideDataButtonExpected = false;
    mockRequestsOnMounted(hasCompanyAtLeastOneOwner);
    mountCompanyCockpitWithAuthentication(true, true, [KEYCLOAK_ROLE_UPLOADER]).then(() => {
      waitForRequestsOnMounted();

      validateMobileHeader(false);
      cy.scrollTo('bottom', { duration: scrollDurationInMs });
      validateMobileHeader(true);
      cy.scrollTo('top', { duration: scrollDurationInMs });
      validateMobileHeader(false);

      validateBackButtonExistence(true);
      validateSearchBarExistence(false);
      validateCompanyInformationBanner(hasCompanyAtLeastOneOwner);
      validateClaimOwnershipPanel(isClaimOwnershipPanelExpected);
      validateFrameworkSummaryPanels(isProvideDataButtonExpected);
    });
  });
});
