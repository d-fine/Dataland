import { generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import { SfdrData } from "@clients/backend";
import { generateSfdrData } from "./SfdrDataFixtures";
import { FixtureData } from "@sharedUtils/Fixtures";

type generatorFunction = (input: FixtureData<SfdrData>) => FixtureData<SfdrData>;

/**
 * Generates SFDR prepared fixtures by generating random SFDR datasets and afterwards manipulating some fields
 * via manipulator-functions to set specific values for those fields.
 *
 * @returns the prepared fixtures
 */
export async function generateSfdrPreparedFixtures(): Promise<Array<FixtureData<SfdrData>>> {
  const manipulatorFunctions: Array<generatorFunction> = [
    manipulateFixtureForOneSfdr,
    manipulateFixtureForTwoSfdrDataSetsInDifferentYears,
  ];
  const preparedFixturesBeforeManipulation = await generateFixtureDataset<SfdrData>(
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
function manipulateFixtureForOneSfdr(input: FixtureData<SfdrData>): FixtureData<SfdrData> {
  input.companyInformation.companyName = "company-with-one-sfdr-data-set";
  input.t.social!.general!.annualReport = "/test_pdf.pdf";
  return input;
}

/**
 * Sets the company name and the date in the fixture data to a specific string
 *
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureForTwoSfdrDataSetsInDifferentYears(input: FixtureData<SfdrData>): FixtureData<SfdrData> {
  input.companyInformation.companyName = "two-sfdr-data-sets-in-different-years";
  input.t.social!.general!.fiscalYearEnd = "2020-01-03";
  return input;
}
