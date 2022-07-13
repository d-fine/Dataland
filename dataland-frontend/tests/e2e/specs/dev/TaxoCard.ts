import { fillCompanyUploadFields } from "../../support/commands";

const timeout = 120 * 1000;
describe("EU Taxonomy Data and Cards", function () {
  const companyIdList: Array<string> = [];
  const companyNames: Array<string> = ["eligible & total", "eligible"];
  beforeEach(() => {
    cy.restoreLoginSession("data_uploader", Cypress.env("KEYCLOAK_UPLOADER_PASSWORD"));
  });
  it("Create a Company providing only valid data", () => {
    companyNames.forEach((companyName) => {
      cy.visit("/upload");
      fillCompanyUploadFields(companyName);
      cy.intercept("**/api/companies").as("postCompany");
      cy.get('button[name="postCompanyData"]').click();
      cy.wait("@postCompany", { timeout: timeout }).then(() => {
        cy.get("body").should("contain", "success");
        cy.get("span[title=companyId]").then(($companyID) => {
          const id = $companyID.text();
          companyIdList.push(id);
          cy.visit(`/companies/${id}`);
          cy.get("body").should("contain", companyName);
        });
      });
    });
  });

  /**
   * This function opens the upload page. Then the uploadFormFiller is executed. It's intended to fill the upload form.
   * Then, the upload button is clicked, and the resulting id is taken. Next, the EU Taxonomy Page is opened.
   * On this page, the euTaxonomyPageVerifier is executed - it's intended to verify contents of the EU Taxonomy page.
   * @param uploadFormFiller the fill method for the upload Form
   * @param euTaxonomyPageVerifier the verify method for the EU Taxonomy Page
   */
  function uploadEuTaxonomyDataAndVerifyEuTaxonomyPage(
    uploadFormFiller: () => void,
    euTaxonomyPageVerifier: () => void
  ): void {
    cy.visit("/upload");
    uploadFormFiller();
    cy.intercept("**/api/data/eutaxonomies").as("postTaxonomyData");
    cy.get('button[name="postEUData"]').click({ force: true });
    cy.wait("@postTaxonomyData", { timeout: timeout }).then(() => {
      cy.get("body").should("contain", "success").should("contain", "EU Taxonomy Data");
      cy.get("span[title=dataId]").then(() => {
        cy.get("span[title=companyId]").then(($companyID) => {
          const companyID = $companyID.text();
          cy.intercept("**/api/data/eutaxonomies/*").as("retrieveTaxonomyData");
          cy.visit(`/companies/${companyID}/eutaxonomies`);
          cy.wait("@retrieveTaxonomyData", { timeout: timeout }).then(() => {
            euTaxonomyPageVerifier();
          });
        });
      });
    });
  }

  it("Create a EU Taxonomy Dataset via upload form with total(€) and eligible(%) numbers", () => {
    const eligible = 0.67;
    const total = "15422154";
    uploadEuTaxonomyDataAndVerifyEuTaxonomyPage(
      () => {
        cy.get('input[name="companyId"]').type(companyIdList[0], { force: true });
        cy.get('input[name="Reporting Obligation"][value=Yes]').check({ force: true });
        cy.get('select[name="Attestation"]').select("None");
        for (const argument of ["capex", "opex", "revenue"]) {
          cy.get(`div[title=${argument}] input[name=eligiblePercentage]`).type(eligible.toString());
          cy.get(`div[title=${argument}] input[name=totalAmount]`).type(total);
        }
      },
      () => {
        cy.get("body").should("contain", "Eligible Revenue").should("contain", `Out of total of`);
        cy.get("body")
          .should("contain", "Eligible Revenue")
          .should("contain", `${100 * eligible}%`);
        cy.get(".font-medium.text-3xl").should("contain", "€");
      }
    );
  });

  it("Create a EU Taxonomy Dataset via upload form with only eligible(%) numbers", () => {
    const eligible = 0.67;
    uploadEuTaxonomyDataAndVerifyEuTaxonomyPage(
      () => {
        cy.get('input[name="companyId"]').type(companyIdList[1], { force: true });
        cy.get('input[name="Reporting Obligation"][value=Yes]').check({ force: true });
        cy.get('select[name="Attestation"]').select("None");
        for (const argument of ["capex", "opex", "revenue"]) {
          cy.get(`div[title=${argument}] input[name=eligiblePercentage]`).type(eligible.toString());
        }
      },
      () => {
        cy.get("body")
          .should("contain", "Eligible OpEx")
          .should("contain", `${100 * eligible}%`);
        cy.get("body").should("contain", "Eligible Revenue").should("not.contain", `Out of total of`);
        cy.get(".font-medium.text-3xl").should("not.contain", "€");
      }
    );
  });
});
