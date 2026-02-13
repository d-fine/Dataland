import DatasetsTabMenu from '@/components/general/DatasetsTabMenu.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { CompanyRole, type CompanyRoleAssignmentExtended } from '@clients/communitymanager';
import { getMountingFunction } from '@ct/testUtils/Mount';
import { KEYCLOAK_ROLE_ADMIN, KEYCLOAK_ROLE_REVIEWER, KEYCLOAK_ROLE_USER } from '@/utils/KeycloakRoles';
import { ref } from 'vue';
import router from '@/router';

/**
 * Checks if the tab with the defined text is visible or not
 * @param textInTab that shall be checked
 * @param isTabExpectedToBeVisible describes if the tab is expected to be visible on the navigation bar
 */
function isTabVisible(textInTab: string, isTabExpectedToBeVisible: boolean): void {
  const visibilityAssertion = isTabExpectedToBeVisible ? 'be.visible' : 'not.exist';
  cy.get('[data-pc-name="tablist"]').contains(textInTab).should(visibilityAssertion);
}

describe('Component tests for the tab used by logged-in users to switch pages', () => {
  enum AlwaysVisibleTabs {
    Companies = 'COMPANIES',
    MyDatasets = 'MY DATASETS',
    MyPortfolios = 'MY PORTFOLIOS',
    MyDataRequests = 'MY DATA REQUESTS',
  }

  enum RoleBasedTabs {
    MyCompany = 'MY COMPANY',
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

    isTabVisible(RoleBasedTabs.MyCompany, true);
    isTabVisible(RoleBasedTabs.DataAccessRequests, true);
  });

  it('Validate tabs for a logged-in Dataland-Reader with analyst company role', function () {
    const companyRoleAssignments: CompanyRoleAssignmentExtended[] = [
      {
        companyRole: CompanyRole.Analyst,
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
    isTabVisible(RoleBasedTabs.DataAccessRequests, false);

    isTabVisible(RoleBasedTabs.MyCompany, true);
  });

  it('Validate tabs for a logged-in Dataland-Reviewer with no company role assignments', function () {
    mountDatasetsTabMenuWithAuthentication([KEYCLOAK_ROLE_REVIEWER], []);
    assertThatStandardTabsAreAllVisible();

    isTabVisible(RoleBasedTabs.AllDataRequests, false);
    isTabVisible(RoleBasedTabs.MyCompany, false);
    isTabVisible(RoleBasedTabs.DataAccessRequests, false);

    isTabVisible(RoleBasedTabs.Qa, true);
  });

  it('Validate tabs for a logged-in Dataland-Admin with no company role assignments', function () {
    mountDatasetsTabMenuWithAuthentication([KEYCLOAK_ROLE_REVIEWER, KEYCLOAK_ROLE_ADMIN], []);
    assertThatStandardTabsAreAllVisible();

    isTabVisible(RoleBasedTabs.MyCompany, false);
    isTabVisible(RoleBasedTabs.DataAccessRequests, false);

    isTabVisible(RoleBasedTabs.Qa, true);
    isTabVisible(RoleBasedTabs.AllDataRequests, true);
  });

  it('Validate tabs for a logged-in Dataland-User with shared portfolios', function () {
    cy.intercept('GET', '**/users/portfolios/shared/names', {
      statusCode: 200,
      body: [
        {
          portfolioId: 'dummy-id',
          portfolioName: 'Shared Portfolio 1',
        },
      ],
    });
    mountDatasetsTabMenuWithAuthentication([KEYCLOAK_ROLE_USER], []);
    isTabVisible('SHARED PORTFOLIOS', true);
  });

  it('Validate tabs for a logged-in Dataland-User without shared portfolios', function () {
    cy.intercept('GET', '**/users/portfolios/shared/names', {
      statusCode: 200,
      body: [],
    });
    mountDatasetsTabMenuWithAuthentication([KEYCLOAK_ROLE_USER], []);
    isTabVisible('SHARED PORTFOLIOS', false);
  });

  it('Validate if route navigation leads to correct route when tab is changed', () => {
    mountDatasetsTabMenuWithAuthentication([KEYCLOAK_ROLE_USER], []);
    cy.get('[data-pc-name="tablist"]').contains('MY DATASETS').click();
    cy.get(`[data-pc-name="tab"][data-p-active="true"]`).contains(AlwaysVisibleTabs.MyDatasets).should('exist');
  });

  it('Validate route navigation for MY COMPANY tab', () => {
    const companyRoleAssignments: CompanyRoleAssignmentExtended[] = [
      {
        companyRole: CompanyRole.Analyst,
        companyId: dummyCompanyId,
        userId: dummyUserId,
        firstName: dummyFirstName,
        email: dummyEmail,
      },
      {
        companyRole: CompanyRole.CompanyOwner,
        companyId: 'another-dummy-id',
        userId: dummyUserId,
        firstName: dummyFirstName,
        email: dummyEmail,
      },
    ];
    mountDatasetsTabMenuWithAuthentication([KEYCLOAK_ROLE_USER], companyRoleAssignments);
    cy.get('[data-pc-name="tablist"]').contains('MY COMPANY').click();
    cy.url().should('include', `/companies/${dummyCompanyId}`);
  });
});
