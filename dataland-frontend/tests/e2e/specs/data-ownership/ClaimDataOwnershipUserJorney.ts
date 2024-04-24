import { describeIf } from "@e2e/support/TestUtility";
import { admin_name, admin_pw, reader_name, reader_pw } from "@e2e/utils/Cypress";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { getKeycloakToken, logout } from "@e2e/utils/Auth";

describeIf(
  "As a non-authenticated user, I expect to be redirect to the company cockpit page, after claiming owner ship and logging in",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
  },
  () => {
    it("Upload a company, logout, claim ownership and log-in, verify the correct company cockpit", () => {
      cy.ensureLoggedIn(admin_name, admin_pw);
      const uniqueCompanyMarker = Date.now().toString();
      const testCompanyName = "Company-Created-In-Claim-Data-Owner-Test-" + uniqueCompanyMarker;
      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName)).then((storedCompany) => {
          checkClaimOwnership(storedCompany.companyId, testCompanyName);
        });
      });
    });
  },
);
/**
 * This method verifies that a non-authenticated user is redirect to the company cockpit page after claiming ownership
 * @param companyId companyId
 * @param companyName company Name
 */
export function checkClaimOwnership(companyId: string, companyName: string): void {
  logout();
  cy.visitAndCheckAppMount("/companies/" + companyId);
  cy.get("[data-test='claimOwnershipPanelLink']").should("have.text", " Claim company dataset ownership. ").click();
  cy.contains("button", "LOGIN TO ACCOUNT").should("exist").should("be.visible").click();

  cy.get("#username")
    .should("exist")
    .type(reader_name, { force: true })
    .get("#password")
    .should("exist")
    .type(reader_pw, { force: true })

    .get("#kc-login")
    .should("exist")
    .click();
  cy.get("[data-test='companyNameTitle']").should("have.text", companyName);
}
