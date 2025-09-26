import {
  generateCompanyRoleAssignment,
  mountCompanyCockpitWithAuthentication,
  mockRequestsOnMounted,
  validateVsmeFrameworkSummaryPanel,
} from '@ct/testUtils/CompanyCockpitUtils.ts';
import {
  type AggregatedFrameworkDataSummary,
  type CompanyInformation,
  type DataTypeEnum,
  type LksgData,
} from '@clients/backend';
import { type FixtureData } from '@sharedUtils/Fixtures';
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
    cy.clearLocalStorage();
    cy.fixture('CompanyInformationWithLksgData').then(function (jsonContent) {
      const lksgFixtures = jsonContent as Array<FixtureData<LksgData>>;
      companyInformationForTest = lksgFixtures[0].companyInformation;
    });
    cy.fixture('MapOfFrameworkNameToAggregatedFrameworkDataSummaryMock').then(function (jsonContent) {
      mockMapOfDataTypeToAggregatedFrameworkDataSummary = jsonContent as Map<
        DataTypeEnum,
        AggregatedFrameworkDataSummary
      >;
    });
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

  it('Users Page is visible for a Company Member', () => {
    const companyRoleAssignmentsOfUser = [generateCompanyRoleAssignment(CompanyRole.Member, dummyCompanyId)];
    mockRequestsOnMounted(true, companyInformationForTest, mockMapOfDataTypeToAggregatedFrameworkDataSummary);
    mountCompanyCockpitWithAuthentication(true, false, undefined, companyRoleAssignmentsOfUser);
    cy.get('[data-test=sfdr-summary-panel]').should('be.visible');
    cy.get('[data-test="company-roles-card"]').should('not.be.visible');
    cy.get('[data-test="usersTab"]').click();
    cy.get('[data-test=sfdr-summary-panel]').should('not.be.visible');
    cy.get('[data-test="company-roles-card"]').should('be.visible');
    cy.get('[data-test="datasetsTab"]').click();
    cy.get('[data-test=sfdr-summary-panel]').should('be.visible');
    cy.get('[data-test="company-roles-card"]').should('not.be.visible');
  });

  it('Users Page is not visible for a user that is not a Company Member', () => {
    mockRequestsOnMounted(true, companyInformationForTest, mockMapOfDataTypeToAggregatedFrameworkDataSummary);
    mountCompanyCockpitWithAuthentication(true, false, undefined, []);
    cy.get('[data-test=sfdr-summary-panel]').should('be.visible');
    cy.get('[data-test="usersTab"]').should('not.exist');
  });

  it('Users are being displayed correctly in the Users Page', () => {
    const companyRoleAssignmentsOfUser = [generateCompanyRoleAssignment(CompanyRole.Member, dummyCompanyId)];
    cy.intercept('GET', '**/community/company-role-assignments*', (req) => {
      const q = req.query as Record<string, string | undefined>;
      if (q.role === CompanyRole.Member) {
        req.reply({
          statusCode: 200,
          body: [
            {
              companyRole: 'Member',
              companyId: dummyCompanyId,
              userId: dummyUserId,
              email: dummyEmail,
              firstName: dummyFirstName,
              lastName: dummyLastName,
            },
          ],
        });
      } else {
        req.reply({ statusCode: 200, body: [] });
      }
    }).as('roleFetch');
    mockRequestsOnMounted(true, companyInformationForTest, mockMapOfDataTypeToAggregatedFrameworkDataSummary);
    mountCompanyCockpitWithAuthentication(true, false, undefined, companyRoleAssignmentsOfUser);
    cy.wait('@roleFetch');
    cy.get('[data-test="usersTab"]').click();
    cy.wait('@roleFetch');
    cy.get('[data-test="company-roles-card"]', { timeout: 10000 }).should('exist');
    cy.contains('[data-test="company-roles-card"]', 'Members').within(() => {
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
