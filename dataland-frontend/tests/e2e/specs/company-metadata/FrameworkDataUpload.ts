import { describeIf } from "@e2e/support/TestUtility";
import { uploadCompanyViaApi, generateDummyCompanyInformation } from "@e2e/utils/CompanyUpload";
import { uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { uploadEuTaxonomyDataForNonFinancialsViaForm } from "@e2e/utils/EuTaxonomyNonFinancialsUpload";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { LONG_TIMEOUT_IN_MS } from "@e2e/utils/Constants";

describeIf(
  "As a user, I want to be able to upload new framework data via an upload form if I have the rights",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
    dataEnvironments: ["fakeFixtures"],
  },
  () => {
    it(
      "Upload EU Taxonomy Dataset via form with no values for revenue and assure that it can be viewed on the framework " +
        "data view page with an appropriate message shown for the missing revenue data",
      () => {
        cy.ensureLoggedIn(uploader_name, uploader_pw);
        const companyName = "Missing field company";
        const missingDataMessage = "No data has been reported";
        getKeycloakToken(uploader_name, uploader_pw).then((token) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation(companyName)).then((storedCompany) => {
            uploadEuTaxonomyDataForNonFinancialsViaForm(storedCompany.companyId);
            cy.intercept("**/api/data/eutaxonomy-non-financials/*").as("retrieveTaxonomyData");
            cy.visitAndCheckAppMount(`/companies/${storedCompany.companyId}/frameworks/eutaxonomy-non-financials`);
            cy.wait("@retrieveTaxonomyData", { timeout: LONG_TIMEOUT_IN_MS }).then(() => {
              cy.get("h1[class='mb-0']").contains(companyName);
              cy.get("body").should("contain", "Eligible Revenue").should("contain", missingDataMessage);
            });
          });
        });
      }
    );
  }
);
