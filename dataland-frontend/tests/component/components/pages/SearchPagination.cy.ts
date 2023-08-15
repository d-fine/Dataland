import SearchCompaniesForFrameworkData from "@/components/pages/SearchCompaniesForFrameworkData.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { prepareSimpleDataSearchStoredCompanyArray } from "@ct/testUtils/PrepareDataSearchStoredCompanyArray";
import Keycloak from "keycloak-js";
describe("As a user, I expect there to be multiple result pages if there are many results to be displayed", () => {
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


    const mockDataSearchStoredCompanyArray = prepareSimpleDataSearchStoredCompanyArray();
    beforeEach(() => {
        cy.intercept("**/api/companies?**", mockDataSearchStoredCompanyArray);
        cy.intercept("**/api/companies/meta-information", mockDataSearchStoredCompanyArray[0].dataRegisteredByDataland[0]);
    });

    it.only("Do a search with 0 matches, then assure that the paginator is gone and the page text says no results", () => {
        const inputValueThatWillResultInZeroMatches = "ABCDEFGHIJKLMNOPQRSTUVWXYZ12345678987654321";
        const keycloakMock = minimalKeycloakMock({
            roles: ["ROLE_USER", "ROLE_UPLOADER", "ROLE_REVIEWER"],
        });
        verifyExistenceAndFunctionalityOfRequestDataButton(keycloakMock);

        const inputValue = "a";
        cy.get("input[id=search_bar_top]")
            .should("not.be.disabled")
            .click({ force: true })
            .type(inputValue)
            .type("{enter}")
            .should("have.value", inputValue);
    });

    it("Search for all companies containing 'a' and verify that results are paginated, only first 100 are shown", () => {
        cy.visit("/companies");
        const inputValue = "a";
        cy.get("input[id=search_bar_top]")
            .should("not.be.disabled")
            .click({ force: true })
            .type(inputValue)
            .type("{enter}")
            .should("have.value", inputValue);
        cy.get("table.p-datatable-table").should("exist");
        cy.get(".p-paginator-current").should("contain.text", "Showing 1 to 100 of").contains("entries");
        cy.contains("span", "1-100 of");
    });

    it("Search for all companies, go to page 2 of the search results, then run a another query and verify that paginator and the page text are reset", () => {
        cy.visitAndCheckAppMount("/companies");
        cy.get("table.p-datatable-table").should("exist");
        cy.get('button[class="p-paginator-page p-paginator-element p-link"]').eq(0).should("contain.text", "2").click();
        cy.get("table.p-datatable-table").should("exist");
        const inputValue = "a";
        cy.get("input[id=search_bar_top]")
            .should("not.be.disabled")
            .click({ force: true })
            .type(inputValue)
            .type("{enter}")
            .should("have.value", inputValue);
        cy.get(".p-paginator-current").should("contain.text", "Showing 1 to 100 of").contains("entries");
        cy.contains("span", "1-100 of");
    });
});

/**
 *
 * @param keycloakMock abc
 */
