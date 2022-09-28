import { CompanyInformation, CompanyReport } from "@clients/backend";
import { generateCompanyInformation } from "./CompanyFixtures";

export type ReferencedReports = { [key: string]: CompanyReport };

export interface FixtureData<T> {
  companyInformation: CompanyInformation;
  t: T;
}

export function generateFixtureDataset<T>(generator: () => T, numElements: number): Array<FixtureData<T>> {
  const fixtureDataset = [];
  for (let id = 1; id <= numElements; id++) {
    fixtureDataset.push({
      companyInformation: generateCompanyInformation(),
      t: generator(),
    });
  }
  return fixtureDataset;
}
