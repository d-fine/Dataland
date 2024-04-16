import SingleDataRequestComponent from "@/components/pages/SingleDataRequest.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { singleDataRequestPage } from "@sharedUtils/components/SingleDataRequest";
import { type SingleDataRequest } from "@clients/communitymanager";

describe("Component tests for the single data request page", function (): void {
  it("Check email parsing", function () {
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
      });
      cy.get("button[type='submit']").should("exist").click();
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
