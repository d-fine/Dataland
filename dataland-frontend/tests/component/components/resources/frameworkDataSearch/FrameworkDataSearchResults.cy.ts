import FrameworkDataSearchResults from "@/components/resources/frameworkDataSearch/FrameworkDataSearchResults.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { prepareSimpleDataSearchStoredCompanyArray } from "@ct/testUtils/PrepareDataSearchStoredCompanyArray";

describe("Component tests for 'Request Data' button on the level of company search results", () => {
  const keycloakMock = minimalKeycloakMock({});

  it("Check that 'Request Data' button is not appearing in case of a successful company search", () => {
    const mockDataSearchStoredCompanyArray = prepareSimpleDataSearchStoredCompanyArray();
    cy.mountWithPlugins<typeof FrameworkDataSearchResults>(FrameworkDataSearchResults, {
      keycloak: keycloakMock,
    }).then((mounted) => {
      void mounted.wrapper.setProps({
        data: mockDataSearchStoredCompanyArray,
        rowsPerPage: 100,
      });
      cy.get("button[aria-label='Request Data]").should("not.exist");
    });
  });

  it("Check that 'Request Data' button appears and works properly if company search is not successful", () => {
    cy.mountWithPlugins<typeof FrameworkDataSearchResults>(FrameworkDataSearchResults, {
      keycloak: keycloakMock,
    }).then((mounted) => {
      cy.get("button[aria-label='Request Data']").should("exist").click();
      cy.wrap(mounted.component).its("$route.path").should("eq", "/requests");
    });
  });
});
