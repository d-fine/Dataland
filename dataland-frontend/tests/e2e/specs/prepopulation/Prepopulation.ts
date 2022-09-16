import { retrieveDataIdsList, retrieveCompanyIdsList } from "@e2e/utils/ApiUtils";
import { doThingsInChunks } from "@e2e/utils/Cypress";
import { CompanyInformation, EuTaxonomyDataForNonFinancials, EuTaxonomyDataForFinancials } from "@clients/backend";
const chunkSize = 40;

describe(
  "As a user, I want to be able to see some data on the DataLand webpage",
  {
    defaultCommandTimeout: Cypress.env("PREPOPULATE_TIMEOUT_S") * 1000,
    retries: {
      runMode: 0,
      openMode: 0,
    },
  },
  () => {
    let companiesWithEuTaxonomyDataForNonFinancials: Array<{
      companyInformation: CompanyInformation;
      t: EuTaxonomyDataForNonFinancials;
    }>;
    let companiesWithEuTaxonomyDataForFinancials: Array<{
      companyInformation: CompanyInformation;
      t: EuTaxonomyDataForFinancials;
    }>;

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
          // Introduced if to reduce number of unnecessary asserts which add some overhead as coverage is re-computed after
          // every assert
          if (response.status !== 200) {
            assert(
              response.status === 200,
              `Got status code ${response.status} during upload of single element to ${endpoint}. Expected: 200.`
            );
          }

          return response;
        });
      }

      cy.getKeycloakToken("data_uploader", Cypress.env("KEYCLOAK_UPLOADER_PASSWORD"))
        .then((token) => {
          doThingsInChunks(companiesWithEuTaxonomyDataForNonFinancials, chunkSize, (element) => {
            return browserPromiseUploadSingleElementOnce("companies", element.companyInformation, token)
              .then((response) => response.json())
              .then((companyUploadResponseJson) => {
                browserPromiseUploadSingleElementOnce(
                  "data/eutaxonomy-non-financials",
                  {
                    companyId: companyUploadResponseJson.companyId,
                    data: element.t,
                  },
                  token
                );
              });
          });
          doThingsInChunks(companiesWithEuTaxonomyDataForFinancials, chunkSize, (element) => {
            return browserPromiseUploadSingleElementOnce("companies", element.companyInformation, token)
              .then((response) => response.json())
              .then((json) => {
                browserPromiseUploadSingleElementOnce(
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
