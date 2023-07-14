/**
 * Sorts dates to ensure that Sfdr and LkSG datasets are displayed chronologically in the table in terms of reporting
 * periods (strings starting with numbers should at least be listed before those that do not)
 * @param  listOfDataDateToDisplayAsColumns list of objects to sort
 * @returns []
 */
export function sortReportingPeriodsToDisplayAsColumns(
  listOfDataDateToDisplayAsColumns: ReportingPeriodOfDataSetWithId[]
): ReportingPeriodOfDataSetWithId[] {
  return listOfDataDateToDisplayAsColumns.sort((dataSetA, dataSetB) =>
    compareReportingPeriods(dataSetA.reportingPeriod, dataSetB.reportingPeriod)
  );
}


/**
 * Compares two reporting periods for sorting
 * @param firstReportingPeriod the first reporting period to compare
 * @param secondReportingPeriod the reporting period to compare with
 * @returns 1 if the first reporting period should be sorted after to the second one else -1
 */
export function compareReportingPeriods(firstReportingPeriod: string, secondReportingPeriod: string): number {
  if (!isNaN(Number(firstReportingPeriod)) && !isNaN(Number(secondReportingPeriod))) {
    if (Number(firstReportingPeriod) < Number(secondReportingPeriod)) {
      return 1;
    } else {
      return -1;
    }
  } else if (!isNaN(Number(firstReportingPeriod))) {
    return -1;
  } else if (!isNaN(Number(secondReportingPeriod))) {
    return 1;
  } else {
    if (firstReportingPeriod > secondReportingPeriod) {
      return 1;
    } else {
      return -1;
    }
  }
}

export type ReportingPeriodOfDataSetWithId = {
  dataId: string;
  reportingPeriod: string;
};
