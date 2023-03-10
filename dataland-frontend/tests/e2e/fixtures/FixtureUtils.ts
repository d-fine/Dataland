import { CompanyInformation, CompanyReport, LksgData } from "@clients/backend";
import { generateCompanyInformation } from "./CompanyFixtures";
import { getRandomReportingPeriod } from "@e2e/fixtures/common/ReportingPeriodFixtures";

export type ReferencedReports = { [key: string]: CompanyReport };

export interface FixtureData<T> {
  companyInformation: CompanyInformation;
  t: T;
  reportingPeriod: string;
}

/**
 * Randomly generates a fixture datasets consisting of a specified number of groups of company information datasets, framework datasets and
 * a reporting period
 *
 * @param frameworkDataGenerator a generator that generates a random framework dataset of type T
 * @param numElements the number of elements to generate
 * @returns a list of numElements pairs of randomly generated companies associated to randomly generated framework datasets
 */
export function generateFixtureDataset<T>(frameworkDataGenerator: () => T, numElements: number): Array<FixtureData<T>> {
  const fixtureDataset = [];
  for (let id = 1; id <= numElements; id++) {
    const data = frameworkDataGenerator();
    fixtureDataset.push({
      companyInformation: generateCompanyInformation(),
      t: data,
      reportingPeriod: (data as LksgData)?.social?.general?.dataDate?.substring(0, 4) || getRandomReportingPeriod(),
    });
  }
  return fixtureDataset;
}

export interface DataPoint<T, Y> {
  label: string;
  value: (x: T) => Y | undefined;
}
