import { describeIf } from "@e2e/support/TestUtility";
import { uploadCompanyViaFormAndGetId } from "@e2e/utils/CompanyUpload";
import { uploader_name, uploader_pw } from "@e2e/utils/Cypress";

describeIf(
  "As a user, I want to be able to upload new companies via an upload form if I have the rights",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
    dataEnvironments: ["fakeFixtures"],
  },
  () => {
    it("Check if filling and submitting the form is possible and assure that it can be accessed via the company view page", () => {
      cy.ensureLoggedIn(uploader_name, uploader_pw);
      const companyName = "Test company XX";
      cy.visitAndCheckAppMount("/companies/upload");
      uploadCompanyViaFormAndGetId(companyName).then((companyId) => {
        cy.visitAndCheckAppMount(`/companies/${companyId}`);
        cy.get("body").should("contain", companyName);
      });
    });
    /*
    it("Log in as data reader, fill the company upload form and assure that upload fails because of insufficient rights", () => {
      cy.ensureLoggedIn();
      const companyName = "Test company";
      cy.visitAndCheckAppMount("/companies/upload");
      cy.contains("div", "Create a Company");
      fillCompanyUploadFields(companyName);*/
    // cy.intercept("**/api/companies").as("postCompany");
    /*
      cy.get('button[name="addCompany"]').click();
      cy.wait("@postCompany");
      cy.get("body").should("contain", "Sorry");
    }); */ // TODO this test does not make sense anymore since the upload form won't appear if you are logged in as reader
    // TODO we could change this test so that it checks if you get an error message if you visit the company upload page as reader
  }
);
