import { type FixtureData } from '@sharedUtils/Fixtures';
import { type EuTaxonomyFinancialsData } from '@clients/backend';
import { generateEuTaxonomyFinancialsFixtures } from './EuTaxonomyFinancialsDataFixtures';

/**
 * Generates eu-taxonomy-financials prepared fixtures by generating random eu-taxonomy-financials datasets and
 * afterwards manipulating some fields via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateEuTaxonomyFinancialsPreparedFixtures(): Array<FixtureData<EuTaxonomyFinancialsData>> {
  const preparedFixtures = [];
  preparedFixtures.push(generateFixturesWithNoNullFields());
  return preparedFixtures;
}

/**
 * Generate a prepared Fixture with no null entries
 * @returns the fixture
 */
function generateFixturesWithNoNullFields(): FixtureData<EuTaxonomyFinancialsData> {
  const newFixture = generateEuTaxonomyFinancialsFixtures(1, 0)[0];
  newFixture.companyInformation.companyName = 'eu-taxonomy-financials-dataset-with-no-null-fields';
  return newFixture;
}
