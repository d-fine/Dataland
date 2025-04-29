import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import DatasetOverview from '@/components/pages/DatasetOverview.vue';
import SearchCompaniesForFrameworkData from '@/components/pages/SearchCompaniesForFrameworkData.vue';
import router from '@/router';
import { KEYCLOAK_ROLE_REVIEWER, KEYCLOAK_ROLE_UPLOADER, KEYCLOAK_ROLE_USER } from '@/utils/KeycloakRoles';

describe('Component tests for the DatasetOverview page', () => {
  it('Should not display the New Dataset button to non-uploader users', () => {
    const keycloakMock = minimalKeycloakMock({});
    cy.intercept('**/api/users/**', []);
    cy.mountWithPlugins(DatasetOverview, {
      keycloak: keycloakMock,
    });
    cy.get('h1[data-test=noDatasetUploadedText]').should('be.visible');
    cy.get('div[data-test=datasetOverviewTable]').should('not.be.visible');
    cy.get('a[data-test=newDatasetButton]').should('not.exist');
  });

  it('Should display the New Dataset button to uploaders', () => {
    const keycloakMock = minimalKeycloakMock({
      roles: [KEYCLOAK_ROLE_USER, KEYCLOAK_ROLE_UPLOADER],
    });
    cy.intercept('**/api/users/**', []);
    cy.mountWithPlugins(DatasetOverview, {
      keycloak: keycloakMock,
    });
    cy.get('h1[data-test=noDatasetUploadedText]').should('be.visible');
    cy.get('div[data-test=datasetOverviewTable]').should('not.be.visible');
    cy.get('a[data-test=newDatasetButton]').should('have.attr', 'href', '/companies/choose');
  });

  /**
   * Get the selector for a tab given by input number
   * @param tabIndex number identifying the tab
   * @returns the selector to choose a tab
   */
  function getTabSelector(tabIndex: number): string {
    return `.p-tabview-header[data-pc-index="${tabIndex}"]`;
  }

  /**
   * Validates the tab bar identified by the input
   * @param activeTabIndex number identifying the tab bar
   */
  function validateTabBar(activeTabIndex: number): void {
    cy.get(getTabSelector(0)).should('have.text', 'COMPANIES');
    cy.get(getTabSelector(1)).should('have.text', 'MY DATASETS');
    for (let i = 0; i < 3; i++) {
      cy.get(getTabSelector(i)).should((i != activeTabIndex ? 'not.' : '') + 'have.class', 'p-highlight');
    }
  }

  it("Checks that the tab-bar is rendered correctly and that clicking on 'MY DATASETS' performs a router push", () => {
    const keycloakMock = minimalKeycloakMock({});
    cy.intercept('**/api/companies?**', []);
    const mockDistinctValues = {
      countryCodes: [],
      sectors: [],
    };
    cy.intercept('**/api/companies/meta-information', mockDistinctValues);
    cy.spy(router, 'push').as('routerPush');
    cy.mountWithPlugins(SearchCompaniesForFrameworkData, {
      keycloak: keycloakMock,
      router: router,
    }).then(() => {
      validateTabBar(0);
      cy.wait(100);
      cy.get(getTabSelector(1)).click();
      cy.get('@routerPush').should('have.been.calledWith', '/datasets');
      validateTabBar(1);
    });
  });

  it("Checks that the tab-bar and clicking on 'QA' works as expected for data reviewer", () => {
    const keycloakMock = minimalKeycloakMock({
      roles: [KEYCLOAK_ROLE_USER, KEYCLOAK_ROLE_UPLOADER, KEYCLOAK_ROLE_REVIEWER],
    });
    cy.intercept('**/api/companies?**', []);
    const mockDistinctValues = {
      countryCodes: [],
      sectors: [],
    };
    cy.intercept('**/api/companies/meta-information', mockDistinctValues);
    cy.spy(router, 'push').as('routerPush');

    cy.mountWithPlugins(SearchCompaniesForFrameworkData, {
      keycloak: keycloakMock,
      router: router,
    }).then(() => {
      validateTabBar(0);
      cy.wait(100);
      cy.get(getTabSelector(2)).click();
      cy.get('@routerPush').should('have.been.called');
      validateTabBar(2);
    });
  });

  it("Checks that the tab-bar is rendered correctly and that clicking on 'COMPANIES' performs a router push", () => {
    const keycloakMock = minimalKeycloakMock({});
    cy.intercept('**/api/companies?**', []);
    cy.spy(router, 'push').as('routerPush');

    cy.mountWithPlugins(DatasetOverview, {
      keycloak: keycloakMock,
      router: router,
    }).then(() => {
      validateTabBar(1);
      cy.wait(100);
      cy.get(getTabSelector(0)).click();
      cy.get('@routerPush').should('have.been.calledWith', '/companies');
      validateTabBar(0);
    });
  });
});
