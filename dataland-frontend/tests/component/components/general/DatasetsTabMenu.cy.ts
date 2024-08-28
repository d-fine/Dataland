import DatasetsTabMenu from '@/components/general/DatasetsTabMenu.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { CompanyRole, type CompanyRoleAssignment } from '@clients/communitymanager';
import { KEYCLOAK_ROLE_REVIEWER, KEYCLOAK_ROLE_USER } from '@/utils/KeycloakUtils';
import { getMountingFunction } from '@ct/testUtils/Mount';

describe('Component tests for the tab used by logged-in users to switch pages', () => {
  enum AlwaysVisibleTabs {
    Companies = 'COMPANIES',
    MyDatasets = 'MY DATASETS',
    MyDataRequests = 'MY DATA REQUESTS',
  }

  enum RoleBasedTabs {
    Qa = 'QA',
    DataAccessRequests = 'DATA REQUESTS FOR MY COMPANIES',
  }

  const dummyUserId = 'mock-user-id';
  const dummyCompanyId = '550e8400-e29b-11d4-a716-446655440000';

  /**
   * Mounts the Dataland navigation tab with a specific authentication
   * @param keycloakRoles of the logged-in user that sees the tab
   * @param companyRoleAssignments for the logged-in user that sees the tab
   * @param indexOfHighlightedTab sets which tab is highlighted as if the user had clicked on it
   */
  function mountDatasetsTabMenuWithAuthentication(
    keycloakRoles: string[],
    companyRoleAssignments: CompanyRoleAssignment[],
    indexOfHighlightedTab: number
  ): void {
    getMountingFunction()(DatasetsTabMenu, {
      keycloak: minimalKeycloakMock({
        authenticated: true,
        roles: keycloakRoles,
        userId: dummyUserId,
      }),
      global: {
        provide: {
          companyRoleAssignments: companyRoleAssignments,
        },
      },
      props: {
        initialTabIndex: indexOfHighlightedTab,
      },
    });
  }

  /**
   * Checks if the tab with the defined text is visible or not
   * @param textInTab that shall be checked
   * @param isTabExpectedToBeVisible describes if the tab is expected to be visible on the navigation bar
   */
  function isTabVisible(textInTab: string, isTabExpectedToBeVisible: boolean): void {
    const visibilityAssertion = isTabExpectedToBeVisible ? 'be.visible' : 'not.be.visible';
    cy.get('li[data-pc-name="tabpanel"]').contains(textInTab).should(visibilityAssertion);
  }

  /**
   * Checks if the tab with the defined text is highlighted or not
   * @param textInTab that shall be checked
   * @param isTabExpectedToBeHighlighted describes if the tab is expected to be highlighted on the navigation bar
   */
  function isTabHighlighted(textInTab: string, isTabExpectedToBeHighlighted: boolean): void {
    cy.get(`li[data-pc-name="tabpanel"][data-p-active="${isTabExpectedToBeHighlighted}"]`)
      .contains(textInTab)
      .should('exist');
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
    mountDatasetsTabMenuWithAuthentication([KEYCLOAK_ROLE_USER], [], 0);
    assertThatStandardTabsAreAllVisible();
    for (const tabText of Object.values(RoleBasedTabs)) {
      isTabVisible(tabText, false);
    }
    isTabHighlighted(AlwaysVisibleTabs.Companies, true);
  });

  it('Validate tabs for a logged-in Dataland-Reader with company ownership', function () {
    const companyRoleAssignments: CompanyRoleAssignment[] = [
      {
        companyRole: CompanyRole.CompanyOwner,
        companyId: dummyCompanyId,
        userId: dummyUserId,
      },
    ];
    mountDatasetsTabMenuWithAuthentication([KEYCLOAK_ROLE_USER], companyRoleAssignments, 4);
    assertThatStandardTabsAreAllVisible();
    isTabVisible(RoleBasedTabs.Qa, false);
    isTabVisible(RoleBasedTabs.DataAccessRequests, true);
    isTabHighlighted(RoleBasedTabs.DataAccessRequests, true);
  });

  it('Validate tabs for a logged-in Dataland-Reviewer with no company role assignments', function () {
    mountDatasetsTabMenuWithAuthentication([KEYCLOAK_ROLE_REVIEWER], [], 2);
    assertThatStandardTabsAreAllVisible();
    isTabVisible(RoleBasedTabs.Qa, true);
    isTabVisible(RoleBasedTabs.DataAccessRequests, false);
    isTabHighlighted(RoleBasedTabs.Qa, true);
  });
});
