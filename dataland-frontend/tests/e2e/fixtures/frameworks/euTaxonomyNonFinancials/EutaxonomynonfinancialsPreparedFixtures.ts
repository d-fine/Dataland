import { type FixtureData } from "@sharedUtils/Fixtures";
import { type EutaxonomynonfinancialsData } from "@clients/backend";
import { generateEutaxonomynonfinancialsFixtures } from "./EutaxonomynonfinancialsDataFixtures";

/**
 * Generates euTaxonomyNonFinancials prepared fixtures by generating random euTaxonomyNonFinancials datasets and
 * afterwards manipulating some fields via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateEutaxonomynonfinancialsPreparedFixtures(): Array<FixtureData<EutaxonomynonfinancialsData>> {
  const preparedFixtures = [];
  // Note: Put the code for prepared fixture generation below. This file will not be overwritten automatically

  const manipulatorFunctions: Array<
    (input: FixtureData<EutaxonomynonfinancialsData>) => FixtureData<EutaxonomynonfinancialsData>
  > = [];
  const preparedFixturesBeforeManipulation = generateEutaxonomynonfinancialsFixtures(manipulatorFunctions.length);

  for (let i = 0; i < manipulatorFunctions.length; i++) {
    preparedFixtures.push(manipulatorFunctions[i](preparedFixturesBeforeManipulation[i]));
  }

  return preparedFixtures;
}
