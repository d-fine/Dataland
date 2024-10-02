import { type FixtureData } from '@sharedUtils/Fixtures';
import { type EuTaxonomyFinancialsData } from '@clients/backend';
import {
  generateEuTaxonomyFinancialsData,
  generateEuTaxonomyFinancialsFixtures,
} from './EuTaxonomyFinancialsDataFixtures';

/**
 * Generates eu-taxonomy-financials prepared fixtures by generating random eu-taxonomy-financials datasets and
 * afterwards manipulating some fields via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateEuTaxonomyFinancialsPreparedFixtures(): Array<FixtureData<EuTaxonomyFinancialsData>> {
  const preparedFixtures = [];
  preparedFixtures.push(generateFixtureWithNoNullFields());
  preparedFixtures.push(generateFixtureForFileUploadAndLinkingTest());
  return preparedFixtures;
}

/**
 * Generate a prepared Fixture with no null entries
 * @returns the fixture
 */
function generateFixtureWithNoNullFields(): FixtureData<EuTaxonomyFinancialsData> {
  const newFixture = generateEuTaxonomyFinancialsFixtures(1, 0)[0];
  newFixture.companyInformation.companyName = 'eu-taxonomy-financials-dataset-with-no-null-fields';
  return newFixture;
}

/**
 * Generate a prepared Fixture with specific fields for the respective e2e-test
 * @returns the fixture
 */
function generateFixtureForFileUploadAndLinkingTest(): FixtureData<EuTaxonomyFinancialsData> {
  const newFixture = generateEuTaxonomyFinancialsFixtures(1, 1)[0];
  const fullDataSet = generateEuTaxonomyFinancialsData(0);
  newFixture.companyInformation.companyName = 'for-file-upload-and-linking-test';
  newFixture.t.general = fullDataSet.general;
  if (newFixture.t.creditInstitution?.assetsForCalculationOfGreenAssetRatio) {
    newFixture.t.creditInstitution.assetsForCalculationOfGreenAssetRatio =
      fullDataSet.creditInstitution?.assetsForCalculationOfGreenAssetRatio;
  }
  return newFixture; // TODO replace all tests that dont need lots of data with this!  then they need less memory
}
