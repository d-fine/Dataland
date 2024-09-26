import { type FixtureData } from '@sharedUtils/Fixtures';
import { type NuclearAndGasData } from '@clients/backend';
import { generateNuclearAndGasFixtures } from './NuclearAndGasDataFixtures';

/**
 * Generates nuclear-and-gas prepared fixtures by generating random nuclear-and-gas datasets and
 * afterwards manipulating some fields via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateNuclearAndGasPreparedFixtures(): Array<FixtureData<NuclearAndGasData>> {
  const preparedFixtures = [];
  // Note: Put the code for prepared fixture generation below. This file will not be overwritten automatically

  const manipulatorFunctions: Array<(input: FixtureData<NuclearAndGasData>) => FixtureData<NuclearAndGasData>> = [];
  const preparedFixturesBeforeManipulation = generateNuclearAndGasFixtures(manipulatorFunctions.length);

  for (let i = 0; i < manipulatorFunctions.length; i++) {
    preparedFixtures.push(manipulatorFunctions[i](preparedFixturesBeforeManipulation[i]));
  }

  return preparedFixtures;
}
