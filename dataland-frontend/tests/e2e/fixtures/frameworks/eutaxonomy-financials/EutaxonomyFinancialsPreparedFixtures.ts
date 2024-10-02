import { type FixtureData } from '@sharedUtils/Fixtures';
import { type EutaxonomyFinancialsData } from '@clients/backend';
import {
  generateEutaxonomyFinancialsData,
  generateEutaxonomyFinancialsFixtures,
} from './EutaxonomyFinancialsDataFixtures';

/**
 * Generates eutaxonomy-financials prepared fixtures by generating random eutaxonomy-financials datasets and
 * afterwards manipulating some fields via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateEutaxonomyFinancialsPreparedFixtures(): Array<FixtureData<EutaxonomyFinancialsData>> {
  const preparedFixtures = [];
  preparedFixtures.push(generateFixtureWithNoNullFields());
  preparedFixtures.push(generateLightweightEuTaxoFinancialsFixture());
  return preparedFixtures;
}

/**
 * Generate a prepared Fixture with no null entries
 * @returns the fixture
 */
function generateFixtureWithNoNullFields(): FixtureData<EutaxonomyFinancialsData> {
  const newFixture = generateEutaxonomyFinancialsFixtures(1, 0)[0];
  newFixture.companyInformation.companyName = 'eutaxonomy-financials-dataset-with-no-null-fields';
  return newFixture;
}

/**
 * Generate a prepared Fixture with a couple of datapoints filled. The rest is null to keep the fixture small.
 * @returns the fixture
 */
function generateLightweightEuTaxoFinancialsFixture(): FixtureData<EutaxonomyFinancialsData> {
  const newFixture = generateEutaxonomyFinancialsFixtures(1, 1)[0];
  const fullDataSet = generateEutaxonomyFinancialsData(0);
  newFixture.companyInformation.companyName = 'lighweight-eu-taxo-financials-dataset';
  newFixture.t.general = fullDataSet.general;
  if (newFixture.t.creditInstitution?.assetsForCalculationOfGreenAssetRatio) {
    newFixture.t.creditInstitution.assetsForCalculationOfGreenAssetRatio =
      fullDataSet.creditInstitution?.assetsForCalculationOfGreenAssetRatio;
  }
  return newFixture;
}
