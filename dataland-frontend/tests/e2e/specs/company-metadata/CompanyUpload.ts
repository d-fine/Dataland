import { describeIf } from "@e2e/support/TestUtility";
import {
  uploadCompanyViaFormAndGetId,
  fillCompanyUploadFields,
  uploadCompanyViaApi,
  generateDummyCompanyInformation,
} from "@e2e/utils/CompanyUpload";
import { uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";

describeIf(
  "As a user, I want to be able to upload new companies via an upload form if I have the rights",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
    dataEnvironments: ["fakeFixtures"],
  },
  () => {
    it(
      "Check if post company button is disabled before filling the upload form, then fill and submit the form to " +
        "upload a company and assure that it can be accessed via the company view page",
      () => {
        cy.ensureLoggedIn(uploader_name, uploader_pw);
        const companyName = "Test company XX";
        uploadCompanyViaFormAndGetId(companyName).then((companyId) => {
          cy.visitAndCheckAppMount(`/companies/${companyId}`);
          cy.get("body").should("contain", companyName);
        });
      }
    );

    it(
      "Log in as data reader, fill the company upload form and the Eu Taxonomy data upload form and assure that " +
        "both upload form submits fail because of insufficient rights",
      () => {
        cy.ensureLoggedIn();
        const companyName = "Test company";
        cy.visitAndCheckAppMount("/companies/upload");
        fillCompanyUploadFields(companyName);
        cy.get('button[name="postCompanyData"]').click();
        cy.get("body").should("contain", "Sorry");
        getKeycloakToken(uploader_name, uploader_pw).then((token) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation("Permission check company")).then(
            (storedCompany) => {
              cy.visitAndCheckAppMount(
                `/companies/${storedCompany.companyId}/frameworks/eutaxonomy-non-financials/upload`
              );
              cy.get("select[name=assurance]").select("Limited Assurance");
              cy.get('input[id="reportingObligation-option-yes"][value=Yes]').check({
                force: true,
              });
              cy.intercept("**/api/data/eutaxonomy-non-financials").as("postCompanyAssociatedData");
              cy.get('button[name="postEUData"]').click({ force: true });
              cy.wait("@postCompanyAssociatedData").get("body").should("contain", "Sorry");
            }
          );
        });
      }
    );
  }
);
