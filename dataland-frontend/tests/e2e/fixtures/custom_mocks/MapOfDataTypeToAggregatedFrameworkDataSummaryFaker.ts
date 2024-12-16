import { type AggregatedFrameworkDataSummary } from '@clients/backend';
import { GetAggregatedOpenDataRequestsDataTypesEnum } from '@clients/communitymanager';
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
  Object.values(GetAggregatedOpenDataRequestsDataTypesEnum).forEach((frameworkName) => {
    mapOfFrameworkNameToAggregatedFrameworkDataSummary[frameworkName] = {
      numberOfProvidedReportingPeriods: generateInt(30),
    };
  });
  return mapOfFrameworkNameToAggregatedFrameworkDataSummary;
}
