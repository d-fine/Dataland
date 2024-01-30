import { type FixtureData } from "@sharedUtils/Fixtures";
import { type EutaxonomyNonFinancialsData } from "@clients/backend";
import { generateEutaxonomyNonFinancialsFixtures } from "./EutaxonomyNonFinancialsDataFixtures";

/**
 * Generates eutaxonomy-non-financials prepared fixtures by generating random eutaxonomy-non-financials datasets and
 * afterwards manipulating some fields via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateEutaxonomyNonFinancialsPreparedFixtures(): Array<FixtureData<EutaxonomyNonFinancialsData>> {
  const preparedFixtures = [];
  // Note: Put the code for prepared fixture generation below. This file will not be overwritten automatically

  const manipulatorFunctions: Array<(input: FixtureData<EutaxonomyNonFinancialsData>) => FixtureData<EutaxonomyNonFinancialsData>> = [];
  const preparedFixturesBeforeManipulation = generateEutaxonomyNonFinancialsFixtures(manipulatorFunctions.length);

  for (let i = 0; i < manipulatorFunctions.length; i++) {
    preparedFixtures.push(manipulatorFunctions[i](preparedFixturesBeforeManipulation[i]));
  }

  return preparedFixtures;
}
