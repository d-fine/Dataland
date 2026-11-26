import CompanyDatasetsPane from '@/components/resources/companyCockpit/CompanyDatasetsPane.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { DataTypeEnum } from '@clients/backend';
import { CompanyRole } from '@clients/communitymanager';
import { KEYCLOAK_ROLE_USER } from '@/utils/KeycloakRoles';

describe('Component test for the company datasets pane', () => {
  // Valid UUID format required by isCompanyIdValid, used e.g. by the company-ownership check.
  const companyId = 'ad50bc6e-eb04-4bf6-9e70-a4b1c1c8e161';
  const dummyUserId = 'mock-user-id';

  /**
   * Stubs the requests of the pane that are not under test.
   * @param options the mocked answers
   * @param options.ownershipStatusCode status code of the company ownership check, 404 means "no owner"
   * @param options.ownershipDelay delay of the company ownership check in milliseconds
   * @param options.companyRoles the company roles of the current user for the company
   * @param options.nonSourceableSearchResponse the mocked response for POST /api/non-sourceable/search
   */
  function interceptRequests(
    options: {
      ownershipStatusCode?: number;
      ownershipDelay?: number;
      companyRoles?: CompanyRole[];
      nonSourceableSearchResponse?: object[];
    } = {}
  ): void {
    cy.intercept('GET', `/api/companies/${companyId}/aggregated-framework-data-summary`, {}).as('getAggregatedSummary');
    cy.intercept('HEAD', `/community/company-ownership/${companyId}`, {
      statusCode: options.ownershipStatusCode ?? 200,
      delay: options.ownershipDelay ?? 0,
    }).as('getCompanyOwnership');
    cy.intercept('GET', '/documents/*', []).as('searchDocuments');
    cy.intercept('POST', '/api/non-sourceable/search', options.nonSourceableSearchResponse ?? []).as(
      'postNonSourceableSearch'
    );
    cy.intercept('GET', `**/community/company-role-assignments?userId=${dummyUserId}`, {
      body: (options.companyRoles ?? []).map((companyRole) => ({
        companyRole,
        companyId,
        userId: dummyUserId,
        firstName: 'mock-first-name',
        email: 'mock@Company.com',
      })),
    }).as('fetchCurrentUserRoleAssignments');
  }

  /**
   * Mounts CompanyDatasetsPane for a logged-in user without Keycloak uploader rights.
   */
  function mountComponent(): void {
    //@ts-ignore
    cy.mountWithPlugins(CompanyDatasetsPane, {
      keycloak: minimalKeycloakMock({ userId: dummyUserId, roles: [KEYCLOAK_ROLE_USER] }),
      props: { companyId },
    });
  }

  it('Shows the correct non-sourceable count on the correct framework tile, grouped separately per framework', () => {
    interceptRequests({
      nonSourceableSearchResponse: [
        { companyId, dataType: DataTypeEnum.Sfdr, reportingPeriod: '2023' },
        { companyId, dataType: DataTypeEnum.Sfdr, reportingPeriod: '2022' },
        { companyId, dataType: DataTypeEnum.NuclearAndGas, reportingPeriod: '2023' },
      ],
    });
    mountComponent();
    cy.wait('@postNonSourceableSearch');
    cy.get(`[data-test="${DataTypeEnum.Sfdr}-panel-non-sourceable-value"]`).should('have.text', '2');
    cy.get(`[data-test="${DataTypeEnum.NuclearAndGas}-panel-non-sourceable-value"]`).should('have.text', '1');
  });

  it('Does not show a non-sourceable hint on any tile when the response is empty', () => {
    interceptRequests();
    mountComponent();
    cy.wait('@postNonSourceableSearch');

    cy.get('[data-test="summaryPanels"]').find('.summary-panel__non-sourceable').should('not.exist');
  });

  it('Shows the claim ownership panel only once the ownership check found no owner', () => {
    interceptRequests({ ownershipStatusCode: 404, ownershipDelay: 500 });
    mountComponent();
    cy.get('[data-test="summaryPanels"]').should('be.visible');
    cy.get('[data-test="claimOwnershipPanelLink"]').should('not.exist');
    cy.wait('@getCompanyOwnership');
    cy.get('[data-test="claimOwnershipPanelLink"]').should('be.visible');
  });

  it('Does not show the claim ownership panel for companies with an owner', () => {
    interceptRequests({ ownershipStatusCode: 200 });
    mountComponent();
    cy.wait('@getCompanyOwnership');
    cy.get('[data-test="summaryPanels"]').should('be.visible');
    cy.get('[data-test="claimOwnershipPanelLink"]').should('not.exist');
  });

  it('Allows company owners and data uploaders of the company to provide data', () => {
    interceptRequests({ companyRoles: [CompanyRole.DataUploader] });
    mountComponent();
    cy.wait('@fetchCurrentUserRoleAssignments');
    cy.get(`[data-test="${DataTypeEnum.Sfdr}-provide-data-button"]`).should('be.visible');
  });

  it('Does not allow other company members to provide data', () => {
    interceptRequests({ companyRoles: [CompanyRole.Analyst] });
    mountComponent();
    cy.wait('@fetchCurrentUserRoleAssignments');
    cy.get(`[data-test="${DataTypeEnum.Sfdr}-summary-panel"]`).should('be.visible');
    cy.get(`[data-test="${DataTypeEnum.Sfdr}-provide-data-button"]`).should('not.exist');
  });
});
