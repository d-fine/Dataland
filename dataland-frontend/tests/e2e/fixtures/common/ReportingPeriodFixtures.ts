const randomReportingPeriodOptions = ["2019", "2020", "2021", "2022", "2023"];

/**
 * Method to randomly pick one element of the above list of options as reporting period
 * @returns a random reporting period
 */
export function getRandomReportingPeriod(): string {
  const randomIndex = Math.floor(Math.random() * randomReportingPeriodOptions.length);
  return randomReportingPeriodOptions[randomIndex];
}
