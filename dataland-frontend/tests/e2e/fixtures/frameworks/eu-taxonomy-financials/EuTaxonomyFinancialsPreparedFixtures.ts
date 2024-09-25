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
  // Note: Put the code for prepared fixture generation below. This file will not be overwritten automatically

  const manipulatorFunctions: Array<
    (input: FixtureData<EuTaxonomyFinancialsData>) => FixtureData<EuTaxonomyFinancialsData>
  > = [];
  const preparedFixturesBeforeManipulation = generateEuTaxonomyFinancialsFixtures(manipulatorFunctions.length);

  for (let i = 0; i < manipulatorFunctions.length; i++) {
    preparedFixtures.push(manipulatorFunctions[i](preparedFixturesBeforeManipulation[i]));
  }

  return preparedFixtures;
}
