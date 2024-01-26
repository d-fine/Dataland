import { type FixtureData } from "@sharedUtils/Fixtures";
import { type ${frameworkBaseName}Data } from "@clients/backend";
import { generate${frameworkBaseName}Fixtures } from "./${frameworkBaseName}DataFixtures";

/**
 * Generates ${frameworkIdentifier} prepared fixtures by generating random ${frameworkIdentifier} datasets and
 * afterwards manipulating some fields via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generate${frameworkBaseName}PreparedFixtures(): Array<FixtureData<${frameworkBaseName}Data>> {
  const preparedFixtures = [];
  // Note: Put the code for prepared fixture generation below. This file will not be overwritten automatically

  const manipulatorFunctions: Array<(input: FixtureData<${frameworkBaseName}Data>) => FixtureData<${frameworkBaseName}Data>> = [];
  const preparedFixturesBeforeManipulation = generate${frameworkBaseName}Fixtures(manipulatorFunctions.length);

  for (let i = 0; i < manipulatorFunctions.length; i++) {
    preparedFixtures.push(manipulatorFunctions[i](preparedFixturesBeforeManipulation[i]));
  }

  return preparedFixtures;
}
