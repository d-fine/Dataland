import { type FixtureData } from '@sharedUtils/Fixtures';
import { type LksgmediumData } from '@clients/backend';
import { generateLksgmediumFixtures } from './LksgmediumDataFixtures';

/**
 * Generates lksgmedium prepared fixtures by generating random lksgmedium datasets and
 * afterwards manipulating some fields via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateLksgmediumPreparedFixtures(): Array<FixtureData<LksgmediumData>> {
  const preparedFixtures = [];
  // Note: Put the code for prepared fixture generation below. This file will not be overwritten automatically

  const manipulatorFunctions: Array<(input: FixtureData<LksgmediumData>) => FixtureData<LksgmediumData>> = [];
  const preparedFixturesBeforeManipulation = generateLksgmediumFixtures(manipulatorFunctions.length);

  for (let i = 0; i < manipulatorFunctions.length; i++) {
    preparedFixtures.push(manipulatorFunctions[i](preparedFixturesBeforeManipulation[i]));
  }

  return preparedFixtures;
}
