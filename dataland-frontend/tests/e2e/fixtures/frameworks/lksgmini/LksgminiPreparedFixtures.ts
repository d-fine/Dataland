import { type FixtureData } from "@sharedUtils/Fixtures";
import { type LksgminiData } from "@clients/backend";
import { generateLksgminiFixtures } from "./LksgminiDataFixtures";

/**
 * Generates lksgmini prepared fixtures by generating random lksgmini datasets and
 * afterwards manipulating some fields via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateLksgminiPreparedFixtures(): Array<FixtureData<LksgminiData>> {
  const preparedFixtures = [];
  // Note: Put the code for prepared fixture generation below. This file will not be overwritten automatically

  const manipulatorFunctions: Array<(input: FixtureData<LksgminiData>) => FixtureData<LksgminiData>> = [];
  const preparedFixturesBeforeManipulation = generateLksgminiFixtures(manipulatorFunctions.length);

  for (let i = 0; i < manipulatorFunctions.length; i++) {
    preparedFixtures.push(manipulatorFunctions[i](preparedFixturesBeforeManipulation[i]));
  }

  return preparedFixtures;
}
