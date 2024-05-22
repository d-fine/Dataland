import DownloadProgressSpinner from "@/components/resources/frameworkDataSearch/DownloadProgressSpinner.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";

describe("Component test for DownloadProgressSpinner", () => {
  it("Check that Download Progress Spinner and Checkmark appear and disappear correctly", () => {
    cy.mountWithPlugins(DownloadProgressSpinner, {
      keycloak: minimalKeycloakMock({}),
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      props: {
        percentCompleted: 50,
      },
    }).then((mounted) => {
      cy.get("[data-test='spinner-icon']").should("not.exist");
      cy.get("[data-test='percentage-text']").should("not.exist");
      cy.get("[data-test='checkmark-icon']").should("not.exist");

      void mounted.wrapper.setProps({
        percentCompleted: 50,
      });
      cy.get("[data-test='percentage-text']").should("be.visible").should("have.text", "50%");
      cy.get("[data-test='spinner-icon']").should("be.visible");
      cy.get("[data-test='checkmark-icon']").should("not.exist");

      void mounted.wrapper.setProps({
        percentCompleted: 100,
      });
      cy.get("[data-test='spinner-icon']").should("not.exist");
      cy.get("[data-test='percentage-text']").should("not.exist");
      cy.get("[data-test='checkmark-icon']").should("be.visible");

      void mounted.wrapper.setProps({
        percentCompleted: undefined,
      });
      cy.wait(2000);
      cy.get("[data-test='spinner-icon']").should("not.exist");
      cy.get("[data-test='percentage-text']").should("not.exist");
      cy.get("[data-test='checkmark-icon']").should("not.exist");
    });
  });
});
