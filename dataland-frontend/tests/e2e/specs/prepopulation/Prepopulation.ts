import { retrieveDataIdsList, retrieveCompanyIdsList } from "../../utils/ApiUtils";
import { doThingsInChunks } from "../../utils/Cypress";
import {
  CompanyInformation,
  EuTaxonomyDataForNonFinancials,
  EuTaxonomyDataForFinancials,
} from "../../../../build/clients/backend";
const chunkSize = 40;

describe(
  "As a user, I want to be able to see some data on the DataLand webpage",
  { defaultCommandTimeout: Cypress.env("PREPOPULATE_TIMEOUT_S") * 1000 },
  () => {
    let companiesWithEuTaxonomyDataForNonFinancials: Array<{
      companyInformation: CompanyInformation;
      t: EuTaxonomyDataForNonFinancials;
    }>;
    let companiesWithEuTaxonomyDataForFinancials: Array<{
      companyInformation: CompanyInformation;
      t: EuTaxonomyDataForFinancials;
    }>;
    const teaserCompanyIds: Array<string> = [];
    let teaserCompanyPermIds: Array<string> = [];
    if (Cypress.env("REALDATA") === true) {
      teaserCompanyPermIds = Cypress.env("TEASER_COMPANY_PERM_IDS").toString().split(",");
    }

    before(function () {
      cy.fixture("CompanyInformationWithEuTaxonomyDataForNonFinancials").then(function (companies) {
        companiesWithEuTaxonomyDataForNonFinancials = companies;
      });
    });
    before(function () {
      cy.fixture("CompanyInformationWithEuTaxonomyDataForFinancials").then(function (companies) {
        companiesWithEuTaxonomyDataForFinancials = companies;
      });
    });
    beforeEach(function () {
      cy.ensureLoggedIn();
    });

    it("Populate Companies and Eu Taxonomy Data", () => {
      function getPermId(companyInformation: CompanyInformation) {
        const permIdArray = companyInformation.identifiers
          .filter((identifier) => identifier.identifierType === "PermId")
          .map((identifier) => identifier.identifierValue);
        if (permIdArray.length >= 1) {
          return permIdArray[0];
        } else {
          return "NotAvailable";
        }
      }

      function addCompanyIdToTeaserCompanies(companyInformation: CompanyInformation, json: any) {
        if (
          (Cypress.env("REALDATA") === true && teaserCompanyPermIds.includes(getPermId(companyInformation))) ||
          (Cypress.env("REALDATA") !== true && teaserCompanyIds.length == 0)
        ) {
          teaserCompanyIds.push(json.companyId);
        }
      }

      function browserPromiseUploadSingleElementOnce(
        endpoint: string,
        element: object,
        token: string
      ): Promise<Response> {
        return fetch(`/api/${endpoint}`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: "Bearer " + token,
          },
          body: JSON.stringify(element),
        }).then((response) => {
          assert(
            response.status.toString() === "200",
            `Got status code ${response.status.toString()} during upload of single ` +
              `Element to ${endpoint}. Expected: 200.`
          );
          return response;
        });
      }

      function uploadSingleElementWithRetries(endpoint: string, element: object, token: string): Promise<Response> {
        return browserPromiseUploadSingleElementOnce(endpoint, element, token)
          .catch((_) => browserPromiseUploadSingleElementOnce(endpoint, element, token))
          .catch((_) => browserPromiseUploadSingleElementOnce(endpoint, element, token));
      }

      cy.getKeycloakToken("data_uploader", Cypress.env("KEYCLOAK_UPLOADER_PASSWORD"))
        .then((token) => {
          doThingsInChunks(companiesWithEuTaxonomyDataForNonFinancials, chunkSize, (element) => {
            return uploadSingleElementWithRetries("companies", element.companyInformation, token)
              .then((response) => response.json())
              .then((companyUploadResponseJson) => {
                uploadSingleElementWithRetries(
                  "data/eutaxonomy-non-financials",
                  {
                    companyId: companyUploadResponseJson.companyId,
                    data: element.t,
                  },
                  token
                );
                addCompanyIdToTeaserCompanies(element.companyInformation, companyUploadResponseJson);
              });
          });
          doThingsInChunks(companiesWithEuTaxonomyDataForFinancials, chunkSize, (element) => {
            return uploadSingleElementWithRetries("companies", element.companyInformation, token)
              .then((response) => response.json())
              .then((json) => {
                uploadSingleElementWithRetries(
                  "data/eutaxonomy-financials",
                  {
                    companyId: json.companyId,
                    data: element.t,
                  },
                  token
                );
              });
          });
        })
        .should("eq", "done");
    });

    it("Check if the teaser company can be set", () => {
      cy.wrap(teaserCompanyIds).should("have.length", 1);
      cy.getKeycloakToken("data_uploader", Cypress.env("KEYCLOAK_UPLOADER_PASSWORD"))
        .then((token) =>
          cy.request({
            url: "/api/companies/teaser",
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              Authorization: "Bearer " + token,
            },
            body: JSON.stringify(teaserCompanyIds),
          })
        )
        .its("status")
        .should("eq", 200);
    });

    it("Check if all the company ids can be retrieved", () => {
      retrieveCompanyIdsList().then((allCompanyIdsList: Array<string>) => {
        assert(
          allCompanyIdsList.length >= companiesWithEuTaxonomyDataForNonFinancials.length, // >= to avoid problem with several runs in a row
          `Found ${allCompanyIdsList.length}, expected at least ${companiesWithEuTaxonomyDataForNonFinancials.length} companies`
        );
      });
      retrieveCompanyIdsList().then((allCompanyIdsList: Array<string>) => {
        assert(
          allCompanyIdsList.length >= companiesWithEuTaxonomyDataForFinancials.length, // >= to avoid problem with several runs in a row
          `Found ${allCompanyIdsList.length}, expected at least ${companiesWithEuTaxonomyDataForFinancials.length} companies`
        );
      });
    });

    it("Check if all the data ids can be retrieved", () => {
      retrieveDataIdsList().then((allDataIdsList: any) => {
        assert(
          allDataIdsList.length >= companiesWithEuTaxonomyDataForNonFinancials.length, // >= to avoid problem with several runs in a row
          `Found ${allDataIdsList.length}, expected at least ${companiesWithEuTaxonomyDataForNonFinancials.length} datasets`
        );
      });
      retrieveDataIdsList().then((allDataIdsList: any) => {
        assert(
          allDataIdsList.length >= companiesWithEuTaxonomyDataForFinancials.length, // >= to avoid problem with several runs in a row
          `Found ${allDataIdsList.length}, expected at least ${companiesWithEuTaxonomyDataForFinancials.length} datasets`
        );
      });
    });

    it("Company Name Input field exists and works", () => {
      const inputValue = companiesWithEuTaxonomyDataForNonFinancials[0].companyInformation.companyName;
      cy.visitAndCheckAppMount("/companies-only-search");
      cy.get("input[name=companyName]")
        .should("not.be.disabled")
        .type(inputValue, { force: true })
        .should("have.value", inputValue);
      cy.intercept("**/api/companies*").as("retrieveCompany");
      cy.get("button[name=getCompanies]").click();
      cy.wait("@retrieveCompany", { timeout: 60 * 1000 }).then(() => {
        cy.get("td").contains(companiesWithEuTaxonomyDataForNonFinancials[0].companyInformation.companyName);
      });
    });

    it("Show all companies button exists", () => {
      cy.visitAndCheckAppMount("/companies-only-search");
      cy.get("button.p-button").contains("Show all companies").should("not.be.disabled").click();
    });
  }
);
