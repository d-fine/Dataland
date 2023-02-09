import { FixtureData, generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import { SfdrData } from "@clients/backend";
import { generateSfdrData } from "./SfdrDataFixtures";

type generatorFunction = (input: FixtureData<SfdrData>) => FixtureData<SfdrData>;

/**
 * Generates SFDR prepared fixtures by generating random SFDR datasets and afterwards manipulating some fields
 * via manipulator-functions to set specific values for those fields.
 *
 * @returns the prepared fixtures
 */
export function generateSfdrPreparedFixtures(): Array<FixtureData<SfdrData>> {
  const manipulatorFunctions: Array<generatorFunction> = [
    manipulateFixtureForSfdr,
  ];
  const preparedFixturesBeforeManipulation = generateFixtureDataset<SfdrData>(
    generateSfdrData,
    manipulatorFunctions.length
  );
  const preparedFixtures = [];
  for (let i = 0; i < manipulatorFunctions.length; i++) {
    preparedFixtures.push(manipulatorFunctions[i](preparedFixturesBeforeManipulation[i]));
  }
  return preparedFixtures;
}

/**
 * Sets the company name in the fixture data to a specific string
 *
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureForSfdr(input: FixtureData<SfdrData>): FixtureData<SfdrData> {
  input.companyInformation.companyName = "company-with-sfdr-data-set";
  return input;
}
