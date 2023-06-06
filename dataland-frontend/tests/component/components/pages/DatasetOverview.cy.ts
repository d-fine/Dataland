import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import DatasetOverview from "@/components/pages/DatasetOverview.vue";
import SearchCompaniesForFrameworkData from "@/components/pages/SearchCompaniesForFrameworkData.vue";

describe("Component tests for the DatasetOverview page", () => {
  it("Should not display the New Dataset button to non-uploader users", () => {
    const keycloakMock = minimalKeycloakMock({});
    cy.intercept("**/api/companies?**", []);
    cy.mountWithPlugins(DatasetOverview, {
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
    cy.intercept("**/api/companies?**", []);
    cy.mountWithPlugins(DatasetOverview, {
      keycloak: keycloakMock,
    });
    cy.get("h1[data-test=noDatasetUploadedText]").should("be.visible");
    cy.get("div[data-test=datasetOverviewTable]").should("not.be.visible");
    cy.get("a[data-test=newDatasetButton]").should("have.attr", "href", "/companies/choose");
  });

  /**
   * Get the selector for a tab given by input number
   * @param tabIndex number identifying the tab
   * @returns the selector to choose a tab
   */
  function getTabSelector(tabIndex: number): string {
    return `.p-tabview-header[data-index="${tabIndex}"]`;
  }

  /**
   * Validates the tab bar identified by the input
   * @param activeTabIndex number identifying the tab bar
   * @param reviewerRole boolean indicating whether the user has reviewer rights
   */
  function validateTabBar(activeTabIndex: number, reviewerRole = false): void {
    cy.get(getTabSelector(0)).should("have.text", "AVAILABLE DATASETS");
    cy.get(getTabSelector(1)).should("have.text", "MY DATASETS");
    if (reviewerRole) {
      cy.get(getTabSelector(2)).should("have.text", "QA");
    } else {
      cy.get(getTabSelector(2)).should("not.be.visible");
    }
    const inactiveTabIndices = [];
    for (let i = 0; i < 3; i++) {
      if (i != activeTabIndex) {
        inactiveTabIndices.push(i);
      }
    }
    cy.get(getTabSelector(activeTabIndex)).should("have.class", "p-highlight");
    for (const i of inactiveTabIndices) {
      cy.get(getTabSelector(i)).should("not.have.class", "p-highlight");
    }
  }

  it("Checks that the tab-bar is rendered correctly and that clicking on 'AVAILABLE DATASETS' performs a router push", () => {
    const keycloakMock = minimalKeycloakMock({});
    cy.intercept("**/api/companies?**", []);
    cy.mountWithPlugins(DatasetOverview, {
      keycloak: keycloakMock,
    }).then((mounted) => {
      validateTabBar(1);
      cy.get(getTabSelector(0)).click();
      cy.wrap(mounted.component).its("$route.path").should("eq", "/companies");
    });
  });

  it("Checks that the tab-bar is rendered correctly and that clicking on 'MY DATASETS' performs a router push", () => {
    const keycloakMock = minimalKeycloakMock({});
    cy.intercept("**/api/companies?**", []);
    const mockDistinctValues = {
      countryCodes: [],
      sectors: [],
    };
    cy.intercept("**/api/companies/meta-information", mockDistinctValues);
    cy.mountWithPlugins(SearchCompaniesForFrameworkData, {
      keycloak: keycloakMock,
    }).then((mounted) => {
      validateTabBar(0);
      cy.get(getTabSelector(1)).click();
      cy.wrap(mounted.component).its("$route.path").should("eq", "/datasets");
    });
  });

  it("Checks that the tab-bar and clicking on 'QA' works as expected for data reviewer", () => {
    const keycloakMock = minimalKeycloakMock({
      roles: ["ROLE_USER", "ROLE_UPLOADER", "ROLE_REVIEWER"],
    });
    cy.intercept("**/api/companies?**", []);
    const mockDistinctValues = {
      countryCodes: [],
      sectors: [],
    };
    cy.intercept("**/api/companies/meta-information", mockDistinctValues);
    cy.mountWithPlugins(SearchCompaniesForFrameworkData, {
      keycloak: keycloakMock,
    }).then((mounted) => {
      validateTabBar(0, true);
      cy.get(getTabSelector(2)).click();
      cy.wrap(mounted.component).its("$route.path").should("eq", "/qualityassurance");
    });
  });
});
