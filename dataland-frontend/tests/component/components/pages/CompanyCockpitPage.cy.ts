import {
  generateCompanyRoleAssignment,
  mountCompanyCockpitWithAuthentication,
  mockRequestsOnMounted,
  validateVsmeFrameworkSummaryPanel,
} from '@ct/testUtils/CompanyCockpitUtils.ts';
import {
  type AggregatedFrameworkDataSummary,
  type CompanyInformation,
  DataTypeEnum,
  type LksgData,
} from '@clients/backend';
import { type FixtureData } from '@sharedUtils/Fixtures';
import { setMobileDeviceViewport } from '@sharedUtils/TestSetupUtils';
import { CompanyRole } from '@clients/communitymanager';
import {
  KEYCLOAK_ROLE_ADMIN,
  KEYCLOAK_ROLE_PREMIUM_USER,
  KEYCLOAK_ROLE_UPLOADER,
  KEYCLOAK_ROLE_USER,
  KEYCLOAK_ROLES,
} from '@/utils/KeycloakRoles';
import { DocumentMetaInfoDocumentCategoryEnum } from '@clients/documentmanager';

/**
 * Validates the existence of the company search bar
 * @param isSearchBarExpected determines if the existence of the search bar is expected
 */
function validateSearchBarExistence(isSearchBarExpected: boolean): void {
  const searchBarSelector = 'input[type="text"]#company_search_bar_standard';
  cy.get(searchBarSelector).should(isSearchBarExpected ? 'exist' : 'not.exist');
}

/**
 * Validates the existence of the panel that shows the offer to claim company ownership
 * @param isThisExpected is this panel expected
 */
function validateClaimOwnershipPanel(isThisExpected: boolean): void {
  cy.get("[data-test='claimOwnershipPanelLink']").should(isThisExpected ? 'exist' : 'not.exist');
}

/**
 * Validates the existence or non-existence of the single data request button
 * @param isButtonExpected self explanatory
 */
function validateSingleDataRequestButton(isButtonExpected: boolean): void {
  cy.get('[data-test="singleDataRequestButton"]').should(isButtonExpected ? 'exist' : 'not.exist');
}

