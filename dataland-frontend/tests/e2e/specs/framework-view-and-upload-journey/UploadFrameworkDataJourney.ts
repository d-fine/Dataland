import { uploader_name, uploader_pw } from "../../utils/Cypress";
import { generateCompanyInformation } from "../../fixtures/CompanyFixtures";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "../../utils/CompanyUpload";
import { getKeycloakToken } from "../../utils/Auth";

before(function () {});

describe("As a user, I expect the search functionality on the /companies page to behave as I expect", function () {
  beforeEach(function () {
    cy.ensureLoggedIn(uploader_name, uploader_pw);
  });

  it("Go through the whole dataset creation process for a newly created company and verify pages and elements", function () {
    /* TODO
          - Before: Create an fresh company with a specific name, but do not upload any framework data for it
          - Click on CREATE DATASET on search page and assure that you get redirected to the ChooseCompanyPage
          - Verify the search bar there for option 1, and assure that the created company appears there
          - Don't use that company. Instead, click on "add it" and add a new company via the form, assure that you get automatically redirected
          - Click on CREATE DATASET for some framwork and assure that you are redirected to the form
    */
    getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
     return uploadCompanyViaApi(
        token,
        generateDummyCompanyInformation("New-Company-For-UploadFrameworkDataJourenyTest-8f40g")
      ).then((storedCompany) => {
        const primevueHighlightedSuggestionClass = "p-focus";
        cy.visitAndCheckAppMount("/companies");
        cy.wait(10000) //TODO remove
        cy.get('button[aria-label="New Dataset"]').click();
        cy.intercept("**/api/companies*").as("searchCompanyName");
        cy.get("input[id=company_search_bar_standard]")
          .click({ force: true })
          .type(storedCompany.companyInformation.companyName.substring(3, 8));
        cy.wait("@searchCompany", { timeout: Cypress.env("short_timeout_in_ms") as number }).then(() => {
          cy.get("ul[class=p-autocomplete-items]");
          cy.get("input[id=company_search_bar_standard]").type("{downArrow}");
          cy.get(".p-autocomplete-item").eq(0).should("have.class", primevueHighlightedSuggestionClass);
          cy.get(".p-autocomplete-item").eq(1).should("not.have.class", primevueHighlightedSuggestionClass);
          cy.get("input[id=company_search_bar_standard]").type("{upArrow}");
        });
      });
    });
  });

  it(
    "Go through the whole dataset creation process for an existing company, which already has framework data for multiple frameworks," +
      " and verify pages and elements",
    function () {
      /* TODO
          - Before: Create Company with lots of framework data for it
          - Click on CREATE DATASET on search page and assure that you get redirected to the ChooseCompanyPage
          - Choose that company via keys on the autocomplete dropdown and assure that you are redirected to the ChooseFrameworkPage
          - Now verify the ChooseCompanyPage (are the existing datasets displayed?)
          - Click on the latest non-lksg dataset and check if you get redirected to the exact framework-view-page for it, and not the older one
          - Click on one existing lksg dataset and check if you get redirected to the general lksg view page for that company
    */
      cy.visitAndCheckAppMount("/companies");
    }
  );
});
