import FrameworkDataSearchResults from "@/components/resources/frameworkDataSearch/FrameworkDataSearchResults.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { type BasicCompanyInformation } from "@clients/backend";

let mockDataSearchResponse: Array<BasicCompanyInformation>;
before(function () {
  cy.fixture("DataSearchStoredCompanyMocks").then(function (jsonContent) {
    mockDataSearchResponse = jsonContent as Array<BasicCompanyInformation>;
  });
});

describe("Component tests for 'Bulk Request Data' button on the level of company search results", () => {
  const keycloakMock = minimalKeycloakMock({});

  it("Check that 'Bulk Request Data' button is not appearing in case of a successful company search", () => {
    cy.mountWithPlugins<typeof FrameworkDataSearchResults>(FrameworkDataSearchResults, {
      keycloak: keycloakMock,
    }).then((mounted) => {
      void mounted.wrapper.setProps({
        data: mockDataSearchResponse,
        rowsPerPage: 100,
      });
      cy.get("a[data-test=bulkDataRequestButton]").should("not.exist");
    });
  });

  it("Check that 'Bulk Request Data' button appears and works properly if company search is not successful", () => {
    cy.mountWithPlugins<typeof FrameworkDataSearchResults>(FrameworkDataSearchResults, {
      keycloak: keycloakMock,
    }).then((mounted) => {
      cy.get("button").contains("Bulk Request Data").should("exist").click();
      cy.wrap(mounted.component).its("$route.path").should("eq", "/bulkdatarequest");
    });
  });
});
