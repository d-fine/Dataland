import DatasetsTabMenu from '@/components/general/DatasetsTabMenu.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { CompanyRole, type CompanyRoleAssignmentExtended } from '@clients/communitymanager';
import { getMountingFunction } from '@ct/testUtils/Mount';
import { KEYCLOAK_ROLE_ADMIN, KEYCLOAK_ROLE_REVIEWER, KEYCLOAK_ROLE_USER } from '@/utils/KeycloakRoles';
import { ref } from 'vue';
import router from '@/router';

describe('Component tests for the tab used by logged-in users to switch pages', () => {
  enum AlwaysVisibleTabs {
    Companies = 'COMPANIES',
    MyDatasets = 'MY DATASETS',
    MyPortfolios = 'MY PORTFOLIOS',
    MyDataRequests = 'MY DATA REQUESTS',
  }

  enum RoleBasedTabs {
    Qa = 'QA',
    DataAccessRequests = 'DATA REQUESTS FOR MY COMPANIES',
    AllDataRequests = 'ALL DATA REQUESTS',
  }

  const dummyUserId = 'mock-user-id';
  const dummyFirstName = 'mock-first-name';
  const dummyEmail = 'mock@Company.com';
  const dummyCompanyId = '550e8400-e29b-11d4-a716-446655440000';

  /**
   * Mounts the Dataland navigation tab with a specific authentication
   * @param keycloakRoles of the logged-in user that sees the tab
   * @param companyRoleAssignments for the logged-in user that sees the tab
   */
  function mountDatasetsTabMenuWithAuthentication(
    keycloakRoles: string[],
    companyRoleAssignments: CompanyRoleAssignmentExtended[]
  ): void {
    getMountingFunction({
      keycloak: minimalKeycloakMock({
        authenticated: true,
        roles: keycloakRoles,
        userId: dummyUserId,
      }),
      router: router,
    })(
      //@ts-ignore
      DatasetsTabMenu,
      {
        global: {
          provide: {
            companyRoleAssignments: ref(companyRoleAssignments),
          },
        },
      }
    ).then(() => {
      void router.push('/portfolios');
      assertPortfoliosTabIsHighlighted();
    });
  }

  /**
   * Checks if the tab with the defined text is visible or not
   * @param textInTab that shall be checked
   * @param isTabExpectedToBeVisible describes if the tab is expected to be visible on the navigation bar
   */
  function isTabVisible(textInTab: string, isTabExpectedToBeVisible: boolean): void {
    const visibilityAssertion = isTabExpectedToBeVisible ? 'be.visible' : 'not.be.visible';
    cy.get('[data-pc-name="tablist"]').contains(textInTab).should(visibilityAssertion);
  }

  /**
   * Asserts that the 'Portfolios' tab is highlighted
   */
  function assertPortfoliosTabIsHighlighted(): void {
    cy.get(`[data-pc-name="tab"][data-p-active="true"]`).contains(AlwaysVisibleTabs.MyPortfolios).should('exist');
  }

  /**
   * Asserts that all standard tabs are visible in the navigation bar
   */
  function assertThatStandardTabsAreAllVisible(): void {
    for (const tabText of Object.values(AlwaysVisibleTabs)) {
      isTabVisible(tabText, true);
    }
  }

  it('Validate tabs for a logged-in Dataland-Reader with no company role assignments', function () {
    mountDatasetsTabMenuWithAuthentication([KEYCLOAK_ROLE_USER], []);
    assertThatStandardTabsAreAllVisible();
    for (const tabText of Object.values(RoleBasedTabs)) {
      isTabVisible(tabText, false);
    }
  });

  it('Validate tabs for a logged-in Dataland-Reader with company ownership', function () {
    const companyRoleAssignments: CompanyRoleAssignmentExtended[] = [
      {
        companyRole: CompanyRole.CompanyOwner,
        companyId: dummyCompanyId,
        userId: dummyUserId,
        firstName: dummyFirstName,
        email: dummyEmail,
      },
    ];
    mountDatasetsTabMenuWithAuthentication([KEYCLOAK_ROLE_USER], companyRoleAssignments);
    assertThatStandardTabsAreAllVisible();

    isTabVisible(RoleBasedTabs.Qa, false);
    isTabVisible(RoleBasedTabs.AllDataRequests, false);

    isTabVisible(RoleBasedTabs.DataAccessRequests, true);
  });

  it('Validate tabs for a logged-in Dataland-Reviewer with no company role assignments', function () {
    mountDatasetsTabMenuWithAuthentication([KEYCLOAK_ROLE_REVIEWER], []);
    assertThatStandardTabsAreAllVisible();

    isTabVisible(RoleBasedTabs.AllDataRequests, false);
    isTabVisible(RoleBasedTabs.DataAccessRequests, false);

    isTabVisible(RoleBasedTabs.Qa, true);
  });

  it('Validate tabs for a logged-in Dataland-Admin with no company role assignments', function () {
    mountDatasetsTabMenuWithAuthentication([KEYCLOAK_ROLE_REVIEWER, KEYCLOAK_ROLE_ADMIN], []);
    assertThatStandardTabsAreAllVisible();

    isTabVisible(RoleBasedTabs.DataAccessRequests, false);

    isTabVisible(RoleBasedTabs.Qa, true);
    isTabVisible(RoleBasedTabs.AllDataRequests, true);
  });
  it('Validate if route navigation leads to correct route when tab is changed', () => {
    mountDatasetsTabMenuWithAuthentication([KEYCLOAK_ROLE_USER], []);
    cy.get('[data-pc-name="tablist"]').contains('MY DATASETS').click();
    cy.get(`[data-pc-name="tab"][data-p-active="true"]`).contains(AlwaysVisibleTabs.MyDatasets).should('exist');
  });
});
