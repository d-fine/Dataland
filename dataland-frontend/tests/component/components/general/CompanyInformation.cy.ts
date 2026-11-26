import CompanyInformationComponent from '@/components/pages/CompanyInformation.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { createTestQueryClient } from '@ct/testUtils/TestQueryClient';
import { type CompanyInformation, type LksgData } from '@clients/backend';
import { CompanyRole } from '@clients/communitymanager';
import { type FixtureData } from '@sharedUtils/Fixtures';
import router from '@/router';
import { getMountingFunction } from '@ct/testUtils/Mount';
import { KEYCLOAK_ROLE_ADMIN, KEYCLOAK_ROLE_USER } from '@/utils/KeycloakRoles';
import { type QueryClient } from '@tanstack/vue-query';

describe('Component tests for the company info sheet', function (): void {
  const dummyCompanyId = '550e8400-e29b-11d4-a716-446655440000';
  const dummyUserId = 'mock-user-id';
  const dummyParentCompanyLei = 'dummyParentLei';
  const dummyParentCompanyId = 'dummyParentCompanyId';
  const dummyParentCompanyName = 'dummyParent Company';
  const dummyCompanyLei = 'dummyCompanyLei';
  const companyInfoUrl = `**/api/companies/${dummyCompanyId}/info`;

  let companyInformationForTest: CompanyInformation;

  before(function () {
    cy.fixture('CompanyInformationWithLksgData').then(function (jsonContent) {
      companyInformationForTest = (jsonContent as Array<FixtureData<LksgData>>)[0].companyInformation;
      companyInformationForTest.parentCompanyLei = dummyParentCompanyLei;
      companyInformationForTest.identifiers = {
        Lei: [dummyCompanyLei],
      };
    });
  });

  /**
   * Mocks the requests of the component
   * @param options the mocked answers
   * @param options.hasCompanyOwner whether the company has at least one company owner
   * @param options.companyRoles the company roles of the current user for the company
   * @param options.companyRights the company rights of the company
   */
  function mockRequestsOnMounted(
    options: { hasCompanyOwner?: boolean; companyRoles?: CompanyRole[]; companyRights?: string[] } = {}
  ): void {
    cy.intercept(companyInfoUrl, { body: companyInformationForTest }).as('fetchCompanyInfo');
    cy.intercept(`**/api/companies/names?searchString=${dummyParentCompanyLei}**`, {
      body: [
        {
          companyId: dummyParentCompanyId,
          companyName: dummyParentCompanyName,
        },
      ],
    }).as('fetchParentCompany');
    cy.intercept('HEAD', `**/community/company-ownership/${dummyCompanyId}`, {
      statusCode: options.hasCompanyOwner ? 200 : 404,
    }).as('fetchCompanyOwnershipExistence');
    cy.intercept('GET', `**/community/company-role-assignments?userId=${dummyUserId}`, {
      body: (options.companyRoles ?? []).map((companyRole) => ({
        companyRole,
        companyId: dummyCompanyId,
        userId: dummyUserId,
        firstName: 'mock-first-name',
        email: 'mock@Company.com',
      })),
    }).as('fetchCurrentUserRoleAssignments');
    cy.intercept('GET', `**/community/company-rights/${dummyCompanyId}`, { body: options.companyRights ?? [] }).as(
      'fetchCompanyRights'
    );
  }

  /**
   * Mounts the CompanyInformation component with a mocked authentication state.
   * @param authenticated - Whether the user is authenticated.
   * @param roles - The Keycloak roles of the user.
   * @param queryClient - The query client to use, only needed if the test inspects or reuses the query cache.
   * @returns the mounted component
   */
  function mountComponentWithAuth(
    authenticated: boolean,
    roles: string[] = [KEYCLOAK_ROLE_USER],
    queryClient?: QueryClient
  ): Cypress.Chainable {
    const mountingFunction = getMountingFunction({
      keycloak: minimalKeycloakMock({ authenticated, roles, userId: dummyUserId }),
      router,
      queryClient,
    });

    return mountingFunction(CompanyInformationComponent, {
      props: {
        companyId: dummyCompanyId,
      },
    });
  }

  it('Check visibility of company information', function () {
    mockRequestsOnMounted();
    cy.spy(router, 'push').as('routerPush');
    mountComponentWithAuth(true);
    cy.get('[data-test="lei-visible"]').should('have.text', companyInformationForTest.identifiers['Lei'][0]);
    cy.get('[data-test="headquarter-visible"]').should('have.text', companyInformationForTest.headquarters);
    cy.get('[data-test="sector-visible"]').should('have.text', companyInformationForTest.sector);
    cy.get('[data-test="parent-visible"]').should('have.text', dummyParentCompanyName).click();
    cy.get('@routerPush').should('have.been.calledWith', `/companies/${dummyParentCompanyId}`);
  });

  it('should not show "Add to portfolio" button when user is not authenticated', function () {
    mockRequestsOnMounted();
    mountComponentWithAuth(false);
    cy.get('[data-test="companyNameTitle"]').should('be.visible');
    cy.get('[data-test="addCompanyToPortfoliosButton"]').should('not.exist');
  });

  it('should show "Add to portfolio" button when user is authenticated', function () {
    mockRequestsOnMounted();
    mountComponentWithAuth(true);
    cy.get('[data-test="addCompanyToPortfoliosButton"]').should('exist');
  });

  it('Shows a loading indicator until the company information is loaded', function () {
    mockRequestsOnMounted();
    cy.intercept(companyInfoUrl, { body: companyInformationForTest, delay: 500 }).as('fetchCompanyInfo');
    mountComponentWithAuth(true);
    cy.contains('Loading company information...').should('be.visible');
    cy.wait('@fetchCompanyInfo');
    cy.contains('Loading company information...').should('not.exist');
    cy.get('[data-test="companyNameTitle"]').should('have.text', companyInformationForTest.companyName);
  });

  it('Shows that the company does not exist if the company information is not found', function () {
    mockRequestsOnMounted();
    cy.intercept(companyInfoUrl, { statusCode: 404 }).as('fetchCompanyInfo');
    mountComponentWithAuth(true);
    cy.wait('@fetchCompanyInfo');
    cy.get('[data-test="noCompanyWithThisIdErrorIndicator"]').should('be.visible');
    cy.get('[data-test="companyInformationErrorIndicator"]').should('not.exist');
    cy.get('@fetchCompanyInfo.all').should('have.length', 1);
  });

  it('Shows an error message if the company information cannot be loaded after retrying', function () {
    mockRequestsOnMounted();
    cy.intercept(companyInfoUrl, { statusCode: 500 }).as('fetchCompanyInfo');
    cy.clock();
    mountComponentWithAuth(true);
    cy.wait('@fetchCompanyInfo');
    // Unlike a missing company, other errors are retried three times with exponential backoff
    for (const retryDelayInMs of [1000, 2000, 4000]) {
      cy.contains('Loading company information...').should('be.visible');
      cy.tick(retryDelayInMs);
      cy.wait('@fetchCompanyInfo');
    }
    cy.get('[data-test="companyInformationErrorIndicator"]').should('be.visible');
    cy.get('[data-test="noCompanyWithThisIdErrorIndicator"]').should('not.exist');
    cy.contains('Loading company information...').should('not.exist');
  });

  it('Shows the verified company owner badge for companies with an owner', function () {
    mockRequestsOnMounted({ hasCompanyOwner: true });
    mountComponentWithAuth(true);
    cy.get('[data-test="verifiedCompanyOwnerBadge"]').should('be.visible');
  });

  it('Does not show the verified company owner badge for companies without an owner', function () {
    mockRequestsOnMounted({ hasCompanyOwner: false });
    mountComponentWithAuth(true);
    cy.wait('@fetchCompanyOwnershipExistence');
    cy.get('[data-test="companyNameTitle"]').should('be.visible');
    cy.get('[data-test="verifiedCompanyOwnerBadge"]').should('not.exist');
  });

  it('Shows the Dataland member badge to company members of member companies', function () {
    mockRequestsOnMounted({ companyRoles: [CompanyRole.Analyst], companyRights: ['Member'] });
    mountComponentWithAuth(true);
    cy.wait('@fetchCompanyRights');
    cy.get('[data-test="datalandMemberBadge"]').should('be.visible');
  });

  it('Shows the Dataland member badge to Dataland admins', function () {
    mockRequestsOnMounted({ companyRights: ['Member'] });
    mountComponentWithAuth(true, [KEYCLOAK_ROLE_ADMIN]);
    cy.get('[data-test="datalandMemberBadge"]').should('be.visible');
  });

  it('Does not request the company rights for users who are neither company members nor admins', function () {
    mockRequestsOnMounted({ companyRights: ['Member'] });
    mountComponentWithAuth(true, [KEYCLOAK_ROLE_USER]);
    cy.wait('@fetchCurrentUserRoleAssignments');
    cy.get('[data-test="companyNameTitle"]').should('be.visible');
    cy.get('[data-test="datalandMemberBadge"]').should('not.exist');
    cy.get('@fetchCompanyRights.all').should('have.length', 0);
  });

  it('Shows cached company information immediately when the component is mounted again', function () {
    const queryClient = createTestQueryClient();
    mockRequestsOnMounted();
    mountComponentWithAuth(true, [KEYCLOAK_ROLE_USER], queryClient).then(({ wrapper }) => {
      cy.get('[data-test="companyNameTitle"]').should('have.text', companyInformationForTest.companyName);
      cy.then(() => wrapper.unmount());
    });
    cy.get('[data-test="companyNameTitle"]').should('not.exist');

    cy.intercept(companyInfoUrl, { body: companyInformationForTest, delay: 2000 }).as('refetchCompanyInfo');
    mountComponentWithAuth(true, [KEYCLOAK_ROLE_USER], queryClient);
    // The short timeout ensures that the name is shown from the cache while the background refetch is still pending
    cy.get('[data-test="companyNameTitle"]', { timeout: 500 }).should(
      'have.text',
      companyInformationForTest.companyName
    );
    cy.contains('Loading company information...').should('not.exist');
    cy.wait('@refetchCompanyInfo');
  });
});
