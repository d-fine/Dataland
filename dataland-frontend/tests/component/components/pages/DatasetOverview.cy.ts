import { minimalKeycloakMock } from "../../testUtils/keycloak";
import DatasetOverview from "@/components/pages/DatasetOverview.vue";

describe("Component tests for the DatasetOverview page", () => {
  it("Should not display the New Dataset button to non-uploader users", () => {
    const keycloakMock = minimalKeycloakMock({});
    cy.intercept("**/api/companies**", []);
    cy.mount(DatasetOverview, {
      keycloak: keycloakMock,
    });
    cy.get("h1[data-test=noDatasetUploadedText]").should("be.visible");
    cy.get("div[data-test=datasetOverviewTable]").should("not.be.visible");
    cy.get("a[data-test=newDatasetButton]").should("not.exist");
  });

  it("Should display the New Dataset button to uploaders", () => {
    const keycloakMock = minimalKeycloakMock({
      roles: ["ROLE_USER", "ROLE_UPLOADER"],
    });
    cy.intercept("**/api/companies**", []);
    cy.mount(DatasetOverview, {
      keycloak: keycloakMock,
    });
    cy.get("h1[data-test=noDatasetUploadedText]").should("be.visible");
    cy.get("div[data-test=datasetOverviewTable]").should("not.be.visible");
    cy.get("a[data-test=newDatasetButton]").should("have.attr", "href", "/companies/choose");
  });
});
