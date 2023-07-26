import { faker } from "@faker-js/faker";
import {
  EnergySourceForHeatingAndHotWater,
  NaturalHazard,
  PercentRangeForEnergyConsumptionCoveredByOwnRenewablePower,
  PercentRangeForInvestmentsInEnergyEfficiency,
} from "@clients/backend";
import { getRandomNumberOfDistinctElementsFromArray } from "@e2e/fixtures/FixtureUtils";

/**
 * Picks a random percentage range option
 * @returns a random percentage range option
 */
export function getRandomPercentageRangeEnergyConsumption(): PercentRangeForEnergyConsumptionCoveredByOwnRenewablePower {
  return faker.helpers.arrayElement([...Object.values(PercentRangeForEnergyConsumptionCoveredByOwnRenewablePower)]);
}

/**
 * Picks a random percentage range option
 * @returns a random percentage range option
 */
export function getRandomPercentageRangeInvestmentEnergyEfficiency(): PercentRangeForInvestmentsInEnergyEfficiency {
  return faker.helpers.arrayElement([...Object.values(PercentRangeForInvestmentsInEnergyEfficiency)]);
}

/**
 * Picks a random heat source
 * @returns a random heat source
 */
export function getRandomHeatSource(): EnergySourceForHeatingAndHotWater {
  return faker.helpers.arrayElement([...Object.values(EnergySourceForHeatingAndHotWater)]);
}

/**
 * Picks a random natural hazard
 * @returns a random natural hazard
 */
export function getRandomSlectionOfNaturalHazards(): NaturalHazard[] {
  return getRandomNumberOfDistinctElementsFromArray(Object.values(NaturalHazard));
}
