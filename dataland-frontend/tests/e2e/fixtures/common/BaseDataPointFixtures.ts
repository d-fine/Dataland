import { valueOrUndefined } from "@e2e/utils/FakeFixtureUtils";
import { generateReferencedDocuments } from "@e2e/utils/DocumentReference";
import { faker } from "@faker-js/faker";
import { type DocumentReference } from "@clients/backend";

/**
 * Generates a base data point with the given value, choosing a random (possibly undefined) report
 * @param value the type for which the base data point shall be generated
 * @param undefinedProbability the probability with which the referenced report is undefined
 * @returns the generated base data point
 */
export function generateBaseDataPoint<T>(value: T, undefinedProbability: number): GenericBaseDataPoint<T> {
  const documents = generateReferencedDocuments();
  const chosenReport = valueOrUndefined(faker.helpers.arrayElement(Object.values(documents)), undefinedProbability);

  return {
    value: value,
    dataSource: chosenReport,
  } as GenericBaseDataPoint<T>;
}

export interface GenericBaseDataPoint<T> {
  value: T;
  dataSource: DocumentReference | undefined;
}
