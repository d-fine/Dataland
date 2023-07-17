/**
 * Sorts dates to ensure that Sfdr and LkSG datasets are displayed chronologically in the table in terms of reporting
 * periods (strings starting with numbers should at least be listed before those that do not)
 * @param  listOfDataDateToDisplayAsColumns list of objects to sort
 * @param boolSwap toogles the swap of both list elements in listOfDataDateToDisplayAsColumns (in case there are two.
 * Shortens the test-function and avoids code duplications.
 * @returns []
 */
export function sortReportingPeriodsToDisplayAsColumns(
  listOfDataDateToDisplayAsColumns: ReportingPeriodOfDataSetWithId[],
  boolSwap = false
): ReportingPeriodOfDataSetWithId[] {
  let tempList; // : {list: ReportingPeriodOfDataSetWithId[];};
  if (boolSwap && listOfDataDateToDisplayAsColumns.length == 2) {
    tempList = listOfDataDateToDisplayAsColumns.slice();
    tempList[0] = listOfDataDateToDisplayAsColumns[1];
    tempList[1] = listOfDataDateToDisplayAsColumns[0];
    listOfDataDateToDisplayAsColumns = tempList.slice();
  }
  return listOfDataDateToDisplayAsColumns.sort((dataSetA, dataSetB) => {
    if (!isNaN(Number(dataSetA.reportingPeriod[0])) && !isNaN(Number(dataSetB.reportingPeriod[0]))) {
      if (dataSetA.reportingPeriod < dataSetB.reportingPeriod) {
        return 1;
      } else {
        return -1;
      }
    } else {
      if (dataSetA.reportingPeriod > dataSetB.reportingPeriod) {
        return 1;
      } else {
        return -1;
      }
    }
  });
}

export type ReportingPeriodOfDataSetWithId = {
  dataId: string;
  reportingPeriod: string;
};
