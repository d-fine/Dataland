import { type FixtureData } from "@sharedUtils/Fixtures";
import {
  type SmeData,
  SmePowerConsumptionEnergyConsumptionCoveredByOwnRenewablePowerGenerationOptions,
  SmePowerInvestmentsInvestmentsInEnhancingEnergyEfficiencyOptions,
} from "@clients/backend";
import { generateSmeFixtures } from "@e2e/fixtures/frameworks/sme/SmeDataFixtures";
import { generateNaceCodes } from "@e2e/fixtures/common/NaceCodeFixtures";

/**
 * Generates one SME prepared fixture dataset by generating a random SME dataset and afterwards manipulating some fields
 * via a manipulator-function to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateSmePreparedFixtures(): Array<FixtureData<SmeData>> {
  const preparedFixtures = [];
  preparedFixtures.push(manipulateFixtureForSpecificTest(generateSmeFixtures(1)[0]));
  preparedFixtures.push(manipulateFixtureForNoNullFields(generateSmeFixtures(1, 0)[0]));
  return preparedFixtures;
}

/**
 * Sets the company name, reporting period and some data in the fixture data to specific values needed for a specific
 * test.
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureForSpecificTest(input: FixtureData<SmeData>): FixtureData<SmeData> {
  const reportingPeriod = "2023";
  input.companyInformation.companyName = "SME-year-" + reportingPeriod;
  input.reportingPeriod = reportingPeriod;
  input.t.general.basicInformation.sectors = generateNaceCodes(2);
  input.t.power ??= {};
  input.t.power.investments ??= {};
  input.t.power.investments.investmentsInEnhancingEnergyEfficiency =
    SmePowerInvestmentsInvestmentsInEnhancingEnergyEfficiencyOptions.LessThan1Percent;
  input.t.power.consumption ??= {};
  input.t.power.consumption.energyConsumptionCoveredByOwnRenewablePowerGeneration =
    SmePowerConsumptionEnergyConsumptionCoveredByOwnRenewablePowerGenerationOptions.LessThan25Percent;
  input.t.general.financialInformation = {
    revenueInEur: 2500000,
    operatingCostInEur: 1000000,
    capitalAssetsInEur: 10000000,
  };
  return input;
}

/**
 * Sets the company name to a specific value to be able to pick this dataset from the prepared fixtures.
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureForNoNullFields(input: FixtureData<SmeData>): FixtureData<SmeData> {
  input.companyInformation.companyName = "Sme-dataset-with-no-null-fields";
  return input;
}
