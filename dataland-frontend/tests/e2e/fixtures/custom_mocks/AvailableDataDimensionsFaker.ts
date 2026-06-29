import { type BasicDataDimensions } from '@clients/backend';
import { AggregatedDataRequestWithAggregatedPriorityDataTypeEnum } from '@clients/communitymanager';
import { generateInt } from '@e2e/fixtures/common/NumberFixtures';

/**
 * Creates a list of BasicDataDimensions entries covering all existing frameworks with a random number of
 * reporting periods each. The dummy company ID and reporting period values are irrelevant for component
 * tests that only check counts — they just need to be distinct strings per period.
 * @returns the list
 */
export function generateAvailableDataDimensions(): BasicDataDimensions[] {
  const dummyCompanyId = '550e8400-e29b-11d4-a716-446655440000';
  const dimensions: BasicDataDimensions[] = [];
  for (const frameworkName of Object.values(AggregatedDataRequestWithAggregatedPriorityDataTypeEnum)) {
    const numberOfPeriods = generateInt(30);
    for (let i = 0; i < numberOfPeriods; i++) {
      dimensions.push({
        companyId: dummyCompanyId,
        dataType: frameworkName,
        reportingPeriod: String(2023 - i),
      });
    }
  }
  return dimensions;
}
