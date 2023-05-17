import { valueOrUndefined } from "@e2e/utils/FakeFixtureUtils";
import { generateReferencedDocuments } from "@e2e/utils/DocumentReference";
import { faker } from "@faker-js/faker";

/**
 * Generates a base data point with the given value, choosing a random (possibly undefined) report, or undefined
 * @param value the type for which the base data point shall be generated
 * @param undefinedProbability the probability with which "undefined" is returned
 * @returns the generated base data point
 */
export function generateBaseDataPointOrUndefined<T, Y>(value: T | null, undefinedProbability: number): Y | undefined {
  const documents = generateReferencedDocuments();
  const chosenReport = valueOrUndefined(faker.helpers.arrayElement(Object.values(documents)));

  const baseDataPoint = {
    value: value ?? undefined,
    dataSource: chosenReport,
  } as Y;

  return valueOrUndefined(baseDataPoint, undefinedProbability);
}
