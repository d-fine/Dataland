import { describeIf } from "../../support/TestUtility";
import { createCompanyAndGetId } from "../../utils/CompanyUpload";

const timeout = 120 * 1000;
describeIf(
  "As a user, I expect data that I upload for a company to be displayed correctly",
  {
    executionEnvironments: ["development"],
    dataEnvironments: ["fakeFixtures"],
  },
  function () {
    const companyIdList: Array<string> = [];
    const companyNames: Array<string> = ["eligible & total", "eligible"];
    beforeEach(() => {
      cy.ensureLoggedIn("data_uploader", Cypress.env("KEYCLOAK_UPLOADER_PASSWORD"));
    });

    /**
     * This function opens the upload page. Then the uploadFormFiller is executed. It's intended to fill the upload form.
     * Then, the upload button is clicked, and the resulting id is taken. Next, the EU Taxonomy Page is opened.
     * On this page, the euTaxonomyPageVerifier is executed - it's intended to verify contents of the EU Taxonomy page.
     * @param companyId the company ID to upload the data for
     * @param uploadFormFiller the fill method for the upload Form
     * @param euTaxonomyPageVerifier the verify method for the EU Taxonomy Page
     */
    function uploadEuTaxonomyDataAndVerifyEuTaxonomyPage(
      companyId: string,
      uploadFormFiller: () => void,
      euTaxonomyPageVerifier: () => void
    ): void {
      cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/eutaxonomy-non-financials/upload`);
      uploadFormFiller();
      cy.intercept("**/api/data/eutaxonomy-non-financials").as("postTaxonomyData");
      cy.get('button[name="postEUData"]').click({ force: true });
      cy.wait("@postTaxonomyData", { timeout: timeout }).then(() => {
        cy.get("body").should("contain", "success").should("contain", "EU Taxonomy Data");
        cy.get("span[title=dataId]").then(() => {
          cy.get("span[title=companyId]").then(($companyID) => {
            const companyID = $companyID.text();
            cy.intercept("**/api/data/eutaxonomy-non-financials/*").as("retrieveTaxonomyData");
            cy.visitAndCheckAppMount(`/companies/${companyID}/frameworks/eutaxonomy-non-financials`);
            cy.wait("@retrieveTaxonomyData", { timeout: timeout }).then(() => {
              euTaxonomyPageVerifier();
            });
          });
        });
      });
    }

    it("Create a Company providing only valid data", () => {
      companyNames.forEach((companyName) => {
        createCompanyAndGetId(companyName).then((id) => {
          companyIdList.push(id);
          cy.visitAndCheckAppMount(`/companies/${id}`);
          cy.get("body").should("contain", companyName);
        });
      });
    });

    it("Create a EU Taxonomy Dataset via upload form with total(€) and eligible(%) numbers", () => {
      const eligible = 0.67;
      const total = "15422154";
      uploadEuTaxonomyDataAndVerifyEuTaxonomyPage(
        companyIdList[0],
        () => {
          cy.get('input[name="reportingObligation"][value=Yes]').check({ force: true });
          cy.get('select[name="attestation"]').select("None");
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
        companyIdList[1],
        () => {
          cy.get('input[name="reportingObligation"][value=Yes]').check({ force: true });
          cy.get('select[name="attestation"]').select("None");
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
  }
);
