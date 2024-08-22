import { type CompanyReport, type BaseDocumentReference } from '@clients/backend';
import { generateCompanyInformation } from './CompanyFixtures';
import { generateReportingPeriod } from '@e2e/fixtures/common/ReportingPeriodFixtures';
import { type FixtureData } from '@sharedUtils/Fixtures';
import { faker } from '@faker-js/faker';

export type ReferencedDocuments = { [key: string]: CompanyReport | BaseDocumentReference };

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
  reportingPeriodGenerator: (dataSet: T) => string = generateReportingPeriod
): Array<FixtureData<T>> {
  const fixtureDataset = [];
  for (let id = 1; id <= numElements; id++) {
    const data = frameworkDataGenerator();
    fixtureDataset.push({
      companyInformation: generateCompanyInformation(),
      t: data,
      reportingPeriod: reportingPeriodGenerator(data),
    });
    if (id > 1) {
      fixtureDataset[id - 1].companyInformation.parentCompanyLei =
        fixtureDataset[id - 2].companyInformation.identifiers['Lei']?.[0] ?? null;
    }
  }
  return fixtureDataset;
}

/**
 * Generates an array of random length with content
 * @param generator generator for a single entry
 * @param min the minimum number of entries
 * @param max the maximum number of entries
 * @param nullProbabilityInGenerator the probability of "null" values to use inside the generator
 * @returns the generated array
 */
export function generateArray<T>(
  generator: (nullProbabilityInGenerator?: number) => T,
  min = 0,
  max = 5,
  nullProbabilityInGenerator?: number
): T[] {
  return Array.from({ length: faker.number.int({ min, max }) }, () => generator(nullProbabilityInGenerator));
}

/**
 * Picks one element of a given array and returns it
 * @param inputArray the array containing all available values to choose from
 * @returns a single element of the given array
 */
export function pickOneElement<T>(inputArray: T[]): T {
  return faker.helpers.arrayElement(inputArray);
}

/**
 * Picks one or no element of a given array and returns it
 * @param inputArray the array containing all available values to choose from
 * @returns an empty array or an array containing single element of the given array
 */
export function pickOneOrNoElement<T>(inputArray: T[]): T[] {
  return pickSubsetOfElements(inputArray, 0, 1);
}

/**
 * Picks a subset of elements from the given array and returns it
 * @param inputArray the array containing all available values to choose from
 * @param min the minimal number of elements returned
 * @param max the maximal number of elements returned
 * @returns an array containing a subset of elements from the given array
 */
export function pickSubsetOfElements<T>(inputArray: T[], min = 1, max = inputArray.length): T[] {
  return faker.helpers.arrayElements(inputArray, { min: min, max: max });
}
