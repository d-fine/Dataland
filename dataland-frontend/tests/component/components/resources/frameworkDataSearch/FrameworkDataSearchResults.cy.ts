import FrameworkDataSearchResults from "@/components/resources/frameworkDataSearch/FrameworkDataSearchResults.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { prepareSimpleDataSearchStoredCompanyArray } from "@ct/testUtils/PrepareDataSearchStoredCompanyArray";
import {
  DataSearchStoredCompany,
  getRouterLinkTargetFramework
} from "@/utils/SearchCompaniesForFrameworkDataPageDataRequester";
import {DataMetaInformation, DataTypeEnum} from "@clients/backend";
import {ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE} from "@/utils/Constants";

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
      cy.get("a[data-test=requestDataButton]").should("not.exist");
    });
  });

  it("Check that 'Request Data' button appears and works properly if company search is not successful", () => {
    cy.mountWithPlugins<typeof FrameworkDataSearchResults>(FrameworkDataSearchResults, {
      keycloak: keycloakMock,
    }).then((mounted) => {
      cy.get("button").contains("Request Data").should("exist").click();
      cy.wrap(mounted.component).its("$route.path").should("eq", "/requests");
    });
  });

  it("Unit test for getRouterLinkTargetFramework", () => {
    const companyData = {
      companyId: "dummy",
      dataRegisteredByDataland: [
        {
          currentlyActive: false,
          dataType: DataTypeEnum.EutaxonomyNonFinancials,
        },
        {
          currentlyActive: true,
          dataType: DataTypeEnum.Lksg,
        }
      ] as DataMetaInformation[],
    } as DataSearchStoredCompany;
    const routerLink = getRouterLinkTargetFramework(companyData, ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE);
    expect(routerLink).to.equal(`/companies/dummy/frameworks/${DataTypeEnum.Lksg}`);
  });
});
