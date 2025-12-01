import {
  generateCompanyRoleAssignment,
  mountCompanyCockpitWithAuthentication,
  mockRequestsOnMounted,
} from '@ct/testUtils/CompanyCockpitUtils.ts';
import {
  type AggregatedFrameworkDataSummary,
  type CompanyInformation,
  type DataTypeEnum,
  type LksgData,
} from '@clients/backend';
import { type FixtureData } from '@sharedUtils/Fixtures';
import { CompanyRole } from '@clients/communitymanager';

describe('Component test for Dataland Member Badge in Company Cockpit', () => {
  let companyInformationForTest: CompanyInformation;
  let mockMapOfDataTypeToAggregatedFrameworkDataSummary: Map<DataTypeEnum, AggregatedFrameworkDataSummary>;
  const dummyCompanyId = '550e8400-e29b-11d4-a716-446655440000';

  before(function () {
    cy.clearLocalStorage();
    cy.fixture('CompanyInformationWithLksgData').then(function (jsonContent) {
      const lksgFixtures = jsonContent as Array<FixtureData<LksgData>>;
      companyInformationForTest = lksgFixtures[0]!.companyInformation;
    });
    cy.fixture('MapOfFrameworkNameToAggregatedFrameworkDataSummaryMock').then(function (jsonContent) {
      mockMapOfDataTypeToAggregatedFrameworkDataSummary = jsonContent as Map<
        DataTypeEnum,
        AggregatedFrameworkDataSummary
      >;
    });
  });

  it('Dataland Member badge is visible when user is Dataland Member and Company Admin', () => {
    const companyRoleAssignmentsOfUser = [generateCompanyRoleAssignment(CompanyRole.Admin, dummyCompanyId)];
    const hasCompanyAtLeastOneOwner = true;

    cy.intercept('POST', '**/community/company-rights*', {
      statusCode: 200,
      body: [
        {
          companyId: dummyCompanyId,
          companyRight: 'Member',
        },
      ],
    }).as('companyRights');

    mockRequestsOnMounted(
      hasCompanyAtLeastOneOwner,
      companyInformationForTest,
      mockMapOfDataTypeToAggregatedFrameworkDataSummary
    );

    mountCompanyCockpitWithAuthentication(true, true, undefined, companyRoleAssignmentsOfUser);

    cy.wait('@companyRights');
    cy.get('[data-test="datalandMemberBadge"]', { timeout: 10000 })
      .should('be.visible')
      .should('contain.text', 'Dataland Member')
      .find('.pi-star')
      .should('exist');
  });
});
