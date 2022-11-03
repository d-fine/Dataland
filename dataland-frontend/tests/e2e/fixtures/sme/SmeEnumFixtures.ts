import { faker } from "@faker-js/faker";
import {
  Branch,
  CompanyAgeBracket,
  EnergyEfficiencyBracket,
  EnergyProductionBracket,
  HeatSource,
  LegalForm,
} from "@clients/backend";

const possibleCompanyAgeBracketUndefinedValues = [undefined, ...Object.values(CompanyAgeBracket)];
export function randomCompanyAgeBracketOrUndefined(): CompanyAgeBracket | undefined {
  return faker.helpers.arrayElement(possibleCompanyAgeBracketUndefinedValues);
}

const possibleBranchUndefinedValues = [undefined, ...Object.values(Branch)];
export function randomBranchOrUndefined(): Branch | undefined {
  return faker.helpers.arrayElement(possibleBranchUndefinedValues);
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

const possibleLegalFormUndefinedValues = [undefined, ...Object.values(LegalForm)];
export function randomLegalFormOrUndefined(): LegalForm | undefined {
  return faker.helpers.arrayElement(possibleLegalFormUndefinedValues);
}
