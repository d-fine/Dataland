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
      cy.get('[data-test="contactEmail"]').type("example@Email.com,   , someone@else.com ");
      cy.intercept("**/single", (request) => {
        const singleDataRequest = assertDefined(request.body as SingleDataRequest);
        expect(singleDataRequest.contacts).to.deep.equal(["example@Email.com", "someone@else.com"]);
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
