import CompanyCockpitPage from "@/components/pages/CompanyCockpitPage.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { type AggregatedFrameworkDataSummary, type CompanyInformation, type SmeData } from "@clients/backend";
import { type FixtureData } from "@sharedUtils/Fixtures";
import { AggregatedDataRequestDataTypeEnum } from "@clients/communitymanager";
import { KEYCLOAK_ROLE_UPLOADER, KEYCLOAK_ROLE_USER } from "@/utils/KeycloakUtils";
import type * as Cypress from "cypress";

describe("Component test for the company cockpit", () => {
  let companyInformationForTest: CompanyInformation;
  let mockMapOfDataTypeToAggregatedFrameworkDataSummary: Map<
    AggregatedDataRequestDataTypeEnum,
    AggregatedFrameworkDataSummary
  >;
  const dummyCompanyId = "abcde-fghij-klmop";

  before(function () {
    cy.fixture("CompanyInformationWithSmeData").then(function (jsonContent) {
      const smeFixtures = jsonContent as Array<FixtureData<SmeData>>;
      companyInformationForTest = smeFixtures[0].companyInformation;
    });
    cy.fixture("MapOfDataTypeToAggregatedFrameworkDataSummary").then(function (jsonContent) {
      mockMapOfDataTypeToAggregatedFrameworkDataSummary = jsonContent as Map<
        AggregatedDataRequestDataTypeEnum,
        AggregatedFrameworkDataSummary
      >;
    });
  });

  /**
   * Mocks the two requests that happen when the company cockpit page is being mounted
   */
  function mockRequestsOnMounted(): void {
    cy.intercept(`**/api/companies/${dummyCompanyId}/info`, {
      body: companyInformationForTest,
      times: 1,
    }).as("fetchCompanyInfo");
    cy.intercept("**/api/companies/*/aggregated-framework-meta-info", {
      body: mockMapOfDataTypeToAggregatedFrameworkDataSummary,
      times: 1,
    }).as("fetchAggregatedFrameworkMetaInfo");
  }

  /**
   * Mounts the company cockpit page with a specific authentication
   * @param isLoggedIn is a boolean that determines if the mount shall happen from a logged-in users perspective or not
   * @param roles defines the roles of the user if the mount happens from a logged-in users perspective
   * @returns the mounted component
   */
  function mountCompanyCockpitWithAuthentication(isLoggedIn: boolean, roles?: string[]): Cypress.Chainable {
    return cy.mountWithPlugins(CompanyCockpitPage, {
      keycloak: minimalKeycloakMock({
        authenticated: isLoggedIn,
        roles: roles,
      }),
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      props: {
        companyId: dummyCompanyId,
      },
    });
  }

  /**
   * Validates the existence of the back-button
   */
  function validateBackButtonExistence(): void {
    cy.contains("span", "BACK");
  }

  /**
   * Validates the existence of the company search bar
   */
  function validateSearchBarExistence(): void {
    cy.get('input[type="text"]#company_search_bar_standard').should("exist");
  }

  /**
   * Validates the existence of the banner that shows info about the company
   */
  function validateCompanyInformationBanner(): void {
    cy.contains("h1", companyInformationForTest.companyName);
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

  it("Check for all expected elements from a non-logged-in users perspective", () => {
    mockRequestsOnMounted();
    mountCompanyCockpitWithAuthentication(false).then(() => {
      validateBackButtonExistence();
      validateSearchBarExistence();
      validateCompanyInformationBanner();
      validateFrameworkSummaryPanels(false);
    });
  });

  it("Check for all expected elements from a logged-in users perspective with read-only rights", () => {
    mockRequestsOnMounted();
    mountCompanyCockpitWithAuthentication(true, [KEYCLOAK_ROLE_USER]).then(() => {
      validateBackButtonExistence();
      validateSearchBarExistence();
      validateCompanyInformationBanner();
      validateFrameworkSummaryPanels(false);
    });
  });

  it("Check for all expected elements from a logged-in users perspective with uploader-rights", () => {
    mockRequestsOnMounted();
    mountCompanyCockpitWithAuthentication(true, [KEYCLOAK_ROLE_UPLOADER]).then(() => {
      validateBackButtonExistence();
      validateSearchBarExistence();
      validateCompanyInformationBanner();
      validateFrameworkSummaryPanels(true);
    });
  });
  /* TODO Emanuel: Jenachdem wie wir letztendlich checken wollen ob der user auf einem mobile device ist, müsste noch
        ein Test hier ergänzt werden.  Wenn wir das über die Breite machen, sollte in diesem Test mit verkleinertem
        Viewport gecheckt werden, ob die Buttons versteckt werden.
        Wenn wir das über den Client machen, müsste cypress hier als mobile-client die Seite mounten.
        Besprechen wir am Ende des Tickets.
   */
});
