import { type FixtureData } from "@sharedUtils/Fixtures";
import { type EsgquestionnaireData } from "@clients/backend";
import { generateEsgquestionnaireFixtures } from "./EsgquestionnaireDataFixtures";

/**
 * Generates esgquestionnaire prepared fixtures by generating random esgquestionnaire datasets and
 * afterwards manipulating some fields via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateEsgquestionnairePreparedFixtures(): Array<FixtureData<EsgquestionnaireData>> {
  const preparedFixtures = [];
  // Note: Put the code for prepared fixture generation below. This file will not be overwritten automatically

  const manipulatorFunctions: Array<(input: FixtureData<EsgquestionnaireData>) => FixtureData<EsgquestionnaireData>> = [];
  const preparedFixturesBeforeManipulation = generateEsgquestionnaireFixtures(manipulatorFunctions.length);

  for (let i = 0; i < manipulatorFunctions.length; i++) {
    preparedFixtures.push(manipulatorFunctions[i](preparedFixturesBeforeManipulation[i]));
  }

  return preparedFixtures;
}
