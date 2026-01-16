import {
  generateCompanyRoleAssignment,
  mountCompanyCockpitWithAuthentication,
  mockRequestsOnMounted,
} from '@ct/testUtils/CompanyCockpitUtils.ts';
import { setupCompanyCockpitFixtures } from './testUtils';
import { type AggregatedFrameworkDataSummary, type CompanyInformation, type DataTypeEnum } from '@clients/backend';
import { CompanyRole } from '@clients/communitymanager';
import { KEYCLOAK_ROLE_ADMIN } from '@/utils/KeycloakRoles';

/**
 * Intercepts the company rights endpoint for the given company id and returns the company rights
 * @param companyId
 * @param companyRights such as "Member" (company)
 */
function interceptCompanyRights(companyId: string, companyRights: string[]): void {
  cy.intercept('GET', `**/community/company-rights/${companyId}*`, {
    statusCode: 200,
    body: companyRights,
  }).as('fetchCompanyRights');
}

describe('Component test for the authorization of company cockpit components', () => {
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

  it('Check tab and content visibility for Dataland Admins', () => {
    interceptCompanyRights(dummyCompanyId, []);
    mockRequestsOnMounted(true, companyInformationForTest, mockMapOfDataTypeToAggregatedFrameworkDataSummary);
    mountCompanyCockpitWithAuthentication(true, false, [KEYCLOAK_ROLE_ADMIN], []);

    cy.wait('@fetchCompanyRights');

    cy.get('[data-test="usersTab"]').should('be.visible');
    cy.get('[data-test="creditsTab"]').should('be.visible').click();
    cy.get('[data-test="creditsBalance"]').should('be.visible');
  });

  it('Check tab and content visibility for users without admin and without company rights', () => {
    interceptCompanyRights(dummyCompanyId, []);
    mockRequestsOnMounted(true, companyInformationForTest, mockMapOfDataTypeToAggregatedFrameworkDataSummary);
    mountCompanyCockpitWithAuthentication(true, false, undefined, []);

    cy.wait('@fetchCompanyRights');
    cy.get('[data-test=sfdr-summary-panel]').should('be.visible');
    cy.get('[data-test="usersTab"]').should('not.exist');
    cy.get('[data-test="creditsTab"]').should('not.exist');
  });

  it('Check tab and content visibility for users with company rights for a non member company', () => {
    interceptCompanyRights(dummyCompanyId, []);
    mockRequestsOnMounted(true, companyInformationForTest, mockMapOfDataTypeToAggregatedFrameworkDataSummary);
    mountCompanyCockpitWithAuthentication(true, false, undefined, [
      generateCompanyRoleAssignment(CompanyRole.Analyst, dummyCompanyId),
    ]);

    cy.wait('@fetchCompanyRights');
    cy.get('[data-test="usersTab"]').should('be.visible');
    cy.get('[data-test="creditsTab"]').should('not.exist');
  });

  it('Check tab and content visibility for users with company rights for a member company', (): void => {
    interceptCompanyRights(dummyCompanyId, ['Member']);
    const companyRoleAssignmentsOfUser = [generateCompanyRoleAssignment(CompanyRole.Analyst, dummyCompanyId)];

    mockRequestsOnMounted(true, companyInformationForTest, mockMapOfDataTypeToAggregatedFrameworkDataSummary);
    mountCompanyCockpitWithAuthentication(true, false, undefined, companyRoleAssignmentsOfUser);

    cy.wait('@fetchCompanyRights');

    cy.get('[data-test="usersTab"]').should('be.visible');
    cy.get('[data-test="creditsTab"]').should('be.visible');
  });
});
