import {
  generateCompanyRoleAssignment,
  mountCompanyCockpitWithAuthentication,
  mockRequestsOnMounted,
  validateVsmeFrameworkSummaryPanel,
} from '@ct/testUtils/CompanyCockpitUtils.ts';
import { setupCompanyCockpitFixtures } from './testUtils';
import { type AggregatedFrameworkDataSummary, type CompanyInformation, type DataTypeEnum } from '@clients/backend';
import { CompanyRole } from '@clients/communitymanager';
import { KEYCLOAK_ROLES } from '@/utils/KeycloakRoles';

describe('Component test for the authorization of company cockpit components', () => {
  let companyInformationForTest: CompanyInformation;
  let mockMapOfDataTypeToAggregatedFrameworkDataSummary: Map<DataTypeEnum, AggregatedFrameworkDataSummary>;
  const dummyCompanyId = '550e8400-e29b-11d4-a716-446655440000';
  const dummyUserId = 'mock-user-id';
  const dummyFirstName = 'mock-first-name';
  const dummyLastName = 'mock-last-name';
  const dummyEmail = 'mock@Company.com';

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

  it('Check the Vsme summary panel behaviour if the user is company owner', () => {
    const companyRoleAssignmentsOfUser = [generateCompanyRoleAssignment(CompanyRole.CompanyOwner, dummyCompanyId)];
    const hasCompanyAtLeastOneOwner = true;
    for (const keycloakRole of KEYCLOAK_ROLES) {
      mockRequestsOnMounted(
        hasCompanyAtLeastOneOwner,
        companyInformationForTest,
        mockMapOfDataTypeToAggregatedFrameworkDataSummary
      );
      mountCompanyCockpitWithAuthentication(true, false, [keycloakRole], companyRoleAssignmentsOfUser);
      cy.get('[data-test="toggleShowAll"]').contains('SHOW ALL').click();
      validateVsmeFrameworkSummaryPanel(true);
    }
  });

  it('Users Page is visible for a Company Analyst', () => {
    const companyRoleAssignmentsOfUser = [generateCompanyRoleAssignment(CompanyRole.Analyst, dummyCompanyId)];

    mockRequestsOnMounted(true, companyInformationForTest, mockMapOfDataTypeToAggregatedFrameworkDataSummary);
    mountCompanyCockpitWithAuthentication(true, false, undefined, companyRoleAssignmentsOfUser);

    cy.get('[data-test="company-cockpit-root"]')
      .last()
      .within(() => {
        cy.wait('@fetchRoleAssignments');

        cy.get('[data-test="datasetsTab"]').filter(':visible').contains('Datasets').click();
        cy.get('[data-test=sfdr-summary-panel]').filter(':visible').should('be.visible');
        cy.get('[data-test="company-roles-card"]').filter(':visible').should('not.exist');

        cy.get('[data-test="usersTab"]').filter(':visible').contains('Users').click();
        cy.get('[data-test="company-roles-card"]').filter(':visible').should('exist');
        cy.get('[data-test=sfdr-summary-panel]').filter(':visible').should('not.exist');

        cy.get('[data-test="datasetsTab"]').filter(':visible').contains('Datasets').click();
        cy.get('[data-test=sfdr-summary-panel]').filter(':visible').should('exist');
        cy.get('[data-test="company-roles-card"]').filter(':visible').should('not.exist');
      });
  });

  it('Users Page is not visible for a user that is not a Company Member', () => {
    mockRequestsOnMounted(true, companyInformationForTest, mockMapOfDataTypeToAggregatedFrameworkDataSummary);
    mountCompanyCockpitWithAuthentication(true, false, undefined, []);
    cy.get('[data-test=sfdr-summary-panel]').should('be.visible');
    cy.get('[data-test="usersTab"]').should('not.exist');
  });

  it('Users are being displayed correctly in the Users Page', () => {
    const companyRoleAssignmentsOfUser = [generateCompanyRoleAssignment(CompanyRole.Analyst, dummyCompanyId)];

    mockRequestsOnMounted(true, companyInformationForTest, mockMapOfDataTypeToAggregatedFrameworkDataSummary);
    mountCompanyCockpitWithAuthentication(true, false, undefined, companyRoleAssignmentsOfUser);
    cy.wait('@fetchRoleAssignments');
    cy.get('[data-test="usersTab"]').click();
    cy.wait('@fetchRoleAssignments');
    cy.get('[data-test="company-roles-card"]', { timeout: 10000 }).should('exist');
    cy.contains('[data-test="company-roles-card"]', 'Analysts').within(() => {
      cy.get('td', { timeout: 10000 }).should('exist');
      cy.get('td').contains(dummyFirstName).should('exist');
      cy.get('td').contains(dummyLastName).should('exist');
      cy.get('td').contains(dummyEmail).should('exist');
      cy.get('td').contains(dummyUserId).should('exist');
    });
    cy.contains('[data-test="company-roles-card"]', 'Admins').within(() => {
      cy.get('td').contains(dummyFirstName).should('not.exist');
      cy.get('td').contains(dummyLastName).should('not.exist');
      cy.get('td').contains(dummyEmail).should('not.exist');
      cy.get('td').contains(dummyUserId).should('not.exist');
    });
  });
});
