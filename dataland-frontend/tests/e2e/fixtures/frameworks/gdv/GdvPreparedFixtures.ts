import { type FixtureData } from "@sharedUtils/Fixtures";
import { type GdvData } from "@clients/backend";
import { generateGdvFixtures } from "./GdvDataFixtures";

/**
 * Generates gdv prepared fixtures by generating random gdv datasets and
 * afterwards manipulating some fields via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateGdvPreparedFixtures(): Array<FixtureData<GdvData>> {
  const preparedFixtures = [];
  // Note: Put the code for prepared fixture generation below. This file will not be overwritten automatically

  const manipulatorFunctions: Array<(input: FixtureData<GdvData>) => FixtureData<GdvData>> = [];
  const preparedFixturesBeforeManipulation = generateGdvFixtures(manipulatorFunctions.length);

  for (let i = 0; i < manipulatorFunctions.length; i++) {
    preparedFixtures.push(manipulatorFunctions[i](preparedFixturesBeforeManipulation[i]));
  }

  return preparedFixtures;
}
