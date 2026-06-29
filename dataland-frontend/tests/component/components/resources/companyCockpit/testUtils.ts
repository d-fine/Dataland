import type { BasicDataDimensions, CompanyInformation, LksgData } from '@clients/backend';
import type { FixtureData } from '@sharedUtils/Fixtures';

/**
 * Sets up fixtures for Company Cockpit tests.
 * @param setCompanyInformation
 * @param setAvailableDataDimensions
 */
export function setupCompanyCockpitFixtures(
  setCompanyInformation: (info: CompanyInformation) => void,
  setAvailableDataDimensions: (dimensions: BasicDataDimensions[]) => void
): void {
  cy.clearLocalStorage();
  cy.fixture('CompanyInformationWithLksgData').then((jsonContent) => {
    const lksgFixtures = jsonContent as Array<FixtureData<LksgData>>;
    const firstFixture = lksgFixtures[0];
    if (!firstFixture) {
      throw new Error('Expected at least one fixture');
    }
    setCompanyInformation(firstFixture.companyInformation);
  });
  cy.fixture('AvailableDataDimensionsMock').then((jsonContent) => {
    setAvailableDataDimensions(jsonContent as BasicDataDimensions[]);
  });
}
