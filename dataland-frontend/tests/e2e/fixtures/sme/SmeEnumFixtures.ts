import { faker } from "@faker-js/faker";
import { Branch, CompanyAgeBracket } from "@clients/backend";

const possibleCompanyAgeBracketUndefinedValues = [undefined, ...Object.values(CompanyAgeBracket)];
export function randomCompanyAgeBracketOrUndefined(): CompanyAgeBracket | undefined {
  return faker.helpers.arrayElement(possibleCompanyAgeBracketUndefinedValues);
}

const possibleBranchUndefinedValues = [undefined, ...Object.values(Branch)];
export function randomBranchOrUndefined(): Branch | undefined {
  return faker.helpers.arrayElement(possibleBranchUndefinedValues);
}
