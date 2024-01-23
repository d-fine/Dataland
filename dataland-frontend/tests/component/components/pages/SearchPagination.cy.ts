import SearchCompaniesForFrameworkData from "@/components/pages/SearchCompaniesForFrameworkData.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { prepareSimpleDataSearchStoredCompanyArray } from "@ct/testUtils/PrepareDataSearchStoredCompanyArray";

/**
 * Loads mock data
 * @param arr optional param incase of no matches
 */
function intercept(arr: undefined | [] = undefined): undefined {
  const mockDataSearchStoredCompanyArray = prepareSimpleDataSearchStoredCompanyArray(200);
  cy.intercept("**/api/companies?**", arr ?? mockDataSearchStoredCompanyArray);
  cy.intercept("**/api/companies/meta-information", {
    countryCodes: ["CV"],
    sectors: ["partnerships"],
  });
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
  });
}

/**
 * enter text into search bar
 * @param input search string
 */
function enterSearch(input: string): undefined {
  cy.get("input[id=search_bar_top]").should("exist").type(input).type("{enter}").should("have.value", input).clear();
}

/**
 * checks if paginator exists
 */
function paginatorShouldExist(): undefined {
  cy.get("table.p-datatable-table").should("exist");
  cy.get(".p-paginator-current").should("contain.text", "Showing 1 to 100 of").contains("entries");
  cy.scrollTo("top");
  cy.contains("span", "1-100 of");
}

/**
 * checks if paginator does not exist
 */
function paginatorShouldNotExist(): undefined {
  cy.get("div.p-paginator").should("not.exist");
  cy.contains("span", "No results");
}

describe("As a user, I expect there to be multiple result pages if there are many results to be displayed", () => {
  it("Do a search with 0 matches, then assure that the paginator is gone and the page text says no results", () => {
    intercept([]);
    enterSearch("ABCDEFGHIJKLMNOPQRSTUVWXYZ12345678987654321");
    paginatorShouldNotExist();
  });

  it("Search for all companies containing 'abs' and verify that results are paginated, only first 100 are shown", () => {
    intercept();
    enterSearch("abs");
    paginatorShouldExist();
  });

  it("Search for all companies, go to page 2 of the search results, then run a another query and verify that paginator and the page text are reset", () => {
    intercept();
    cy.get('button[class="p-paginator-page p-paginator-element p-link"]').eq(0).should("contain.text", "2").click();
    enterSearch("abs");
    paginatorShouldExist();
  });
});
