import { doThingsInChunks, getKeycloakToken, uploadSingleElementWithRetries } from "../../support/utility";
import {
  CompanyInformation,
  EuTaxonomyDataForNonFinancials,
  EuTaxonomyDataForFinancials,
} from "../../../../build/clients/backend/api";
const chunkSize = 40;

describe("Population Test", { defaultCommandTimeout: Cypress.env("PREPOPULATE_TIMEOUT_S") * 1000 }, () => {
  let companiesWithEuTaxonomyDataForNonFinancials: Array<{
    companyInformation: CompanyInformation;
    t: EuTaxonomyDataForNonFinancials;
  }>;
  let companiesWithEuTaxonomyDataForFinancials: Array<{
    companyInformation: CompanyInformation;
    t: EuTaxonomyDataForFinancials;
  }>;
  const teaserCompanyIds: Array<{ companyId: string }> = [];
  let teaserCompanyPermIds: Array<{ permId: string }> = [];

  if (Cypress.env("REALDATA")) {
    teaserCompanyPermIds = Cypress.env("TEASER_COMPANY_PERM_IDS").toString().split(",");
  }

  before(function () {
    cy.fixture("CompanyInformationWithEuTaxonomyDataForNonFinancials").then(function (fixtures) {
      companiesWithEuTaxonomyDataForNonFinancials = fixtures;
    });
  });

  before(function () {
    cy.fixture("CompanyInformationWithEuTaxonomyDataForFinancials").then(function (fixtures) {
      companiesWithEuTaxonomyDataForFinancials = fixtures;
    });
  });

  beforeEach(function () {
    cy.restoreLoginSession();
  });

  function getFirstPermId(companyInformation: CompanyInformation) {
    const permIdArray = companyInformation.identifiers
      .filter((identifier) => identifier.identifierType === "PermId")
      .map((identifier) => identifier.identifierValue);
    if (permIdArray.length >= 1) {
      return permIdArray[0];
    } else {
      return "NotAvailable";
    }
  }

  function addCompanyIdToTeaserCompanies(companyInformation: CompanyInformation, companyId: string) {
    if (
      (Cypress.env("REALDATA") && teaserCompanyPermIds.includes({ permId: getFirstPermId(companyInformation) })) ||
      (!Cypress.env("REALDATA") && teaserCompanyIds.length == 0)
    ) {
      teaserCompanyIds.push({companyId: companyId});
    }
  }

  it("Populate Companies and Eu Taxonomy Data", () => {
    getKeycloakToken("data_uploader", Cypress.env("KEYCLOAK_UPLOADER_PASSWORD"))
      .then((token) => {
        doThingsInChunks(companiesWithEuTaxonomyDataForNonFinancials, chunkSize, (element) => {
          return uploadSingleElementWithRetries("companies", element.companyInformation, token)
            .then((response) => response.json())
            .then((companyUploadResponseJson) => {
              uploadSingleElementWithRetries(
                "data/eutaxonomy/nonfinancials",
                {
                  companyId: companyUploadResponseJson.companyId,
                  data: element.t,
                },
                token
              );
              addCompanyIdToTeaserCompanies(element.companyInformation, companyUploadResponseJson.companyId);
            });
        });
        doThingsInChunks(companiesWithEuTaxonomyDataForFinancials, chunkSize, (element) => {
          return uploadSingleElementWithRetries("companies", element.companyInformation, token)
            .then((response) => response.json())
            .then((json) => {
              uploadSingleElementWithRetries(
                "data/eutaxonomy/financials",
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
    getKeycloakToken("data_uploader", Cypress.env("KEYCLOAK_UPLOADER_PASSWORD"))
      .then((token) =>
        cy.request({
          url: `${Cypress.env("API")}/companies/teaser`,
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
    cy.retrieveCompanyIdsList().then((allCompanyIdsList: Array<string>) => {
      assert(
          allCompanyIdsList.length >= companiesWithEuTaxonomyDataForNonFinancials.length, // >= to avoid problem with several runs in a row
        `Found ${allCompanyIdsList.length}, expected at least ${companiesWithEuTaxonomyDataForNonFinancials.length} companies`
      );
    });
    cy.retrieveCompanyIdsList().then((allCompanyIdsList: Array<string>) => {
      assert(
          allCompanyIdsList.length >= companiesWithEuTaxonomyDataForFinancials.length, // >= to avoid problem with several runs in a row
        `Found ${allCompanyIdsList.length}, expected at least ${companiesWithEuTaxonomyDataForFinancials.length} companies`
      );
    });
  });

  it("Check if all the data ids can be retrieved", () => {
    cy.retrieveDataIdsList().then((allDataIdsList: any) => {
      assert(
          allDataIdsList.length >= companiesWithEuTaxonomyDataForNonFinancials.length, // >= to avoid problem with several runs in a row
        `Found ${allDataIdsList.length}, expected at least ${companiesWithEuTaxonomyDataForNonFinancials.length} datasets`
      );
    });
    cy.retrieveDataIdsList().then((allDataIdsList: any) => {
      assert(
          allDataIdsList.length >= companiesWithEuTaxonomyDataForFinancials.length, // >= to avoid problem with several runs in a row
        `Found ${allDataIdsList.length}, expected at least ${companiesWithEuTaxonomyDataForFinancials.length} datasets`
      );
    });
  });

  it("Company Name Input field exists and works, and all companies can be retrieved", () => {
    const inputValue = companiesWithEuTaxonomyDataForNonFinancials[0].companyInformation.companyName;
    cy.visitAndCheckAppMount("/companies-only-search");
    cy.get("input[name=companyName]")
      .should("not.be.disabled")
      .type(inputValue, { force: true })
      .should("have.value", inputValue);
    cy.intercept("**/api/companies*").as("retrieveCompany");
    cy.get("button[name=getCompanies]").click();
    cy.wait("@retrieveCompany", { timeout: 60 * 1000 }).then(() => {
      cy.get("td").contains("VIEW").contains("a", "VIEW").click().url().should("include", "/companies/");
    });
  });

  it("Show all companies button exists", () => {
    cy.visitAndCheckAppMount("/companies-only-search");
    cy.get("button.p-button").contains("Show all companies").should("not.be.disabled").click();
  });
});
