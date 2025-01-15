import { type FixtureData } from '@sharedUtils/Fixtures';
import { type IntegrationtestingData } from '@clients/backend';
import { generateIntegrationtestingFixtures } from './IntegrationtestingDataFixtures';

/**
 * Generates integrationTesting prepared fixtures by generating random integrationTesting datasets and
 * afterwards manipulating some fields via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateIntegrationtestingPreparedFixtures(): Array<FixtureData<IntegrationtestingData>> {
  const preparedFixtures = [];
  // Note: Put the code for prepared fixture generation below. This file will not be overwritten automatically

  const manipulatorFunctions: Array<
    (input: FixtureData<IntegrationtestingData>) => FixtureData<IntegrationtestingData>
  > = [];
  const preparedFixturesBeforeManipulation = generateIntegrationtestingFixtures(manipulatorFunctions.length);

  for (let i = 0; i < manipulatorFunctions.length; i++) {
    preparedFixtures.push(manipulatorFunctions[i](preparedFixturesBeforeManipulation[i]));
  }

  return preparedFixtures;
}
