import SearchCompaniesForFrameworkData from "@/components/pages/SearchCompaniesForFrameworkData.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { prepareSimpleDataSearchStoredCompanyArray } from "@ct/testUtils/PrepareDataSearchStoredCompanyArray";
import type Keycloak from "keycloak-js";
import { KEYCLOAK_ROLE_REVIEWER, KEYCLOAK_ROLE_UPLOADER, KEYCLOAK_ROLE_USER } from "@/utils/KeycloakUtils";

describe("Component tests for 'Request Data' button on the level of company search", function (): void {
  const mockDataSearchStoredCompanyArray = prepareSimpleDataSearchStoredCompanyArray();

  beforeEach(() => {
    cy.intercept("**/api/companies?**", mockDataSearchStoredCompanyArray);
    cy.intercept("**/api/companies/meta-information", mockDataSearchStoredCompanyArray[0].dataRegisteredByDataland[0]);
  });

  /**
   * Method to check the existence and the functionality of the Request Data button after it has been ensured that only
   * one button of this kind can be visible at all (via data table entry)
   * @param keycloakMock Keycloak settings for the mock, especially containing user roles
   */
  function verifyExistenceAndFunctionalityOfRequestDataButton(keycloakMock: Keycloak): void {
    cy.mountWithPlugins<typeof SearchCompaniesForFrameworkData>(SearchCompaniesForFrameworkData, {
      keycloak: keycloakMock,
    }).then((mounted) => {
      void mounted.wrapper.setData({
        resultArray: mockDataSearchStoredCompanyArray,
      });
      cy.wait(500);
      cy.get("button").contains("Request Data").should("exist").click({ force: true });
      cy.wrap(mounted.component).its("$route.path").should("eq", "/requests");
    });
  }

  it("Check that the 'Request Data' button exists and works as expected for a data reader", () => {
    const keycloakMock = minimalKeycloakMock({});
    verifyExistenceAndFunctionalityOfRequestDataButton(keycloakMock);
  });

  it("Check that the 'Request Data' button exists and works as expected when the 'New Dataset' button is also present", () => {
    const keycloakMock = minimalKeycloakMock({
      roles: [KEYCLOAK_ROLE_USER, KEYCLOAK_ROLE_UPLOADER, KEYCLOAK_ROLE_REVIEWER],
    });
    verifyExistenceAndFunctionalityOfRequestDataButton(keycloakMock);
  });
});
