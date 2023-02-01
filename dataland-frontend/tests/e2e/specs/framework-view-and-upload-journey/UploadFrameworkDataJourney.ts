import { getBaseUrl, uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import {
  generateDummyCompanyInformation,
  uploadCompanyViaApi,
  uploadCompanyViaFormAndGetId,
} from "@e2e/utils/CompanyUpload";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { DataTypeEnum } from "@clients/backend";

describe("As a user, I expect the search functionality on the /companies page to behave as I expect", function () {
  beforeEach(function () {
    cy.ensureLoggedIn(uploader_name, uploader_pw);
  });

  it("Go through the whole dataset creation process for a newly created company and verify pages and elements", function () {
    const uniqueCompanyMarkerA = Date.now().toString() + "abc";
    const uniqueCompanyMarkerB = Date.now().toString() + "xyz";
    const testCompanyNameForApiUpload =
      "New-Api-Created-Company-For-UploadFrameworkDataJourenyTest-" + uniqueCompanyMarkerA;
    const testCompanyNameForFormUpload =
      "Form-Created-Company-For-UploadFrameworkDataJourneyTest-" + uniqueCompanyMarkerB;
    getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
      return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyNameForApiUpload)).then(() => {
        const primevueHighlightedSuggestionClass = "p-focus";
        cy.visitAndCheckAppMount("/companies");
        cy.get('button[aria-label="New Dataset"]').click({ force: true });

        cy.intercept("**/api/companies*").as("searchCompanyName");
        cy.get("input[id=company_search_bar_standard]").click({ force: true }).type(uniqueCompanyMarkerA);
        cy.wait("@searchCompanyName", { timeout: Cypress.env("short_timeout_in_ms") as number }).then(() => {
          cy.get("ul[class=p-autocomplete-items]").should("exist");
          cy.get(".p-autocomplete-item").eq(0).should("not.have.class", primevueHighlightedSuggestionClass);
          cy.get("input[id=company_search_bar_standard]").type("{downArrow}");
          cy.get(".p-autocomplete-item")
            .eq(0)
            .should("have.class", primevueHighlightedSuggestionClass)
            .should("contain.text", testCompanyNameForApiUpload);
          cy.get("input[id=company_search_bar_standard]").type("{esc}");
          cy.get("div[id=option1Container")
            .find("span:contains(Add it)") //check if scroll TODO
            .click({ force: true });
          cy.intercept("**/api/metadata*").as("retrieveExistingDatasetsForCompany");
          return uploadCompanyViaFormAndGetId(testCompanyNameForFormUpload).then((companyId) => {
            cy.wait("@retrieveExistingDatasetsForCompany", {
              timeout: Cypress.env("short_timeout_in_ms") as number,
            }).then(() => {
              cy.url().should("eq", getBaseUrl() + "/companies/" + companyId + "/frameworks/upload");
              cy.intercept("**/api/companies/" + companyId).as("getCompanyInformation");
              cy.get("div[id=lksgContainer]").find('button[aria-label="Create Dataset"]').click();
              cy.wait("@getCompanyInformation", { timeout: Cypress.env("short_timeout_in_ms") as number }).then(() => {
                cy.url().should(
                  "eq",
                  getBaseUrl() + "/companies/" + companyId + "/frameworks/" + DataTypeEnum.Lksg + "/upload"
                );
                cy.get("h1").should("contain", testCompanyNameForFormUpload);
              });
            });
          });
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
      const uniqueCompanyMarker = Date.now().toString() + "17gjas59";
      const testCompanyName = "Api-Created-Company-With-Many-FrameworkData" + uniqueCompanyMarker;
      getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
        return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName)).then(() => {
          cy.visitAndCheckAppMount("/companies");
        });
      });
    }
  );
});
