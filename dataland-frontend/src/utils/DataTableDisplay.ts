/**
 * Sorts dates to ensure that Sfdr datasets are displayed chronologically in the table
 *
 * @param  listOfDataDateToDisplayAsColumns table of object to sort
 * @returns []
 */
export function sortDatesToDisplayAsColumns(
  listOfDataDateToDisplayAsColumns: { dataId: string; dataDate: string }[]
): { dataId: string; dataDate: string }[] {
  return listOfDataDateToDisplayAsColumns.sort((dateA, dateB) => {
    if (Date.parse(dateA.dataDate) < Date.parse(dateB.dataDate)) {
      return 1;
    } else {
      return -1;
    }
  });
}
