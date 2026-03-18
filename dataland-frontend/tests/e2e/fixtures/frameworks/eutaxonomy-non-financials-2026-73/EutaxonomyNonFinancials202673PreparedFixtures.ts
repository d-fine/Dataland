import { type FixtureData } from '@sharedUtils/Fixtures';
import { type EutaxonomyNonFinancials202673Data } from '@clients/backend';
import { generateEutaxonomyNonFinancials202673Fixtures } from './EutaxonomyNonFinancials202673DataFixtures';

/**
 * Generates eutaxonomy-non-financials-2026-73 prepared fixtures by generating random eutaxonomy-non-financials-2026-73 datasets and
 * afterwards manipulating some fields via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateEutaxonomyNonFinancials202673PreparedFixtures(): Array<
  FixtureData<EutaxonomyNonFinancials202673Data>
> {
  const preparedFixtures = [];
  // Note: Put the code for prepared fixture generation below. This file will not be overwritten automatically

  const manipulatorFunctions: Array<
    (input: FixtureData<EutaxonomyNonFinancials202673Data>) => FixtureData<EutaxonomyNonFinancials202673Data>
  > = [];
  const preparedFixturesBeforeManipulation = generateEutaxonomyNonFinancials202673Fixtures(manipulatorFunctions.length);

  for (let i = 0; i < manipulatorFunctions.length; i++) {
    preparedFixtures.push(manipulatorFunctions[i](preparedFixturesBeforeManipulation[i]));
  }

  return preparedFixtures;
}
