import { type FixtureData } from '@sharedUtils/Fixtures';
import { type PcafData } from '@clients/backend';
import { generatePcafFixtures } from './PcafDataFixtures';

/**
 * Generates pcaf prepared fixtures by generating random pcaf datasets and
 * afterwards manipulating some fields via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generatePcafPreparedFixtures(): Array<FixtureData<PcafData>> {
  const preparedFixtures = [];
  // Note: Put the code for prepared fixture generation below. This file will not be overwritten automatically

  const manipulatorFunctions: Array<(input: FixtureData<PcafData>) => FixtureData<PcafData>> = [];
  const preparedFixturesBeforeManipulation = generatePcafFixtures(manipulatorFunctions.length);

  for (let i = 0; i < manipulatorFunctions.length; i++) {
    preparedFixtures.push(manipulatorFunctions[i](preparedFixturesBeforeManipulation[i]));
  }

  return preparedFixtures;
}
