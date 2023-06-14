import SearchCompaniesForFrameworkData from "@/components/pages/SearchCompaniesForFrameworkData.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { prepareSimpleDataSearchStoredCompanyArray } from "@ct/testUtils/PrepareDataSearchStoredCompanyArray";

describe("Component tests for 'Request Data' button on the level of company search", function (): void {
  const mockDataSearchStoredCompanyArray = prepareSimpleDataSearchStoredCompanyArray();

  beforeEach(() => {
    cy.intercept("**/api/companies?**", mockDataSearchStoredCompanyArray);
    cy.intercept("**/api/companies/meta-information", mockDataSearchStoredCompanyArray[0].dataRegisteredByDataland[0]);
  });

  it("Check that the 'Request Data' button is permanently visible for a data reader", () => {
    const keycloakMock = minimalKeycloakMock({});
    cy.mountWithPlugins<typeof SearchCompaniesForFrameworkData>(SearchCompaniesForFrameworkData, {
      keycloak: keycloakMock,
    }).then((mounted) => {
      void mounted.wrapper.setData({
        resultArray: mockDataSearchStoredCompanyArray,
      });
      cy.wait(500);
      cy.get("button[aria-label='Request Data']").should("exist").click({ force: true });
      cy.wrap(mounted.component).its("$route.path").should("eq", "/requests");
    });
  });

  it("Check that the 'Request Data' button is permanently visible for a user with upload rights where the 'New Dataset' button is also present", () => {
    const keycloakMock = minimalKeycloakMock({
      roles: ["ROLE_USER", "ROLE_UPLOADER", "ROLE_REVIEWER"],
    });
    cy.mountWithPlugins<typeof SearchCompaniesForFrameworkData>(SearchCompaniesForFrameworkData, {
      keycloak: keycloakMock,
    }).then((mounted) => {
      void mounted.wrapper.setData({
        resultArray: mockDataSearchStoredCompanyArray,
      });
      cy.wait(500);
      cy.get("button[aria-label='Request Data']").should("exist").click({ force: true });
      cy.wrap(mounted.component).its("$route.path").should("eq", "/requests");
    });
  });
});
