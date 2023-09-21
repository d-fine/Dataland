import { type FixtureData } from "@sharedUtils/Fixtures";
import { type SmeData } from "@clients/backend";
import { generateSmeFixtures } from "@e2e/fixtures/sme/SmeDataFixtures";
import { generateNaceCodes } from "@e2e/fixtures/common/NaceCodeFixtures";

/**
 * Generates one SME prepared fixture dataset by generating a random SME dataset and afterwards manipulating some fields
 * via a manipulator-function to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateSmePreparedFixtures(): Array<FixtureData<SmeData>> {
  const preparedFixtures = [];
  preparedFixtures.push(manipulateFixtureForYearWithMultipleSectors(generateSmeFixtures(1)[0], "2023"));
  preparedFixtures.push(manipulateFixtureForMaximumAddress(generateSmeFixtures(1)[0]));
  preparedFixtures.push(manipulateFixtureForMinimumAddress(generateSmeFixtures(1)[0]));
  return preparedFixtures;
}

/**
 * Sets the company name, reporting period and some data in the fixture data to specific values needed for tests.
 * @param input Fixture data to be manipulated
 * @param year the year as a number
 * @returns the manipulated fixture data
 */
function manipulateFixtureForYearWithMultipleSectors(input: FixtureData<SmeData>, year: string): FixtureData<SmeData> {
  input.companyInformation.companyName = "SME-year-" + year;
  input.reportingPeriod = year;
  input.t.general.basicInformation.sector = generateNaceCodes(2);
  input.t.power ??= {};
  input.t.power.investments ??= {};
  input.t.power.investments.percentageForInvestmentsInEnhancingEnergyEfficiency = "LessThan1";
  input.t.power.consumption ??= {};
  input.t.power.consumption.percentRangeForEnergyConsumptionCoveredByOwnRenewablePowerGeneration = "LessThan25";
  input.t.general.companyFinancials = {
    revenueInEur: 0,
    operatingCostInEur: 1000000,
    capitalAssetsInEur: 2000000,
  };
  return input;
}

/**
 * Sets the company name and headquarters address with maximum number of address fields
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureForMaximumAddress(input: FixtureData<SmeData>): FixtureData<SmeData> {
  input.companyInformation.companyName = "SME-maximum-address";
  input.reportingPeriod = "2021";
  input.t.general.basicInformation.addressOfHeadquarters = {
    streetAndHouseNumber: "Main Street 12",
    postalCode: "12345",
    city: "Nonexistingen",
    state: "Fiction",
    country: "Imagination",
  };
  return input;
}

/**
 * Sets the company name and headquarters address with minimum number of address fields
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureForMinimumAddress(input: FixtureData<SmeData>): FixtureData<SmeData> {
  input.companyInformation.companyName = "SME-minimum-address";
  input.reportingPeriod = "2022";
  input.t.general.basicInformation.addressOfHeadquarters = {
    streetAndHouseNumber: undefined,
    postalCode: undefined,
    city: "City 17",
    state: undefined,
    country: "Uninspired",
  };
  return input;
}
