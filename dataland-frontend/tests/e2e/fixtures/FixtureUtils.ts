import { CompanyInformation, CompanyReport } from "@clients/backend";
import { generateCompanyInformation } from "./CompanyFixtures";

export type ReferencedReports = { [key: string]: CompanyReport };

export interface FixtureData<T> {
  companyInformation: CompanyInformation;
  t: T;
}

/**
 * Randomly generates a fixture datasets consisting of a specified number of pairs of companies and framework datasets
 *
 * @param generator a generator that generates a random framework dataset
 * @param numElements the number of elements to generate
 * @returns a list of numElements pairs of randomly generated companies associated to randomly generated framework datasets
 */
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

export interface DataPoint<T, Y> {
  label: string;
  value: (x: T) => Y | undefined;
}