describe('Component test for the company cockpit', () => {
  let companyInformationForTest: CompanyInformation;
  let mockMapOfDataTypeToAggregatedFrameworkDataSummary: Map<DataTypeEnum, AggregatedFrameworkDataSummary>;
  const dummyCompanyId = '550e8400-e29b-11d4-a716-446655440000';
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
   * Validates the existence of the banner that shows info about the company
   * @param hasCompanyCompanyOwner has the mocked company at least one company owner?
   */
  function validateCompanyInformationBanner(hasCompanyCompanyOwner?: boolean): void {
    cy.contains('h1', companyInformationForTest.companyName);
    cy.get("[data-test='verifiedCompanyOwnerBadge']").should(hasCompanyCompanyOwner ? 'exist' : 'not.exist');
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
    for (const frameworkName of frameworksToTest) {
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
        continue;
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
    }
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
    mockRequestsOnMounted(false, companyInformationForTest, mockMapOfDataTypeToAggregatedFrameworkDataSummary);
    mountCompanyCockpitWithAuthentication(true, false, []);
    // For each category a request is made.
    for (let i = 0; i < Object.keys(DocumentMetaInfoDocumentCategoryEnum).length; i++) {
      cy.wait('@fetchDocumentMetadata', { timeout: Cypress.env('medium_timeout_in_ms') as number });
    }
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
    mockRequestsOnMounted(
      hasCompanyAtLeastOneOwner,
      companyInformationForTest,
      mockMapOfDataTypeToAggregatedFrameworkDataSummary
    );
    mountCompanyCockpitWithAuthentication(false, false, []);
    validateSearchBarExistence(true);
    validateCompanyInformationBanner(hasCompanyAtLeastOneOwner);
    validateClaimOwnershipPanel(isClaimOwnershipPanelExpected);
    validateFrameworkSummaryPanels(isProvideDataButtonExpected);
  });
  it('Check for expected company ownership elements for a non-logged-in user and for a company with a company owner', () => {
    const hasCompanyAtLeastOneOwner = true;
    const isClaimOwnershipPanelExpected = false;
    mockRequestsOnMounted(
      hasCompanyAtLeastOneOwner,
      companyInformationForTest,
      mockMapOfDataTypeToAggregatedFrameworkDataSummary
    );
    mountCompanyCockpitWithAuthentication(false, false, []);
    validateCompanyInformationBanner(hasCompanyAtLeastOneOwner);
    validateClaimOwnershipPanel(isClaimOwnershipPanelExpected);
  });

  it('Check for all expected elements for a logged-in reader-user for a company with company owner', () => {
    const hasCompanyAtLeastOneOwner = true;
    const isClaimOwnershipPanelExpected = false;
    const isProvideDataButtonExpected = false;
    const isSingleDataRequestButtonExpected = true;
    mockRequestsOnMounted(
      hasCompanyAtLeastOneOwner,
      companyInformationForTest,
      mockMapOfDataTypeToAggregatedFrameworkDataSummary
    );
    mountCompanyCockpitWithAuthentication(true, false, [KEYCLOAK_ROLE_USER], []);
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
    mockRequestsOnMounted(
      hasCompanyAtLeastOneOwner,
      companyInformationForTest,
      mockMapOfDataTypeToAggregatedFrameworkDataSummary
    );
    mountCompanyCockpitWithAuthentication(true, false, [KEYCLOAK_ROLE_UPLOADER], []);
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
    mockRequestsOnMounted(
      hasCompanyAtLeastOneOwner,
      companyInformationForTest,
      mockMapOfDataTypeToAggregatedFrameworkDataSummary
    );
    mountCompanyCockpitWithAuthentication(true, false, [KEYCLOAK_ROLE_UPLOADER], companyRoleAssignmentsOfUser);
    validateSearchBarExistence(true);
    validateCompanyInformationBanner(hasCompanyAtLeastOneOwner);
    validateClaimOwnershipPanel(isClaimOwnershipPanelExpected);
    validateFrameworkSummaryPanels(isProvideDataButtonExpected, false, true);
    validateSingleDataRequestButton(isSingleDataRequestButtonExpected);
  });
  it('Check for some expected elements for a logged-in premium-user and for a company without company owner', () => {
    const hasCompanyAtLeastOneOwner = false;
    const isSingleDataRequestButtonExpected = true;
    mockRequestsOnMounted(
      hasCompanyAtLeastOneOwner,
      companyInformationForTest,
      mockMapOfDataTypeToAggregatedFrameworkDataSummary
    );
    mountCompanyCockpitWithAuthentication(true, false, [KEYCLOAK_ROLE_PREMIUM_USER], []);
    validateSingleDataRequestButton(isSingleDataRequestButtonExpected);
  });

  for (const keycloakRole of KEYCLOAK_ROLES) {
    it(`Check the Vsme summary panel behaviour if the user is not company owner, Case: ${keycloakRole}`, () => {
      const hasCompanyAtLeastOneOwner = true;
      mockRequestsOnMounted(
        hasCompanyAtLeastOneOwner,
        companyInformationForTest,
        mockMapOfDataTypeToAggregatedFrameworkDataSummary
      );
      mountCompanyCockpitWithAuthentication(true, false, [keycloakRole]);
      cy.get('[data-test="toggleShowAll"]').contains('SHOW ALL').click();
      validateVsmeFrameworkSummaryPanel(false);
    });
  }

  it('Check for all expected elements for an uploader-user on a mobile device for a company without company owner', () => {
    const hasCompanyAtLeastOneOwner = false;
    const isClaimOwnershipPanelExpected = true;
    const isProvideDataButtonExpected = false;
    setMobileDeviceViewport();
    mockRequestsOnMounted(
      hasCompanyAtLeastOneOwner,
      companyInformationForTest,
      mockMapOfDataTypeToAggregatedFrameworkDataSummary
    );
    mountCompanyCockpitWithAuthentication(true, true, [KEYCLOAK_ROLE_UPLOADER]);
    validateSearchBarExistence(false);
    validateCompanyInformationBanner(hasCompanyAtLeastOneOwner);
    validateClaimOwnershipPanel(isClaimOwnershipPanelExpected);
    validateFrameworkSummaryPanels(isProvideDataButtonExpected, true);
  });

  it('Users Page has to be visible for Dataland Admins', () => {
    mockRequestsOnMounted(true, companyInformationForTest, mockMapOfDataTypeToAggregatedFrameworkDataSummary);
    mountCompanyCockpitWithAuthentication(true, false, [KEYCLOAK_ROLE_ADMIN], []);
    cy.get('[data-test="usersTab"]').should('be.visible').click();
  });

  it('Users Page is not visible for non Dataland Admins', () => {
    mockRequestsOnMounted(true, companyInformationForTest, mockMapOfDataTypeToAggregatedFrameworkDataSummary);
    mountCompanyCockpitWithAuthentication(true, false, undefined, []);
    cy.get('[data-test="usersTab"]').should('not.exist');
  });
});
