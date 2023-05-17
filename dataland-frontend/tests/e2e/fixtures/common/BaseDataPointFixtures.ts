import { valueOrUndefined } from "@e2e/utils/FakeFixtureUtils";
import { generateReferencedReports } from "@e2e/utils/CompanyReport";

/**
 * Generates a base data point with the given value, choosing a random (possibly undefined) report, or undefined
 * @param value the type for which the base data point shall be generated
 * @param undefinedProbability the probability with which "undefined" is returned
 * @returns the generated base data point
 */
export function generateBaseDataPointOrUndefined<T, Y>(value: T | null, undefinedProbability: number): Y | undefined {
  const reports = generateReferencedReports();
  const chosenReport = valueOrUndefined(
    Object.values(reports)[Math.floor(Math.random() * Object.keys(reports).length)]
  );

  const baseDataPoint = {
    value: value ?? undefined,
    dataSource: chosenReport,
  } as Y;

  return valueOrUndefined(baseDataPoint, undefinedProbability);
}
