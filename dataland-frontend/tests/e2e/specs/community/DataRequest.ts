import { admin_name, admin_pw } from "@e2e/utils/Cypress";
import { type Interception } from "cypress/types/net-stubbing";
import { type BulkDataRequestResponse } from "@clients/communitymanager";

describe("As a user I want to be able to request data", () => {
  beforeEach(() => {
    cy.ensureLoggedIn(admin_name, admin_pw);
    cy.visitAndCheckAppMount("/requests");
  });

  it("When identifiers are accepted and rejected", () => {
    cy.intercept("POST", "**/community/requests").as("postRequestData");

    checksBasicValidation();
    choseFramewors();

    cy.get("textarea[name='listOfCompanyIdentifiers']")
      .type("549300VJTTKH8P0QWG18, 12345incorrectNumber")
      .get("button[type='submit']")
      .should("exist")
      .click();

    cy.wait("@postRequestData", { timeout: Cypress.env("short_timeout_in_ms") as number }).then((interception) => {
      checkIfIdentifiersProperlyDisplayed(interception);
    });

    cy.get('[data-test="submittingSuccededMessage"] [data-test="someIdentifiersPassed"]')
      .should("exist")
      .get("p.red-text")
      .contains("However, some identifiers couldn’t be recognised.");

    cy.get('[data-test="resetFormButton"]')
      .click()
      .get('[data-test="addedFrameworks"]')
      .contains("No Frameworks added yet");
  });

  it("When identifiers are accepted", () => {
    cy.intercept("POST", "**/community/requests").as("postRequestData");

    checksBasicValidation();
    choseFramewors();

    cy.get("textarea[name='listOfCompanyIdentifiers']")
      .type("549300VJTTKH8P0QWG18")
      .get("button[type='submit']")
      .should("exist")
      .click();

    cy.wait("@postRequestData", { timeout: Cypress.env("short_timeout_in_ms") as number }).then((interception) => {
      checkIfIdentifiersProperlyDisplayed(interception);
    });

    cy.get('[data-test="submittingSuccededMessage"] [data-test="someIdentifiersPassed"]')
      .should("exist")
      .get("p")
      .contains("All identifiers have been submitted successfully.");
  });

  it("When identifiers are rejected", () => {
    cy.intercept("POST", "**/community/requests").as("postRequestData");

    checksBasicValidation();
    choseFramewors();

    cy.get("textarea[name='listOfCompanyIdentifiers']")
      .type("12345incorrectNumber")
      .get("button[type='submit']")
      .should("exist")
      .click();

    cy.get('[data-test="failMessage"]')
      .should("exist")
      .contains("All provided company identifiers have an invalid format.");
  });

  /**
   * Chose framewors
   */
  function choseFramewors(): void {
    cy.get('[data-test="selectFrameworkSelect"] .p-multiselect')
      .should("exist")
      .click()
      .get(".p-multiselect-panel ul.p-multiselect-items li.p-multiselect-item")
      .should("have.length", 6)
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
      cy.get('[data-test="rejectedCompanyIdentifiers"] span[data-test="identifier"]').should(
        "have.length",
        rejectedIdentifiers.length,
      );
      cy.get('[data-test="acceptedCompanyIdentifiers"] span[data-test="identifier"]').should(
        "have.length",
        acceptedIdentifiers.length,
      );
    }
  }

  /**
   * Checks basic validation
   */
  function checksBasicValidation(): void {
    cy.get("button[type='submit']")
      .should("exist")
      .click()
      .get("div[data-test='selectFrameworkDiv'] li[data-message-type='validation']")
      .should("be.visible")
      .should("contain.text", "Select at least one framework")
      .get("div[data-test='provideIdentifiers'] li[data-message-type='validation']")
      .should("be.visible")
      .should("contain.text", "Provide at least one identifier");
  }
});
