import { doThingsInChunks, getKeycloakToken, uploadSingleElementWithRetries } from "../../support/utility";
import { CompanyInformation, EuTaxonomyData } from "../../../../build/clients/backend/api";
import { visitAndCheckAppMount } from "../../support/commands";
const chunkSize = 40;

describe("Population Test", { defaultCommandTimeout: Cypress.env("PREPOPULATE_TIMEOUT_S") * 1000 }, () => {
  let companiesWithData: Array<{ companyInformation: CompanyInformation; euTaxonomyData: EuTaxonomyData }>;
  const teaserCompanies: Array<{ companyIds: string }> = [];
  let teaserCompaniesPermIds: Array<{ permId: string }> = [];

  if (Cypress.env("REALDATA")) {
    teaserCompaniesPermIds = Cypress.env("TEASER_COMPANY_PERM_IDS").toString().split(",");
  }

  before(function () {
    cy.fixture("CompanyInformationWithEuTaxonomyData").then(function (companies) {
      companiesWithData = companies;
    });
  });

  beforeEach(function () {
    cy.restoreLoginSession();
  });

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
      (Cypress.env("REALDATA") && teaserCompaniesPermIds.includes({ permId: getPermId(companyInformation) })) ||
      (!Cypress.env("REALDATA") && teaserCompanies.length == 0)
    ) {
      teaserCompanies.push(json.companyId);
    }
  }

  it("Populate Companies and Eu Taxonomy Data", () => {
    getKeycloakToken("data_uploader", Cypress.env("KEYCLOAK_UPLOADER_PASSWORD"))
      .then((token) => {
        doThingsInChunks(companiesWithData, chunkSize, (element) => {
          return uploadSingleElementWithRetries("companies", element.companyInformation, token)
            .then((response) => response.json())
            .then((json) => {
              uploadSingleElementWithRetries(
                "data/eutaxonomies",
                {
                  companyId: json.companyId,
                  data: element.euTaxonomyData,
                },
                token
              );
              addCompanyIdToTeaserCompanies(element.companyInformation, json);
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
          body: JSON.stringify(teaserCompanies),
        })
      )
      .its("status")
      .should("eq", 200);
  });

  it("Check if all the company ids can be retrieved", () => {
    cy.retrieveCompanyIdsList().then((companyIdList: Array<string>) => {
      assert(
        companyIdList.length >= companiesWithData.length, // >= to avoid problem with several runs in a row
        `Found ${companyIdList.length}, expected at least ${companiesWithData.length} companies`
      );
    });
  });

  it("Check if all the data ids can be retrieved", () => {
    cy.retrieveDataIdsList().then((dataIdList: any) => {
      assert(
        dataIdList.length >= companiesWithData.length, // >= to avoid problem with several runs in a row
        `Found ${dataIdList.length}, expected at least ${companiesWithData.length} datasets`
      );
    });
  });

  it("Company Name Input field exists and works", () => {
    const inputValue = companiesWithData[0].companyInformation.companyName;
    visitAndCheckAppMount("/search");
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
    visitAndCheckAppMount("/search");
    cy.get("button.p-button").contains("Show all companies").should("not.be.disabled").click();
  });

  it("Check Eu Taxonomy Data Presence and Link route", () => {
    cy.retrieveDataIdsList().then((dataIdList: Array<string>) => {
      cy.intercept("**/api/data/eutaxonomies/*").as("retrieveTaxonomyData");
      visitAndCheckAppMount("/data/eutaxonomies/" + dataIdList[0]);
      cy.wait("@retrieveTaxonomyData", { timeout: 60 * 1000 }).then(() => {
        cy.get("h3").should("be.visible");
        cy.get("h3").contains("Revenue");
        cy.get("h3").contains("CapEx");
        cy.get("h3").contains("OpEx");
        cy.get(".d-card").should("contain", "Eligible");
      });
    });
  });

  it("Check Company associated EU Taxonomy Data Presence and Link route", () => {
    cy.retrieveCompanyIdsList().then((companyIdList: Array<string>) => {
      cy.intercept("**/api/companies/*").as("retrieveCompany");
      cy.intercept("**/api/data/eutaxonomies/*").as("retrieveTaxonomyData");
      visitAndCheckAppMount(`/companies/${companyIdList[0]}/eutaxonomies`);
      cy.wait("@retrieveCompany", { timeout: 60 * 1000 })
        .wait("@retrieveTaxonomyData", { timeout: 60 * 1000 })
        .then(() => {
          cy.get("h3").should("be.visible");
          cy.get("h3").contains("Revenue");
          cy.get("h3").contains("CapEx");
          cy.get("h3").contains("OpEx");
          cy.get("body").contains("Market Cap:");
          cy.get("body").contains("Headquarter:");
          cy.get("body").contains("Sector:");
          cy.get("input[name=eu_taxonomy_search_input]").should("exist");
        });
    });
  });
});
