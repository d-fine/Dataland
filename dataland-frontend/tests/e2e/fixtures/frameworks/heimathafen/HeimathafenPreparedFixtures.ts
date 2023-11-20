import { type FixtureData } from "@sharedUtils/Fixtures";
import { type HeimathafenData } from "@clients/backend";
import { generateHeimathafenFixtures } from "./HeimathafenDataFixtures";

/**
 * Generates heimathafen prepared fixtures by generating random heimathafen datasets and
 * afterwards manipulating some fields via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateHeimathafenPreparedFixtures(): Array<FixtureData<HeimathafenData>> {
  const preparedFixtures = [];
  // Note: Put the code for prepared fixture generation below. This file will not be overwritten automatically

  const manipulatorFunctions: Array<(input: FixtureData<HeimathafenData>) => FixtureData<HeimathafenData>> = [];
  const preparedFixturesBeforeManipulation = generateHeimathafenFixtures(manipulatorFunctions.length);

  for (let i = 0; i < manipulatorFunctions.length; i++) {
    preparedFixtures.push(manipulatorFunctions[i](preparedFixturesBeforeManipulation[i]));
  }

  return preparedFixtures;
}
