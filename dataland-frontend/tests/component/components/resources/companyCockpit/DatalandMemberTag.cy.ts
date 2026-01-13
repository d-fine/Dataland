import {
  generateCompanyRoleAssignment,
  mountCompanyCockpitWithAuthentication,
  mockRequestsOnMounted,
} from '@ct/testUtils/CompanyCockpitUtils.ts';
import { type AggregatedFrameworkDataSummary, type CompanyInformation, type DataTypeEnum } from '@clients/backend';
import { CompanyRole, type CompanyRoleAssignmentExtended } from '@clients/communitymanager';
import { setupCompanyCockpitFixtures } from './testUtils';

/**
 * Intercepts the company rights API call and returns the provided body.
 * @param dummyCompanyId
 * @param body
 */
function interceptCompanyRights(dummyCompanyId: string, body: string[]): void {
  cy.intercept('GET', `**/community/company-rights/${dummyCompanyId}*`, {
    statusCode: 200,
    body,
  }).as('companyRights');
}

/**
 * Intercepts the company role assignments API call and returns the provided body.
 * @param body
 */
function interceptExtendedCompanyRoleAssignments(body: CompanyRoleAssignmentExtended[]): void {
  cy.intercept('GET', `**/community/company-role-assignments*`, {
    statusCode: 200,
    body,
  }).as('extendedCompanyRoleAssignments');
}

describe('Component test for Dataland Member Badge in Company Cockpit', () => {
  let companyInformationForTest: CompanyInformation;
  let mockMapOfDataTypeToAggregatedFrameworkDataSummary: Map<DataTypeEnum, AggregatedFrameworkDataSummary>;
  const dummyCompanyId = '550e8400-e29b-11d4-a716-446655440000';

  before(function () {
    setupCompanyCockpitFixtures(
      (info) => {
        companyInformationForTest = info;
      },
      (map) => {
        mockMapOfDataTypeToAggregatedFrameworkDataSummary = map;
      }
    );
  });

  it('Dataland Member badge is visible when Company is Dataland Member and user is Admin', () => {
    const companyRoleAssignmentsOfUser = [generateCompanyRoleAssignment(CompanyRole.Admin, dummyCompanyId)];
    const hasCompanyAtLeastOneOwner = true;

    interceptCompanyRights(dummyCompanyId, ['Member']);
    interceptExtendedCompanyRoleAssignments(companyRoleAssignmentsOfUser);

    mockRequestsOnMounted(
      hasCompanyAtLeastOneOwner,
      companyInformationForTest,
      mockMapOfDataTypeToAggregatedFrameworkDataSummary,
      { stubRoleAssignments: false }
    );

    mountCompanyCockpitWithAuthentication(true, true, undefined, companyRoleAssignmentsOfUser);

    cy.wait('@companyRights');
    cy.wait('@extendedCompanyRoleAssignments');
    cy.get('[data-test="datalandMemberBadge"]')
      .should('be.visible')
      .should('contain.text', 'Dataland Member')
      .find('.pi-star')
      .should('exist');
  });

  it('Dataland Member badge is NOT visible for non-admin users and without company role', () => {
    const companyRoleAssignmentsOfUser: CompanyRoleAssignmentExtended[] = [];
    const hasCompanyAtLeastOneOwner = true;

    interceptCompanyRights(dummyCompanyId, ['Member']);
    interceptExtendedCompanyRoleAssignments(companyRoleAssignmentsOfUser);

    mockRequestsOnMounted(
      hasCompanyAtLeastOneOwner,
      companyInformationForTest,
      mockMapOfDataTypeToAggregatedFrameworkDataSummary,
      { stubRoleAssignments: false }
    );

    mountCompanyCockpitWithAuthentication(true, true, undefined, companyRoleAssignmentsOfUser);
    cy.wait('@extendedCompanyRoleAssignments');
    cy.get('[data-test="datalandMemberBadge"]').should('not.exist');
  });

  it('Dataland Member badge is NOT visible when company is not a Dataland Member', () => {
    const companyRoleAssignmentsOfUser = [generateCompanyRoleAssignment(CompanyRole.Admin, dummyCompanyId)];
    const hasCompanyAtLeastOneOwner = true;

    interceptCompanyRights(dummyCompanyId, []);
    interceptExtendedCompanyRoleAssignments(companyRoleAssignmentsOfUser);

    mockRequestsOnMounted(
      hasCompanyAtLeastOneOwner,
      companyInformationForTest,
      mockMapOfDataTypeToAggregatedFrameworkDataSummary,
      { stubRoleAssignments: false }
    );

    mountCompanyCockpitWithAuthentication(true, true, undefined, companyRoleAssignmentsOfUser);

    cy.wait('@companyRights');
    cy.wait('@extendedCompanyRoleAssignments');
    cy.get('[data-test="datalandMemberBadge"]').should('not.exist');
  });
});
