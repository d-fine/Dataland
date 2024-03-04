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
  preparedFixtures.push(manipulateFixtureForYearWithMultipleSectors(generateSmeFixtures(1)[0], "2023"));
  return preparedFixtures;
}

/**
 * Sets the company name, reporting period and some data in the fixture data to specific values needed for tests.
 * @param input Fixture data to be manipulated
 * @param year the year as a number
 * @returns the manipulated fixture data
 */
function manipulateFixtureForYearWithMultipleSectors(input: FixtureData<SmeData>, year: string): FixtureData<SmeData> {
  // TODO param unnecessary
  input.companyInformation.companyName = "SME-year-" + year;
  input.reportingPeriod = year;
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
