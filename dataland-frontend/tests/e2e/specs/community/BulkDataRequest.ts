import { admin_name, admin_pw } from "@e2e/utils/Cypress";
import { type Interception } from "cypress/types/net-stubbing";
import { type BulkDataRequestResponse } from "@clients/communitymanager";
import { describeIf } from "@e2e/support/TestUtility";
import { DataTypeEnum } from "@clients/backend";

describeIf(
  "As a user I want to be able to conduct a bulk request request",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
  },
  () => {
    beforeEach(() => {
      cy.ensureLoggedIn(admin_name, admin_pw);
      cy.visitAndCheckAppMount("/bulkdatarequest");
    });

    it("When identifiers are accepted and rejected", () => {
      cy.intercept("POST", "**/community/requests/bulk").as("postRequestData");

      checksBasicValidation();
      chooseReportingPeriod();
      chooseFrameworks();

      cy.get("textarea[name='listOfCompanyIdentifiers']")
        .type("549300VJTTKH8P0QWG18, 12345incorrectNumber")
        .get("button[type='submit']")
        .should("exist")
        .click();

      cy.wait("@postRequestData", { timeout: Cypress.env("short_timeout_in_ms") as number }).then((interception) => {
        checkIfIdentifiersProperlyDisplayed(interception);
      });

      cy.get('[data-test="acceptedIdentifiers"] [data-test="identifiersHeading"]').contains("1 REQUESTED IDENTIFIER");
      cy.get('[data-test="rejectedIdentifiers"] [data-test="identifiersHeading"]').contains("1 REJECTED IDENTIFIER");
    });

    it("When identifiers are accepted", () => {
      cy.intercept("POST", "**/community/requests/bulk").as("postRequestData");

      checksBasicValidation();
      chooseReportingPeriod();
      chooseFrameworks();

      cy.get("textarea[name='listOfCompanyIdentifiers']")
        .type("549300VJTTKH8P0QWG18")
        .get("button[type='submit']")
        .should("exist")
        .click();

      cy.wait("@postRequestData", { timeout: Cypress.env("short_timeout_in_ms") as number }).then((interception) => {
        checkIfIdentifiersProperlyDisplayed(interception);
      });

      cy.get('[data-test="acceptedIdentifiers"]')
        .should("exist")
        .get('[data-test="identifiersHeading"')
        .contains("1 REQUESTED IDENTIFIER");

      cy.get('[data-test="requestStatusText"]').should("exist").contains("Success");
    });

    it("When identifiers are rejected", () => {
      checksBasicValidation();
      chooseFrameworks();

      cy.get("textarea[name='listOfCompanyIdentifiers']")
        .type("12345incorrectNumber")
        .get("button[type='submit']")
        .should("exist")
        .click();

      cy.get('[data-test="selectedIdentifiersUnsuccessfulSubmit"]')
        .should("exist")
        .get('[data-test="identifiersHeading"')
        .contains("SELECTED IDENTIFIERS");

      cy.get('[data-test="requestStatusText"]').should("exist").contains("Request Unssuccessful");
    });

    /**
     * Choose reporting periods
     */
    function chooseReportingPeriod(): void {
      cy.get('[data-test="reportingPeriodsDiv"] div[data-test="toggleChipsFormInput"]')
        .should("exist")
        .get('[data-test="toggle-chip"')
        .should("have.length", 4)
        .first()
        .click()
        .should("have.class", "toggled");

      cy.get("div[data-test='reportingPeriodsDiv'] p[data-test='reportingPeriodErrorMessage'").should("not.exist");
    }

    /**
     * Chose frameworks
     */
    function chooseFrameworks(): void {
      const numberOfFrameworks = Object.keys(DataTypeEnum).length;
      cy.get('[data-test="selectFrameworkSelect"] .p-multiselect')
        .should("exist")
        .click()
        .get(".p-multiselect-panel ul.p-multiselect-items li.p-multiselect-item")
        .should("have.length", numberOfFrameworks)
        .eq(3)
        .click()
        .get("div[data-test='addedFrameworks'] span")
        .should("have.length", 1);
    }

    /**
     * Checks whether identifiers are displayed correctly on boxes
     * @param interception request
     */
    function checkIfIdentifiersProperlyDisplayed(interception: Interception): void {
      if (interception.response !== undefined) {
        const rejectedIdentifiers = (interception.response.body as BulkDataRequestResponse).rejectedCompanyIdentifiers;
        const acceptedIdentifiers = (interception.response.body as BulkDataRequestResponse).acceptedCompanyIdentifiers;
        if (rejectedIdentifiers.length > 0) {
          cy.get('[data-test="rejectedIdentifiers"] [data-test="identifiersList"]')
            .children()
            .should("have.length", rejectedIdentifiers.length);
        }
        if (acceptedIdentifiers.length > 0) {
          cy.get('[data-test="acceptedIdentifiers"] [data-test="identifiersList"]').should(
            "have.length",
            acceptedIdentifiers.length,
          );
        }
      }
    }

    /**
     * Checks basic validation
     */
    function checksBasicValidation(): void {
      cy.get("button[type='submit']").should("exist").click();

      cy.get("div[data-test='reportingPeriodsDiv'] p[data-test='reportingPeriodErrorMessage'")
        .should("be.visible")
        .should("contain.text", "Select at least one reporting period.");

      cy.get("div[data-test='selectFrameworkDiv'] li[data-message-type='validation']")
        .should("be.visible")
        .should("contain.text", "Select at least one framework");

      cy.get("div[data-test='selectIdentifiersDiv'] li[data-message-type='validation']")
        .should("be.visible")
        .should("contain.text", "Provide at least one identifier");
    }
  },
);
