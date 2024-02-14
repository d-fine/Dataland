import CompanyCockpitPage from "@/components/pages/CompanyCockpitPage.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { type AggregatedFrameworkDataSummary, type CompanyInformation, type SmeData } from "@clients/backend";
import { type FixtureData } from "@sharedUtils/Fixtures";
import { AggregatedDataRequestDataTypeEnum } from "@clients/communitymanager";
import { KEYCLOAK_ROLE_UPLOADER, KEYCLOAK_ROLE_USER, KEYCLOAK_ROLE_PREMIUM_USER } from "@/utils/KeycloakUtils";
import type * as Cypress from "cypress";
import { setMobileDeviceViewport } from "@sharedUtils/TestSetupUtils";
import { computed } from "vue";

describe("Component test for the company cockpit", () => {
  let companyInformationForTest: CompanyInformation;
  let mockMapOfDataTypeToAggregatedFrameworkDataSummary: Map<
    AggregatedDataRequestDataTypeEnum,
    AggregatedFrameworkDataSummary
  >;
  const dummyCompanyId = "550e8400-e29b-11d4-a716-446655440000";

  before(function () {
    cy.fixture("CompanyInformationWithSmeData").then(function (jsonContent) {
      const smeFixtures = jsonContent as Array<FixtureData<SmeData>>;
      companyInformationForTest = smeFixtures[0].companyInformation;
    });
    cy.fixture("MapOfFrameworkNameToAggregatedFrameworkDataSummaryMock").then(function (jsonContent) {
      mockMapOfDataTypeToAggregatedFrameworkDataSummary = jsonContent as Map<
        AggregatedDataRequestDataTypeEnum,
        AggregatedFrameworkDataSummary
      >;
    });
  });

  /**
   * Mocks the three requests that happen when the company cockpit page is being mounted
   * @param hasCompanyDataOwner has the company at least one data owner
   */
  function mockRequestsOnMounted(hasCompanyDataOwner: boolean = false): void {
    cy.intercept(`**/api/companies/${dummyCompanyId}/info`, {
      body: companyInformationForTest,
      times: 1,
    }).as("fetchCompanyInfo");
    cy.intercept("**/api/companies/*/aggregated-framework-data-summary", {
      body: mockMapOfDataTypeToAggregatedFrameworkDataSummary,
      times: 1,
    }).as("fetchAggregatedFrameworkMetaInfo");

    cy.intercept("**/api/companies/*/data-owners/mock-data-owner-id", {
      status: 200,
    }).as("fetchUserIsDataOwnerTrue");
    if (hasCompanyDataOwner) {
      cy.intercept("**/api/companies/*/data-owners", {
        body: ["company-owner-id"],
      }).as("fetchHasCompanyDataOwnersFalse");
    }
  }

  /**
   * Waits for the two requests that happen when the company cockpit page is being mounted
   */
  function waitForRequestsOnMounted(): void {
    cy.wait("@fetchCompanyInfo");
    cy.wait("@fetchAggregatedFrameworkMetaInfo");
  }

  /**
   * Mounts the company cockpit page with a specific authentication
   * @param isLoggedIn determines if the mount shall happen from a logged-in users perspective
   * @param isMobile determines if the mount shall happen from a mobie-users perspective
   * @param roles defines the roles of the user if the mount happens from a logged-in users perspective
   * @param userId defines a custom user id for the logged-in user
   * @returns the mounted component
   */
  function mountCompanyCockpitWithAuthentication(
    isLoggedIn: boolean,
    isMobile: boolean,
    roles?: string[],
    userId?: string,
  ): Cypress.Chainable {
    return cy.mountWithPlugins(CompanyCockpitPage, {
      keycloak: minimalKeycloakMock({
        authenticated: isLoggedIn,
        roles: roles,
        userId: userId,
      }),
      global: {
        provide: {
          useMobileView: computed((): boolean => isMobile),
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
    const backButtonSelector = `span[data-test="${isMobile ? "back-button-mobile" : "back-button"}"]`;
    cy.get(backButtonSelector).should("exist");
  }

  /**
   * Validates the existence of the company search bar
   * @param isSearchBarExpected determines if the existence of the search bar is expected
   */
  function validateSearchBarExistence(isSearchBarExpected: boolean): void {
    const searchBarSelector = 'input[type="text"]#company_search_bar_standard';
    cy.get(searchBarSelector).should(isSearchBarExpected ? "exist" : "not.exist");
  }

  /**
   * Validates the existence of the banner that shows info about the company
   * @param hasCompanyDataOwner has the mocked company at least one data owner?
   */
  function validateCompanyInformationBanner(hasCompanyDataOwner?: boolean): void {
    cy.contains("h1", companyInformationForTest.companyName);
    cy.get("[data-test='verifiedDataOwnerBadge']").should(hasCompanyDataOwner ? "exist" : "not.exist");
  }

  /**
   * Validates the existence of the panel that shows the offer to claim data ownership
   * @param isThisExpected is this panel expected
   */
  function validateClaimOwnershipPanel(isThisExpected: boolean): void {
    cy.get("[data-test='claimOwnershipPanelLink']").should(isThisExpected ? "exist" : "not.exist");
  }

  /**
   * Validates the framework summary panels by asserting their existence and checking for their contents
   * @param isProvideDataButtonExpected determines if a provide-data-button is expected to be found in the panels
   */
  function validateFrameworkSummaryPanels(isProvideDataButtonExpected: boolean): void {
    Object.entries(mockMapOfDataTypeToAggregatedFrameworkDataSummary).forEach(
      ([frameworkName, aggregatedFrameworkDataSummary]: [string, AggregatedFrameworkDataSummary]) => {
        const frameworkSummaryPanelSelector = `div[data-test="${frameworkName}-summary-panel"]`;
        cy.get(frameworkSummaryPanelSelector).should("exist");
        cy.get(`${frameworkSummaryPanelSelector} span[data-test="${frameworkName}-panel-value"]`).should(
          "contain",
          aggregatedFrameworkDataSummary.numberOfProvidedReportingPeriods.toString(),
        );

        const isSmeFramework = frameworkName === AggregatedDataRequestDataTypeEnum.Sme;

        if (isProvideDataButtonExpected && !isSmeFramework) {
          cy.get(`${frameworkSummaryPanelSelector} a[data-test="${frameworkName}-provide-data-button"]`).should(
            "exist",
          );
        } else {
          cy.get(`${frameworkSummaryPanelSelector} a[data-test="${frameworkName}-provide-data-button"]`).should(
            "not.exist",
          );
        }
      },
    );
  }

  /**
   * Validates if the mobile header of the company info sheet is currently fixed or not
   * @param isScrolled determines if the mobile page is currently scrolled or not
   */
  function validateMobileHeader(isScrolled: boolean): void {
    const sheetSelector = "[data-test=sheet]";
    const attachedSheetSelector = "[data-test=sheet-attached]";
    const mobileHeaderTitleSelector = "[data-test=mobile-header-title]";
    cy.get(mobileHeaderTitleSelector).should(
      "have.text",
      isScrolled ? companyInformationForTest.companyName : "Company Overview",
    );
    cy.get(sheetSelector).should(isScrolled ? "have.css" : "not.have.css", "visibility", "hidden");
    cy.get(attachedSheetSelector).should(isScrolled ? "have.not.css" : "have.css", "visibility", "hidden");
  }

  /**
   * Validates the existence or non-existence of the single data request button
   * @param isButtonExpected self explanatory
   */
  function validateSingleDataRequestButton(isButtonExpected: boolean): void {
    cy.get('[data-test="singleDataRequestButton"]').should(isButtonExpected ? "exist" : "not.exist");
  }

  it("Check for expected elements from a non-logged-in users perspective for a company without data owner", () => {
    const hasCompanyDataOwner = false;
    const isClaimOwnershipPanelExpected = false;
    const isProvideDataButtonExpected = false;
    mockRequestsOnMounted(hasCompanyDataOwner);
    mountCompanyCockpitWithAuthentication(false, false, [], "").then(() => {
      waitForRequestsOnMounted();
      validateBackButtonExistence(false);
      validateSearchBarExistence(true);
      validateCompanyInformationBanner(hasCompanyDataOwner);
      validateClaimOwnershipPanel(isClaimOwnershipPanelExpected);
      validateFrameworkSummaryPanels(isProvideDataButtonExpected);
    });
  });
  it("Check for expected data ownership elements from a non-logged-in users perspective for a company with a data owner", () => {
    const hasCompanyDataOwner = true;
    const isClaimOwnershipPanelExpected = false;
    mockRequestsOnMounted(hasCompanyDataOwner);
    mountCompanyCockpitWithAuthentication(false, false, [], "").then(() => {
      waitForRequestsOnMounted();
      validateCompanyInformationBanner(hasCompanyDataOwner);
      validateClaimOwnershipPanel(isClaimOwnershipPanelExpected);
    });
  });

  it("Check for all expected elements from a logged-in users perspective with read-only rights for a company with data owner", () => {
    const hasCompanyDataOwner = true;
    const isClaimOwnershipPanelExpected = false;
    const isProvideDataButtonExpected = false;
    mockRequestsOnMounted(hasCompanyDataOwner);
    mountCompanyCockpitWithAuthentication(true, false, [KEYCLOAK_ROLE_USER]).then(() => {
      waitForRequestsOnMounted();
      validateBackButtonExistence(false);
      validateSearchBarExistence(true);
      validateCompanyInformationBanner(hasCompanyDataOwner);
      validateClaimOwnershipPanel(isClaimOwnershipPanelExpected);
      validateFrameworkSummaryPanels(isProvideDataButtonExpected);
      validateSingleDataRequestButton(false);
    });
  });

  it("Check for all expected elements from a logged-in users perspective with uploader-rights for a company without data owner", () => {
    const hasCompanyDataOwner = false;
    const isClaimOwnershipPanelExpected = true;
    const isProvideDataButtonExpected = true;
    mockRequestsOnMounted(hasCompanyDataOwner);
    mountCompanyCockpitWithAuthentication(true, false, [KEYCLOAK_ROLE_UPLOADER]).then(() => {
      waitForRequestsOnMounted();
      validateBackButtonExistence(false);
      validateSearchBarExistence(true);
      validateCompanyInformationBanner(hasCompanyDataOwner);
      validateClaimOwnershipPanel(isClaimOwnershipPanelExpected);
      validateFrameworkSummaryPanels(isProvideDataButtonExpected);
    });
  });
  it("Check for all expected elements from a logged-in data owner perspective with uploader-rights for a company with data owner", () => {
    const hasCompanyDataOwner = true;
    const isClaimOwnershipPanelExpected = false;
    const isProvideDataButtonExpected = true;
    const isSingleDataRequestButtonExpected = false;
    mockRequestsOnMounted(hasCompanyDataOwner);
    mountCompanyCockpitWithAuthentication(true, false, [KEYCLOAK_ROLE_UPLOADER], "mock-data-owner-id").then(() => {
      waitForRequestsOnMounted();
      validateBackButtonExistence(false);
      validateSearchBarExistence(true);
      validateCompanyInformationBanner(hasCompanyDataOwner);
      validateClaimOwnershipPanel(isClaimOwnershipPanelExpected);
      validateFrameworkSummaryPanels(isProvideDataButtonExpected);
      validateSingleDataRequestButton(isSingleDataRequestButtonExpected);
    });
  });
  it("Check for some expected elements from a logged-in premium user perspective for a company without data owner", () => {
    const hasCompanyDataOwner = false;
    mockRequestsOnMounted(hasCompanyDataOwner);
    mountCompanyCockpitWithAuthentication(true, false, [KEYCLOAK_ROLE_PREMIUM_USER], "mock-data-owner-id").then(() => {
      waitForRequestsOnMounted();
      validateSingleDataRequestButton(true);
    });
  });

  it("Check for all expected elements from a mobile users perspective with uploader-rights for a company without data owner", () => {
    const scrollDurationInMs = 300;
    setMobileDeviceViewport();
    const hasCompanyDataOwner = false;
    const isClaimOwnershipPanelExpected = true;
    const isProvideDataButtonExpected = false;
    mockRequestsOnMounted(hasCompanyDataOwner);
    mountCompanyCockpitWithAuthentication(true, true, [KEYCLOAK_ROLE_UPLOADER]).then(() => {
      waitForRequestsOnMounted();

      validateMobileHeader(false);
      cy.scrollTo("bottom", { duration: scrollDurationInMs });
      validateMobileHeader(true);
      cy.scrollTo("top", { duration: scrollDurationInMs });
      validateMobileHeader(false);

      validateBackButtonExistence(true);
      validateSearchBarExistence(false);
      validateCompanyInformationBanner();
      validateClaimOwnershipPanel(isClaimOwnershipPanelExpected);
      validateFrameworkSummaryPanels(isProvideDataButtonExpected);
    });
  });
});
