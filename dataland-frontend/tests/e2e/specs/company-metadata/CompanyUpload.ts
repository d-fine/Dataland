import { describeIf } from "../../support/TestUtility";
import { createCompanyAndGetId, fillCompanyUploadFields } from "../../utils/CompanyUpload";

describeIf(
  "As a user, I want to be able to create new companies",
  {
    executionEnvironments: ["development"],
    dataEnvironments: ["fakeFixtures"],
  },
  () => {
    beforeEach(() => {
      cy.ensureLoggedIn("data_uploader", Cypress.env("KEYCLOAK_UPLOADER_PASSWORD"));
    });

    it("Check if post company button is disabled if no values are inserted into the upload form", () => {
      cy.visitAndCheckAppMount("/companies/upload");
      cy.get('button[name="postCompanyData"]').should("be.disabled");
    });

    function uploadEuTaxonomyDataForNonFinancials(companyId: string) {
      cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/eutaxonomy-non-financials/upload`);
      cy.get('button[name="postEUData"]', { timeout: 2 * 1000 }).should("be.visible");
      cy.get('input[id="reportingObligation-option-yes"][value=Yes]').check({ force: true });
      cy.get('select[name="attestation"]').select("None");
      for (const argument of ["capex", "opex"]) {
        cy.get(`div[title=${argument}] input`).each(($element, index) => {
          const inputNumber = 10 * index + 7;
          cy.wrap($element).type(inputNumber.toString(), { force: true });
        });
      }
      cy.get("div[title=revenue] input").eq(0).type("0");
      cy.get("div[title=revenue] input").eq(1).type("0");
      cy.get('button[name="postEUData"]', { timeout: 2 * 1000 }).should("not.be.disabled");
      cy.get('button[name="postEUData"]').click({ force: true });
    }

    it("Upload a company by filling the upload form and assure that it can be accessed via the view company page", () => {
      const companyName = "Test company XX";
      createCompanyAndGetId(companyName).then((id) => {
        cy.visitAndCheckAppMount(`/companies/${id}`);
        cy.get("body").should("contain", companyName);
      });
    });

    it("Log in as data reader, fill the company upload form and assure that upload fails because of insufficient rights", () => {
      cy.ensureLoggedIn();
      const companyName = "Test company";
      cy.visitAndCheckAppMount("/companies/upload");
      fillCompanyUploadFields(companyName);
      cy.get('button[name="postCompanyData"]').click();
      cy.get("body").should("contain", "Sorry");
    });

    it("Upload EU Taxonomy Dataset and assure that it can be viewed on the framework data view page", () => {
      const companyName = "Test non financial company";
      createCompanyAndGetId(companyName).then((id) => {
        uploadEuTaxonomyDataForNonFinancials(id);
        cy.get("body").should("contain", "success").should("contain", "EU Taxonomy Data");
        cy.get("span[title=companyId]").then(($companyID) => {
          const companyID = $companyID.text();
          cy.intercept("/api/data/eutaxonomy-non-financials/*").as("retrieveTaxonomyData");
          cy.visitAndCheckAppMount(`/companies/${companyID}/frameworks/eutaxonomy-non-financials`);
        });
        cy.wait("@retrieveTaxonomyData", { timeout: 120 * 1000 })
          .get("body")
          .should("contain", "Eligible Revenue")
          .should("not.contain", "No data has been reported");
      });
    });

    it("Log in as data reader, fill the Eu Taxonomy data upload form and assure that upload fails because of insufficient rights", () => {
      createCompanyAndGetId("Permission check company").then((id) => {
        cy.ensureLoggedIn();
        uploadEuTaxonomyDataForNonFinancials(id);
        cy.get("body").should("contain", "Sorry");
      });
    });

    it("Upload EU Taxonomy Dataset with no values for capex, opex and revenue and assure that an appropriate message is shown on the framework data view page", () => {
      const missingDataMessage = "No data has been reported";
      createCompanyAndGetId("Missing field company").then((companyId) => {
        cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/eutaxonomy-non-financials/upload`);
        cy.get('button[name="postEUData"]', { timeout: 2 * 1000 }).should("be.visible");
        cy.get('input[id="reportingObligation-option-no"][value=No]').check({ force: true });
        cy.get('select[name="attestation"]').select("None");
        cy.get('button[name="postEUData"]', { timeout: 2 * 1000 }).should("not.be.disabled");
        cy.get('button[name="postEUData"]').click({ force: true });
        cy.get("body").should("contain", "success").should("contain", "EU Taxonomy Data");
        cy.intercept("**/api/data/eutaxonomy-non-financials/*").as("retrieveTaxonomyData");
        cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/eutaxonomy-non-financials`);
        cy.wait("@retrieveTaxonomyData", { timeout: 120 * 1000 }).then(() => {
          cy.get("body").should("contain", "Eligible Revenue").should("contain", missingDataMessage);
        });
      });
    });
  }
);
