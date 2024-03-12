import { type FixtureData } from "@sharedUtils/Fixtures";
import { type SmeData } from "@clients/backend";
import { generateSmeFixtures } from "@e2e/fixtures/frameworks/sme/SmeDataFixtures";
import { generateNaceCodes } from "@e2e/fixtures/common/NaceCodeFixtures";

/**
 * Generates one SME prepared fixture dataset by generating a random SME dataset and afterwards manipulating some fields
 * via a manipulator-function to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateSmePreparedFixtures(): Array<FixtureData<SmeData>> {
  const preparedFixtures = [];
  preparedFixtures.push(manipulateFixtureToIdentifyItAsNoNullFields(generateSmeFixtures(1, 0)[0]));
  return preparedFixtures;
}

/**
 * Sets the company name to a specific value to be able to pick this dataset from the prepared fixtures.
 * Furthermore, one enum field needs to be sorted, so the object-comparison in the blanket test does not fail due to a
 * different order, and also the sectors shall have some nace codes.
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureToIdentifyItAsNoNullFields(input: FixtureData<SmeData>): FixtureData<SmeData> {
  input.companyInformation.companyName = "Sme-dataset-with-no-null-fields";
  input.t.general.basicInformation.sectors = generateNaceCodes(2);
  input.t.insurances?.naturalHazards?.naturalHazardsCovered?.sort();
  return input;
}
