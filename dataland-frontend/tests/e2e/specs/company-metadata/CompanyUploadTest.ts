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

    it("cannot create a Company with no input", () => {
      cy.visitAndCheckAppMount("/companies/upload");
      cy.get('button[name="postCompanyData"]').should("be.disabled");
    });

    function uploadEuTaxonomyDatasetWithReportingObligation(companyId: string) {
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

    it("Create a Company when everything is fine", () => {
      const companyName = "Test company XX";
      createCompanyAndGetId(companyName).then((id) => {
        cy.visitAndCheckAppMount(`/companies/${id}`);
        cy.get("body").should("contain", companyName);
      });
    });

    it("Create a Company with insufficient rights should fail", () => {
      cy.ensureLoggedIn();
      const companyName = "Test company";
      cy.visitAndCheckAppMount("/companies/upload");
      fillCompanyUploadFields(companyName);
      cy.get('button[name="postCompanyData"]').click();
      cy.get("body").should("contain", "Sorry");
    });

    it("Create EU Taxonomy Dataset with Reporting Obligation and Check the Link", () => {
      const companyName = "Test non financial company";
      createCompanyAndGetId(companyName).then((id) => {
        uploadEuTaxonomyDatasetWithReportingObligation(id);
        cy.get("body").should("contain", "success").should("contain", "EU Taxonomy Data");
        cy.get("span[title=dataId]").then(() => {
          cy.get("span[title=companyId]").then(($companyID) => {
            const companyID = $companyID.text();
            cy.intercept("/api/data/eutaxonomy/nonfinancials/*").as("retrieveTaxonomyData");
            cy.visitAndCheckAppMount(`/companies/${companyID}/frameworks/eutaxonomy`);
          });
          cy.wait("@retrieveTaxonomyData", { timeout: 120 * 1000 })
            .get("body")
            .should("contain", "Eligible Revenue")
            .should("not.contain", "No data has been reported");
        });
      });
    });

    it("Create EU Taxonomy Dataset with Reporting Obligation and insufficient rights should fail", () => {
      createCompanyAndGetId("Permission check company").then((id) => {
        cy.ensureLoggedIn();
        uploadEuTaxonomyDatasetWithReportingObligation(id);
        cy.get("body").should("contain", "Sorry");
      });
    });

    it("Create EU Taxonomy Dataset without Reporting Obligation", () => {
      createCompanyAndGetId("Missing field company").then((id) => {
        cy.visitAndCheckAppMount(`/companies/${id}/frameworks/eutaxonomy-non-financials/upload`);
        cy.get('button[name="postEUData"]', { timeout: 2 * 1000 }).should("be.visible");
        cy.get('input[id="reportingObligation-option-no"][value=No]').check({ force: true });
        cy.get('select[name="attestation"]').select("None");
        cy.get('button[name="postEUData"]', { timeout: 2 * 1000 }).should("not.be.disabled");
        cy.get('button[name="postEUData"]').click({ force: true });
        cy.get("body").should("contain", "success").should("contain", "EU Taxonomy Data");
        cy.get("span[title=dataId]").then(($dataID) => {
          const dataId = $dataID.text();
          cy.intercept("**/api/data/eutaxonomy/nonfinancials/*").as("retrieveTaxonomyData");
          cy.visitAndCheckAppMount(`/companies/${id}/frameworks/eutaxonomy`);
          cy.wait("@retrieveTaxonomyData", { timeout: 120 * 1000 }).then(() => {
            cy.get("body").should("contain", "Eligible Revenue").should("contain", "No data has been reported");
          });
        });
      });
    });
  }
);
