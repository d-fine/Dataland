import { describeIf } from "../../support/TestUtility";
import { fillCompanyUploadFields } from "../../utils/CompanyUpload";

describeIf(
  "As a user, I want to be able to create new companies",
  {
    executionEnvironments: ["development"],
    dataEnvironments: ["fakeFixtures"],
  },
  () => {
    let companyId: string;
    beforeEach(() => {
      cy.ensureLoggedIn("data_uploader", Cypress.env("KEYCLOAK_UPLOADER_PASSWORD"));
    });

    it("cannot create a Company with no input", () => {
      cy.visitAndCheckAppMount("/companies/upload");
      cy.get('button[name="postCompanyData"]').should("be.disabled");
    });

    function uploadCompanyWithEverythingFine(companyName: string) {
      cy.visitAndCheckAppMount("/companies/upload");
      fillCompanyUploadFields(companyName);
      cy.get('button[name="postCompanyData"]').click();
    }
    function uploadEuTaxonomyDatasetWithReportingObligation() {
      cy.visitAndCheckAppMount("/companies/:$companyId/frameworks/eutaxonomy-non-financials/upload");
      cy.get('button[name="postEUData"]', { timeout: 2 * 1000 }).should("be.visible");
      cy.get('input[name="companyId"]').type(companyId, { force: true });
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
      const companyName = "Test company";
      uploadCompanyWithEverythingFine(companyName);
      cy.get("body").should("contain", "success");
      cy.get("span[title=companyId]").then(($companyID) => {
        companyId = $companyID.text();
        cy.visitAndCheckAppMount(`/companies/${companyId}`);
        cy.get("body").should("contain", companyName);
      });
    });

    it("Create a Company with insufficient rights should fail", () => {
      cy.ensureLoggedIn();
      const companyName = "Test company";
      uploadCompanyWithEverythingFine(companyName);
      cy.get("body").should("contain", "Sorry");
    });

    it("Create EU Taxonomy Dataset with Reporting Obligation and Check the Link", () => {
      const companyName = "Test non financial company";
      uploadCompanyWithEverythingFine(companyName);
      cy.get("span[title=companyId]").then(($companyID) => {
        uploadEuTaxonomyDatasetWithReportingObligation();
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
      cy.ensureLoggedIn();
      uploadEuTaxonomyDatasetWithReportingObligation();
      cy.get("body").should("contain", "Sorry");
    });

    it("Create EU Taxonomy Dataset without Reporting Obligation", () => {
      cy.visitAndCheckAppMount("/companies/:$companyId/frameworks/eutaxonomy-non-financials/upload");
      cy.get('button[name="postEUData"]', { timeout: 2 * 1000 }).should("be.visible");
      cy.get('input[name="companyId"]').type(companyId, { force: true });
      cy.get('input[id="reportingObligation-option-no"][value=No]').check({ force: true });
      cy.get('select[name="attestation"]').select("None");
      cy.get('button[name="postEUData"]', { timeout: 2 * 1000 }).should("not.be.disabled");
      cy.get('button[name="postEUData"]').click({ force: true });
      cy.get("body").should("contain", "success").should("contain", "EU Taxonomy Data");
      cy.get("span[title=dataId]").then(($dataID) => {
        const dataId = $dataID.text();
        cy.intercept("**/api/data/eutaxonomy/nonfinancials/*").as("retrieveTaxonomyData");
        cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/eutaxonomy`);
        cy.wait("@retrieveTaxonomyData", { timeout: 120 * 1000 }).then(() => {
          cy.get("body").should("contain", "Eligible Revenue").should("contain", "No data has been reported");
        });
      });
    });
  }
);
