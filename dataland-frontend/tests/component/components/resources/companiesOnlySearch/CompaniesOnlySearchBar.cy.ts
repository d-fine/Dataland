import {minimalKeycloakMock} from "../../../testUtils/Keycloak";
import CompaniesOnlySearchBar from "@/components/resources/companiesOnlySearch/CompaniesOnlySearchBar.vue"
import {CompanyIdAndName} from "../../../../../build/clients/backend";

describe("Test CompanyOnlySearchBar vue component", () => {
  it("Validate the order of autocomplete suggestions", () => {
    cy.intercept("**/api/companies/with-active-datasets", (request) => {
      request.reply(200, [
        "1", "2", "3"
      ])
    });
    const expectedCompanyNameOrder = ["ShouldBeFirst", "ShouldBeSecond", "ShouldBeThird"];
    cy.intercept("GET", "**/api/companies/names*", (request) => {
      request.reply(200, <CompanyIdAndName[]>[
        {
          companyId: "4",
          companyName: expectedCompanyNameOrder[2]
        },
        {
          companyId: "2",
          companyName: expectedCompanyNameOrder[0]
        },
        {
          companyId: "1",
          companyName: expectedCompanyNameOrder[1]
        },
      ])
    });
    cy.mountWithPlugins(CompaniesOnlySearchBar, {
      keycloak: minimalKeycloakMock({})
    }).then(() => {
      cy.get("#company_search_bar_standard").type("abc");
      cy.get("ul.p-autocomplete-items li").each((item, index) => {
        expect(item.text()).to.equal(expectedCompanyNameOrder[index]);
      });
    });
  });
});