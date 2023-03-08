/**
 * Sorts dates to ensure that Sfdr and LkSG datasets are displayed chronologically in the table in terms of reporting
 * periods (strings starting with numbers should at least be listed before those that do not)
 *
 * @param  listOfDataDateToDisplayAsColumns list of objects to sort
 * @returns []
 */
export function sortReportingPeriodsToDisplayAsColumns(
    listOfDataDateToDisplayAsColumns: { dataId: string; reportingPeriod: string }[]
): { dataId: string; reportingPeriod: string }[] {
  return listOfDataDateToDisplayAsColumns.sort((pairA, pairB) => {
    if ( !isNaN(Number(pairA.reportingPeriod[0])) && !isNaN(Number(pairB.reportingPeriod[0]))) {
      if (pairA.reportingPeriod < pairB.reportingPeriod) {
        return 1;
      } else {
        return -1;
      }
    } else {
      if (pairA.reportingPeriod > pairB.reportingPeriod) {
        return 1;
      } else {
        return -1;
      }
    }

  });
}
