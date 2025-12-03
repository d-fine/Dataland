import { CompanyInformation, DataTypeEnum, AggregatedFrameworkDataSummary, LksgData } from '@clients/backend';
import { FixtureData } from '@sharedUtils/Fixtures';

export function setupCompanyCockpitFixtures(
  setCompanyInformation: (info: CompanyInformation) => void,
  setFrameworkDataSummary: (map: Map<DataTypeEnum, AggregatedFrameworkDataSummary>) => void
) {
  cy.clearLocalStorage();
  cy.fixture('CompanyInformationWithLksgData').then((jsonContent) => {
    const lksgFixtures = jsonContent as Array<FixtureData<LksgData>>;
    const firstFixture = lksgFixtures[0];
    if (!firstFixture) {
      throw new Error('Expected at least one fixture');
    }
    setCompanyInformation(firstFixture.companyInformation);
  });
  cy.fixture('MapOfFrameworkNameToAggregatedFrameworkDataSummaryMock').then((jsonContent) => {
    setFrameworkDataSummary(jsonContent as Map<DataTypeEnum, AggregatedFrameworkDataSummary>);
  });
}
