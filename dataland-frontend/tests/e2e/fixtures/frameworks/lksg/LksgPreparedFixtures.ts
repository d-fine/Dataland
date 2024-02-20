import { type FixtureData } from "@sharedUtils/Fixtures";
import { type LksgData } from "@clients/backend";
import { generateLksgFixtures } from "./LksgDataFixtures";

/**
 * Generates lksg prepared fixtures by generating random lksg datasets and
 * afterwards manipulating some fields via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateLksgPreparedFixtures(): Array<FixtureData<LksgData>> {
  const preparedFixtures = [];
  // Note: Put the code for prepared fixture generation below. This file will not be overwritten automatically

  const manipulatorFunctions: Array<(input: FixtureData<LksgData>) => FixtureData<LksgData>> = [
    generateFixutreWithNoNullFields,
    generateFixtureWithALotOfNullFields,
  ];
  const preparedFixturesBeforeManipulation = generateLksgFixtures(manipulatorFunctions.length);

  for (let i = 0; i < manipulatorFunctions.length; i++) {
    preparedFixtures.push(manipulatorFunctions[i](preparedFixturesBeforeManipulation[i]));
  }

  return preparedFixtures;
}

/**
 * Generates a lksg fixture with no null values
 * @returns the fixture
 */
function generateFixutreWithNoNullFields(): FixtureData<LksgData> {
  const newFixture = generateLksgFixtures(1, 0)[0];
  newFixture.companyInformation.companyName = "lksg-all-fields";
  return newFixture;
}

/**
 * Generates a lksg fixture with a lot of null fields
 * @returns the fixture
 */
function generateFixtureWithALotOfNullFields(): FixtureData<LksgData> {
  const newFixture = generateLksgFixtures(1, 80)[0];
  newFixture.companyInformation.companyName = "lksg-a-lot-of-nulls";
  return newFixture;
}
