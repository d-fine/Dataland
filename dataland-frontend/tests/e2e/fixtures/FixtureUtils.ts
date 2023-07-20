import { CompanyReport, DocumentReference } from "@clients/backend";
import { generateCompanyInformation } from "./CompanyFixtures";
import { getRandomReportingPeriod } from "@e2e/fixtures/common/ReportingPeriodFixtures";
import { FixtureData } from "@sharedUtils/Fixtures";
import { faker } from "@faker-js/faker";

export type ReferencedDocuments = { [key: string]: CompanyReport | DocumentReference };

/**
 * Randomly generates a fixture datasets consisting of a specified number of groups of company information datasets, framework datasets and
 * a reporting period
 * @param frameworkDataGenerator a generator that generates a random framework dataset of type T
 * @param numElements the number of elements to generate
 * @param reportingPeriodGenerator a generator that generates a reporting period
 * @returns a list of numElements pairs of randomly generated companies associated to randomly generated framework datasets
 */
export function generateFixtureDataset<T>(
  frameworkDataGenerator: () => T,
  numElements: number,
  reportingPeriodGenerator: (dataSet: T) => string = getRandomReportingPeriod
): Array<FixtureData<T>> {
  const fixtureDataset = [];
  for (let id = 1; id <= numElements; id++) {
    const data = frameworkDataGenerator();
    fixtureDataset.push({
      companyInformation: generateCompanyInformation(),
      t: data,
      reportingPeriod: reportingPeriodGenerator(data),
    });
  }
  return fixtureDataset;
}

/**
 * Generates a array of random length with content
 * @param generator generator for a single entry
 * @param min the minimum number of entries
 * @param max the maximum number of entries
 * @returns the generated array
 */
export function generateArray<T>(generator: () => T, min = 0, max = 5): T[] {
  return Array.from({ length: faker.number.int({ min, max }) }, () => generator());
}

export interface DataPoint<T, Y> {
  label: string;
  value: (x: T) => Y | undefined;
}
