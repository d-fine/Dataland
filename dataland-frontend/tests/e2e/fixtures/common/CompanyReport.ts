import { faker } from "@faker-js/faker";
import { CompanyReport } from "@clients/backend";

export function randomCompanyReport(): CompanyReport {
  return faker.helpers.arrayElement(Object.values(CompanyReport));
}
