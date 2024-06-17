import { pickOneElement } from '@e2e/fixtures/FixtureUtils';

/**
 * Method to randomly pick one element of the above list of options as reporting period
 * @returns a random reporting period
 */
export function generateReportingPeriod(): string {
  return pickOneElement(['2020', '2021', '2022', '2023', '2024']);
}
