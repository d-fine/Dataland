import { type FixtureData } from '@sharedUtils/Fixtures';
import { type EutaxonomyFinancialsData } from '@clients/backend';
import { generateEutaxonomyFinancialsFixtures } from './EutaxonomyFinancialsDataFixtures';

/**
 * Generates eutaxonomy-financials prepared fixtures by generating random eutaxonomy-financials datasets and
 * afterwards manipulating some fields via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateEutaxonomyFinancialsPreparedFixtures(): Array<FixtureData<EutaxonomyFinancialsData>> {
  const preparedFixtures = [];
  preparedFixtures.push(generateFixturesWithNoNullFields());
  return preparedFixtures;
}

/**
 * Generate a prepared Fixture with no null entries
 * @returns the fixture
 */
function generateFixturesWithNoNullFields(): FixtureData<EutaxonomyFinancialsData> {
  const newFixture = generateEutaxonomyFinancialsFixtures(1, 0)[0];
  newFixture.companyInformation.companyName = 'eutaxonomy-financials-dataset-with-no-null-fields';
  return newFixture;
}
