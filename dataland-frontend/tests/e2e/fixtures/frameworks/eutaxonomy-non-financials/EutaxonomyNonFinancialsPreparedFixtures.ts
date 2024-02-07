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
  > = [
    createCompanyAlphaWithAllFieldsDefined,
    createCompanyBetaWithAllFieldsDefined,
    createCompanyGammaWithAllFieldsDefined,
  ];
  const preparedFixturesBeforeManipulation = generateEutaxonomyNonFinancialsFixtures(manipulatorFunctions.length);

  for (let i = 0; i < manipulatorFunctions.length; i++) {
    preparedFixtures.push(manipulatorFunctions[i](preparedFixturesBeforeManipulation[i]));
  }
  return preparedFixtures;
}

/**
 * Creates a prepared fixture that has only defined fields and no fields with missing values
 * @param input the base fixture to modify
 * @param companyName the name of the associated company
 * @param reportingPeriod of the dataset
 * @returns the modified fixture
 */
function createDatasetThatHasAllFieldsDefined(
  input: FixtureData<EutaxonomyNonFinancialsData>,
  companyName: string,
  reportingPeriod: string,
): FixtureData<EutaxonomyNonFinancialsData> {
  input.companyInformation.companyName = companyName;
  input.reportingPeriod = reportingPeriod;
  input.t = generateEutaxonomyNonFinancialsData(0);
  return input;
}

/**
 * Creates a prepared fixture that has no fields with missing values and a specific name
 * @param input the base fixture to modify
 * @returns the modified fixture
 */
function createCompanyAlphaWithAllFieldsDefined(
  input: FixtureData<EutaxonomyNonFinancialsData>,
): FixtureData<EutaxonomyNonFinancialsData> {
  return createDatasetThatHasAllFieldsDefined(input, "all-fields-defined-for-eu-taxo-non-financials-alpha", "2023");
}

/**
 * Creates a prepared fixture that has no fields with missing values and a specific name
 * @param input the base fixture to modify
 * @returns the modified fixture
 */
function createCompanyBetaWithAllFieldsDefined(
  input: FixtureData<EutaxonomyNonFinancialsData>,
): FixtureData<EutaxonomyNonFinancialsData> {
  return createDatasetThatHasAllFieldsDefined(input, "all-fields-defined-for-eu-taxo-non-financials-beta", "2022");
}

/**
 * Creates a prepared fixture that has no fields with missing values and a specific name
 * @param input the base fixture to modify
 * @returns the modified fixture
 */
function createCompanyGammaWithAllFieldsDefined(
  input: FixtureData<EutaxonomyNonFinancialsData>,
): FixtureData<EutaxonomyNonFinancialsData> {
  return createDatasetThatHasAllFieldsDefined(input, "all-fields-defined-for-eu-taxo-non-financials-gamma", "2021");
}
