import { type FixtureData } from "@sharedUtils/Fixtures";
import { type ${frameworkIdentifier?cap_first}Data } from "@clients/backend";
import { generate${frameworkIdentifier?cap_first}Fixtures } from "./${frameworkIdentifier?cap_first}DataFixtures";

/**
 * Generates ${frameworkIdentifier} prepared fixtures by generating random ${frameworkIdentifier} datasets and
 * afterwards manipulating some fields via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generate${frameworkIdentifier?cap_first}PreparedFixtures(): Array<FixtureData<${frameworkIdentifier?cap_first}Data>> {
  const preparedFixtures = [];
  // Note: Put the code for prepared fixture generation below. This file will not be overwritten automatically

  const manipulatorFunctions: Array<(input: FixtureData<${frameworkIdentifier?cap_first}Data>) => FixtureData<${frameworkIdentifier?cap_first}Data>> = [];
  const preparedFixturesBeforeManipulation = generate${frameworkIdentifier?cap_first}Fixtures(manipulatorFunctions.length);

  for (let i = 0; i < manipulatorFunctions.length; i++) {
    preparedFixtures.push(manipulatorFunctions[i](preparedFixturesBeforeManipulation[i]));
  }

  return preparedFixtures;
}
