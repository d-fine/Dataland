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
  preparedFixtures.push(generateEmptyReferencedReportsFixture());
  preparedFixtures.push(generateMinimalisticEuTaxoFinancialsFixtureForBlanketTest());
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
  } else {
    throw Error('Could not set assetsForCalculationOfGreenAssetRatio due to missing parent-object.');
  }
  return newFixture;
}

/**
 * Generate a prepared Fixture with the referenced reports property set to "null".
 * @returns the fixture
 */
function generateEmptyReferencedReportsFixture(): FixtureData<EutaxonomyFinancialsData> {
  const newFixture = generateEutaxonomyFinancialsFixtures(1, 0)[0];
  newFixture.companyInformation.companyName = 'TestForIncompleteReferencedReport';
  if (newFixture.t.general?.general?.referencedReports) {
    newFixture.t.general.general.referencedReports = null;
  } else {
    throw Error('Could not set referenced reports to null due to missing parent-object.');
  }
  return newFixture;
}

/**
 * Generates a minimalistic dataset with all "field-types" for the blanket test, as a complete "non-null"-dataset
 * is too memory-heavy for cypress to render.
 * @returns the fixture
 */
function generateMinimalisticEuTaxoFinancialsFixtureForBlanketTest(): FixtureData<EutaxonomyFinancialsData> {
  const newFixture = generateEutaxonomyFinancialsFixtures(1, 1)[0];
  const fullDataSet = generateEutaxonomyFinancialsData(0);
  newFixture.companyInformation.companyName = 'minimalistic-all-field-types-dataset-for-blanket-test';
  newFixture.t.general = fullDataSet.general;

  if (newFixture.t.creditInstitution?.assetsForCalculationOfGreenAssetRatio) {
    newFixture.t.creditInstitution.assetsForCalculationOfGreenAssetRatio =
      fullDataSet.creditInstitution?.assetsForCalculationOfGreenAssetRatio;
  } else {
    throw Error('Could not set assetsForCalculationOfGreenAssetRatio due to missing parent-object.');
  }

  if (newFixture.t.creditInstitution?.turnoverBasedGreenAssetRatioStock) {
    newFixture.t.creditInstitution.turnoverBasedGreenAssetRatioStock.substantialContributionToClimateChangeAdaptationInPercentEligible =
      fullDataSet.creditInstitution?.turnoverBasedGreenAssetRatioStock?.substantialContributionToClimateChangeAdaptationInPercentEligible;
  } else {
    throw Error(
      'Could not set substantialContributionToClimateChangeAdaptationInPercentEligible due to missing parent-object.'
    );
  }
  return newFixture;
}
