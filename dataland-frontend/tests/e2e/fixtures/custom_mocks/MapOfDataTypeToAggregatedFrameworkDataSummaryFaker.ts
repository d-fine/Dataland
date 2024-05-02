import { type AggregatedFrameworkDataSummary, DataTypeEnum } from "@clients/backend";
import { AggregatedDataRequestDataTypeEnum } from "@clients/communitymanager";
import { generateInt } from "@e2e/fixtures/common/NumberFixtures";

/**
 * Creates a map of all existing frameworks to a corresponding framework data summary
 * @returns the map
 */
export function generateMapOfFrameworkNameToAggregatedFrameworkDataSummary(): Record<
  string,
  AggregatedFrameworkDataSummary
> {
  const mapOfFrameworkNameToAggregatedFrameworkDataSummary: Record<
    string,
    { numberOfProvidedReportingPeriods: number }
  > = {};
  Object.values(AggregatedDataRequestDataTypeEnum)
    .filter((item) => item != DataTypeEnum.Sme)
    .forEach((frameworkName) => {
      mapOfFrameworkNameToAggregatedFrameworkDataSummary[frameworkName] = {
        numberOfProvidedReportingPeriods: generateInt(30),
      };
    });
  return mapOfFrameworkNameToAggregatedFrameworkDataSummary;
}
