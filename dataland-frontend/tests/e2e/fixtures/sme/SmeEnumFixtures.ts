import { faker } from "@faker-js/faker";
import {
  Industry,
  CompanyAgeBracket,
  EnergyEfficiencyBracket,
  EnergyProductionBracket,
  HeatSource,
} from "@clients/backend";

const possibleCompanyAgeBracketUndefinedValues = [undefined, ...Object.values(CompanyAgeBracket)];
export function randomCompanyAgeBracketOrUndefined(): CompanyAgeBracket | undefined {
  return faker.helpers.arrayElement(possibleCompanyAgeBracketUndefinedValues);
}

const possibleIndustryUndefinedValues = [undefined, ...Object.values(Industry)];
export function randomIndustryOrUndefined(): Industry | undefined {
  return faker.helpers.arrayElement(possibleIndustryUndefinedValues);
}

const possibleHeatSourceUndefinedValues = [undefined, ...Object.values(HeatSource)];
export function randomHeatSourceOrUndefined(): HeatSource | undefined {
  return faker.helpers.arrayElement(possibleHeatSourceUndefinedValues);
}

const possibleEnergyEfficiencyBracketUndefinedValues = [undefined, ...Object.values(EnergyEfficiencyBracket)];
export function randomEnergyEfficiencyBracketOrUndefined(): EnergyEfficiencyBracket | undefined {
  return faker.helpers.arrayElement(possibleEnergyEfficiencyBracketUndefinedValues);
}

const possibleEnergyProductionBracketUndefinedValues = [undefined, ...Object.values(EnergyProductionBracket)];
export function randomEnergyProductionBracketOrUndefined(): EnergyProductionBracket | undefined {
  return faker.helpers.arrayElement(possibleEnergyProductionBracketUndefinedValues);
}
