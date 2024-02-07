import { admin_name, admin_pw } from "@e2e/utils/Cypress";
import { type Interception } from "cypress/types/net-stubbing";
import { type SingleDataRequest } from "@clients/communitymanager";
import { describeIf } from "@e2e/support/TestUtility";
import { CompanyIdAndName, CompanyInformation, DataTypeEnum, type StoredCompany } from "@clients/backend";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";

describeIf(
  "As a user I want to be able to navigate to the single data request page and submit a request",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
  },
  () => {
    const uniqueCompanyMarker = Date.now().toString();
    const testCompanyName = "Company-for-single-data-request" + uniqueCompanyMarker;
    let testStoredCompany: StoredCompany;
    beforeEach(() => {
      cy.ensureLoggedIn(admin_name, admin_pw);
      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName)).then((storedCompany) => {
          testStoredCompany = storedCompany;
        });
      });
    });

    it("Fill out the request page and check correct validation, request and success message", () => {
      cy.intercept("POST", "**/community/requests/single").as("postRequestData");
      cy.visitAndCheckAppMount(`/singleDataRequest/${testStoredCompany.companyId}`);
      checkCompanyInfoSheet();
      checkValidation();
      chooseReportingPeriod();
      chooseFramework();

      cy.get('[data-test="contactEmail"]').type("example@Email.com");
      cy.get('[data-test="dataRequesterMessage"]').type("Some message");

      cy.get("button[type='submit']").should("exist").click();

      cy.wait("@postRequestData", { timeout: Cypress.env("short_timeout_in_ms") as number }).then((interception) => {
        checkIfRequestContentIsValid(interception);
        //TODO:check existence of popup message
        //TODO:check url to confirm the user is taken back to the company cockpit
      });
    });
    it("Crate a company and navigate to its single request page via the company cockpit", () => {
      cy.intercept("**/api/companies/singledatarequest/" + testStoredCompany.companyId).as("goToSingleRequestPage");
      cy.get("SingleDataRequestButton").should("exist").click();
      cy.wait("@goToSingleRequestPage", { timeout: Cypress.env("medium_timeout_in_ms") as number });
      cy.url().should("contain", `/companies/singledatarequest/${testStoredCompany.companyId}`);
    });

    function checkIfRequestContentIsValid(interception: Interception): void {
      if (interception.request !== undefined) {
        const requestBody = interception.request.body as SingleDataRequest;
        expect(requestBody.companyIdentifier).to.equal(testStoredCompany.companyId);
        expect(requestBody.contactList).to.include("example@Email.com");
        assert.equal(requestBody.message, "Some message");
      }
    }

    /**
     * Choose reporting periods
     */
    function chooseReportingPeriod(): void {
      cy.get('[data-test="reportingPeriods"] div[data-test="toggleChipsFormInput"]')
        .should("exist")
        .get('[data-test="toggle-chip"')
        .first()
        .click()
        .should("have.class", "toggled");

      cy.get("div[data-test='reportingPeriods'] p[data-test='reportingPeriodErrorMessage'").should("not.exist");
    }

    /**
     * Choose a framework
     */
    function chooseFramework(): void {
      const numberOfFrameworks = Object.keys(DataTypeEnum).length;
      cy.get('[data-test="selectFramework"]')
        .should("exist")
        .get('[data-type="select"]')
        .should("exist")
        .click()
        .get('[data-test="datapoint-framework"]')
        .select(3);
      cy.get('[data-test="datapoint-framework"]')
        .children()
        .should("have.length", numberOfFrameworks + 1);

      cy.get("[data-test='datapoint-framework']").should("exist");
    }

    /**
     * Checks basic validation
     */
    function checkValidation(): void {
      cy.get("button[type='submit']").should("exist").click();
      cy.get("div[data-test='reportingPeriods'] p[data-test='reportingPeriodErrorMessage'")
        .should("be.visible")
        .should("contain.text", "Select at least one reporting period.");

      cy.get("div[data-test='selectFramework'] li[data-message-type='validation']")
        .should("be.visible")
        .should("contain.text", "Select a framework");
    }

    /**
     * Checks if the information on the company banner is correct
     */
    function checkCompanyInfoSheet(): void {
      cy.get("[data-test='companyNameTitle']").should("contain.text", testCompanyName);
    }
  },
);
