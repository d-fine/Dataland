import { faker } from "@faker-js/faker";
import {
  Industry,
  CompanyAgeBracket,
  EnergyEfficiencyBracket,
  EnergyProductionBracket,
  HeatSource,
} from "@clients/backend";

const possibleCompanyAgeBracketUndefinedValues = [undefined, ...Object.values(CompanyAgeBracket)];

/**
 * Picks a random company age bracket or undefined
 *
 * @returns a random company age bracket or undefined
 */
export function randomCompanyAgeBracketOrUndefined(): CompanyAgeBracket | undefined {
  return faker.helpers.arrayElement(possibleCompanyAgeBracketUndefinedValues);
}

const possibleIndustryUndefinedValues = [undefined, ...Object.values(Industry)];
/**
 * Picks a random industry or undefined
 *
 * @returns a random industry or undefined
 */
export function randomIndustryOrUndefined(): Industry | undefined {
  return faker.helpers.arrayElement(possibleIndustryUndefinedValues);
}

const possibleHeatSourceUndefinedValues = [undefined, ...Object.values(HeatSource)];
/**
 * Picks a random heat source or undefined
 *
 * @returns a random heat source or undefined
 */
export function randomHeatSourceOrUndefined(): HeatSource | undefined {
  return faker.helpers.arrayElement(possibleHeatSourceUndefinedValues);
}

const possibleEnergyEfficiencyBracketUndefinedValues = [undefined, ...Object.values(EnergyEfficiencyBracket)];
/**
 * Picks a random energy efficiency bracket or undefined
 *
 * @returns a random energy efficiency bracket or undefined
 */
export function randomEnergyEfficiencyBracketOrUndefined(): EnergyEfficiencyBracket | undefined {
  return faker.helpers.arrayElement(possibleEnergyEfficiencyBracketUndefinedValues);
}

const possibleEnergyProductionBracketUndefinedValues = [undefined, ...Object.values(EnergyProductionBracket)];
/**
 * Picks a random energy production bracket or undefined
 *
 * @returns a random energy production bracket or undefined
 */
export function randomEnergyProductionBracketOrUndefined(): EnergyProductionBracket | undefined {
  return faker.helpers.arrayElement(possibleEnergyProductionBracketUndefinedValues);
}
