import { CompanyInformation, CompanyReport } from "@clients/backend";
import { generateCompanyInformation } from "./CompanyFixtures";

export type ReferencedReports = { [key: string]: CompanyReport };

export interface FixtureData<T> {
  companyInformation: CompanyInformation;
  t: T;
}

export function generateFixtureDataset<T>(generator: () => T): Array<FixtureData<T>> {
  const fixtureDataset = [];
  for (let id = 1; id <= 250; id++) {
    fixtureDataset.push({
      companyInformation: generateCompanyInformation(),
      t: generator(),
    });
  }
  return fixtureDataset;
}
