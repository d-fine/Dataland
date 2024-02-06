import { type FixtureData } from "@sharedUtils/Fixtures";
import { type EutaxonomyNonFinancialsData } from "@clients/backend";
import {
  generateEutaxonomyNonFinancialsData,
  generateEutaxonomyNonFinancialsFixtures,
} from "./EutaxonomyNonFinancialsDataFixtures";

/**
 * Generates eutaxonomy-non-financials prepared fixtures by generating random eutaxonomy-non-financials datasets and
 * afterwards manipulating some fields via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateEutaxonomyNonFinancialsPreparedFixtures(): Array<FixtureData<EutaxonomyNonFinancialsData>> {
  const preparedFixtures = [];
  // Note: Put the code for prepared fixture generation below. This file will not be overwritten automatically

  const manipulatorFunctions: Array<
    (input: FixtureData<EutaxonomyNonFinancialsData>) => FixtureData<EutaxonomyNonFinancialsData>
  > = [createDatasetThatHasAllFieldsDefined];
  const preparedFixturesBeforeManipulation = generateEutaxonomyNonFinancialsFixtures(manipulatorFunctions.length);

  for (let i = 0; i < manipulatorFunctions.length; i++) {
    preparedFixtures.push(manipulatorFunctions[i](preparedFixturesBeforeManipulation[i]));
  }
  preparedFixtures.push(generateEutaxonomyNonFinancialsFixtures(1, 0)[0]);
  return preparedFixtures;
}

/**
 * Creates a prepared fixture that has only defined fields and no fields with missing values
 * @param input the base fixture to modify
 * @returns the modified fixture
 */
function createDatasetThatHasAllFieldsDefined(
  input: FixtureData<EutaxonomyNonFinancialsData>,
): FixtureData<EutaxonomyNonFinancialsData> {
  input.companyInformation.companyName = "all-fields-defined-for-eu-taxo-non-financials";
  input.t = generateEutaxonomyNonFinancialsData(0);
  return input;
}
//TODO naceCodes is null im prepared fixture!!!
