import SingleDataRequestComponent from "@/components/pages/SingleDataRequest.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { singleDataRequestPage } from "@sharedUtils/components/SingleDataRequest";
import { type SingleDataRequest } from "@clients/communitymanager";

describe("Component tests for the single data request page", function (): void {
  it("check submitting with message", function () {
    cy.mountWithPlugins(SingleDataRequestComponent, {
      keycloak: minimalKeycloakMock({}),
    }).then(() => {
      fillMandatoryFields();

      cy.get("[data-test='contactEmail']").should("exist").type("example@example");

      cy.get("[data-test='dataRequesterMessage']").should("be.disabled");
      cy.get("input[data-test='acceptConditionsCheckbox']").should("not.be.visible");

      cy.get("[data-test='contactEmail']").should("exist").type(".com,   , someone@example.com ");

      cy.get("[data-test='dataRequesterMessage']").should("be.enabled");
      cy.get("input[data-test='acceptConditionsCheckbox']").should("be.visible");

      cy.get("[data-test='dataRequesterMessage']").type("test text");

      cy.get("[data-test='conditionsNotAcceptedErrorMessage']").should("not.be.visible");

      cy.get("button[type='submit']").should("exist").click();

      cy.get("[data-test='conditionsNotAcceptedErrorMessage']").should("be.visible");
      cy.get("input[data-test='acceptConditionsCheckbox']").click();

      cy.intercept("**/single", (request) => {
        const singleDataRequest = assertDefined(request.body as SingleDataRequest);
        expect(singleDataRequest.contacts).to.deep.equal(["example@example.com", "someone@example.com"]);
        expect(singleDataRequest.message).to.deep.equal("test text");

        request.reply({
          statusCode: 200,
        });
      });

      cy.get("button[type='submit']").should("exist").click();
      cy.get("[data-test='requestStatusText']").should("contain.text", "Submitting your data request was successful.");
    });
  });

  it("check submitting without message", function (): void {
    cy.mountWithPlugins(SingleDataRequestComponent, {
      keycloak: minimalKeycloakMock({}),
    }).then(() => {
      fillMandatoryFields();

      cy.intercept("**/single", {
        statusCode: 200,
        times: 1,
      });

      cy.get("button[type='submit']").should("exist").click();
      cy.get("[data-test='requestStatusText']").should("contain.text", "Submitting your data request was successful.");
    });
  });

  it("check submitting unsuccessfully", function (): void {
    cy.mountWithPlugins(SingleDataRequestComponent, {
      keycloak: minimalKeycloakMock({}),
    }).then(() => {
      fillMandatoryFields();

      cy.intercept("**/single", {
        statusCode: 404,
        times: 1,
      });

      cy.get("button[type='submit']").should("exist").click();
      cy.get("[data-test='requestStatusText']").should(
        "contain.text",
        "The submission of your data request was unsuccessful.",
      );
    });
  });

  it("check quota surpassed modal opens", function (): void {
    cy.mountWithPlugins(SingleDataRequestComponent, {
      keycloak: minimalKeycloakMock({}),
    }).then(() => {
      fillMandatoryFields();

      cy.intercept("**/single", {
        statusCode: 403,
        times: 1,
      });

      cy.get("button[type='submit']").should("exist").click();

      cy.get("[data-test='quotaReachedModal']").should("be.visible");
      cy.get("[data-test='closeMaxRequestsReachedModalButton']").should("be.visible").click();
      cy.get("[data-test='quotaReachedModal']").should("not.exist");
    });
  });

  /**
   * Fills the mandatory fields on the single data request page
   */
  function fillMandatoryFields(): void {
    singleDataRequestPage.chooseReportingPeriod("2023");
    singleDataRequestPage.chooseFrameworkLksg();
  }
});
