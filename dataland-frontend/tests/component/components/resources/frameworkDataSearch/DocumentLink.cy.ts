import DocumentLink from "@/components/resources/frameworkDataSearch/DocumentLink.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";

describe("check that the progress spinner works correctly for the document link component", function (): void {
  it("Check that there are no icons before and after triggering a download", function (): void {
    cy.intercept("**/documents/dummyFile**", {
      statusCode: 200,
    }).as("downloadComplete");
    cy.mountWithPlugins(DocumentLink, {
      keycloak: minimalKeycloakMock({}),
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      props: {
        downloadName: "Test",
        fileReference: "dummyFileReference",
      },
      data() {
        return {
          percentCompleted: undefined,
        };
      },
    }).then(() => {
      validateNoIcons();
      cy.get("[data-test='download-link']").should("exist").click();
      cy.wait("@downloadComplete").then(() => {
        validateNoIcons();
      });
    });
  });
  it("Check that Download Progress Spinner appears if the prop changes", function (): void {
    cy.mountWithPlugins(DocumentLink, {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      props: {
        downloadName: "Test",
        fileReference: "dummyFileReference",
      },
      data() {
        return {
          percentCompleted: undefined,
        };
      },
    }).then((mounted) => {
      validateNoIcons();

      void mounted.wrapper
        .setData({
          percentCompleted: 50,
        })
        .then(() => {
          cy.get('[data-test="spinner-icon"]').should("exist");
          cy.get("[data-test='percentage-text']").should("exist").should("have.text", "50%");
          cy.get("[data-test='checkmark-icon']").should("not.exist");
        });
    });
  });

  it("Check that Download Progress Spinner disappears and the checkmark appears", function (): void {
    cy.mountWithPlugins(DocumentLink, {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      props: {
        downloadName: "Test",
        fileReference: "dummyFileReference",
      },
      data() {
        return {
          percentCompleted: 50,
        };
      },
    }).then((mounted) => {
      cy.get('[data-test="spinner-icon"]').should("exist");
      cy.get("[data-test='percentage-text']").should("exist").should("have.text", "50%");
      cy.get("[data-test='checkmark-icon']").should("not.exist");
      void mounted.wrapper
        .setData({
          percentCompleted: 100,
        })
        .then(() => {
          cy.get("[data-test='checkmark-icon']").should("exist");
          cy.get('[data-test="spinner-icon"]').should("not.exist");
          cy.get("[data-test='percentage-text']").should("not.exist");
        });
    });
  });
  it("Check that Download Progress Checkmark disappears again", function (): void {
    cy.mountWithPlugins(DocumentLink, {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      props: {
        downloadName: "Test",
        fileReference: "dummyFileReference",
      },
      data() {
        return {
          percentCompleted: 100,
        };
      },
    }).then((mounted) => {
      cy.get('[data-test="spinner-icon"]').should("not.exist");
      cy.get("[data-test='percentage-text']").should("not.exist");
      cy.get("[data-test='checkmark-icon']").should("exist");
      void mounted.wrapper
        .setData({
          percentCompleted: undefined,
        })
        .then(() => {
          validateNoIcons();
        });
    });
  });
});
/**
 * Checks that no icons exist
 */
function validateNoIcons(): void {
  cy.get('[data-test="spinner-icon"]').should("not.exist");
  cy.get("[data-test='percentage-text']").should("not.exist");
  cy.get("[data-test='checkmark-icon']").should("not.exist");
}
