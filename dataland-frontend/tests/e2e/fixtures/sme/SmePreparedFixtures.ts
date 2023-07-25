import { FixtureData } from "@sharedUtils/Fixtures";
import { SmeData } from "@clients/backend";
import { generateSmeFixtures } from "@e2e/fixtures/sme/SmeDataFixtures";

/**
 * Generates one SME prepared fixture dataset by generating a random SME dataset and afterwards manipulating some fields
 * via a manipulator-function to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateSmePreparedFixtures(): Array<FixtureData<SmeData>> {
  const preparedFixtures = [];
  preparedFixtures.push(manipulateFixtureForYear(generateSmeFixtures(1)[0], 2023));
  return preparedFixtures;
}

/**
 * Sets the company name, data date and reporting period in the fixture data to
 * specific values needed for tests.
 * @param input Fixture data to be manipulated
 * @param year the year as a number
 * @returns the manipulated fixture data
 */
function manipulateFixtureForYear(input: FixtureData<SmeData>, year: number): FixtureData<SmeData> {
  input.companyInformation.companyName = "SME-year-" + String(year);
  input.t.financialYear = year;
  input.reportingPeriod = String(year);
  return input;
}
