const randomReportingPeriodOptions = ["2019", "2020", "2021", "2022", "2023", "2019-Q3", "2020-Q1", "2021-Q2"];

/**
 * Method to randomly pick one element of the above list of options as reporting period
 */
export function getRandomReportingPeriod(){
    const randomIndex = Math.floor(Math.random() * randomReportingPeriodOptions.length);
    return randomReportingPeriodOptions[randomIndex]
}