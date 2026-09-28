import { type AggregatedFrameworkDataSummary } from '@clients/backend';
import { DataTypeEnum } from '@clients/backend';
import { generateInt } from '@e2e/fixtures/common/NumberFixtures';

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
  for (const frameworkName of Object.values(DataTypeEnum)) {
    mapOfFrameworkNameToAggregatedFrameworkDataSummary[frameworkName] = {
      numberOfProvidedReportingPeriods: generateInt(30),
    };
  }
  return mapOfFrameworkNameToAggregatedFrameworkDataSummary;
}
