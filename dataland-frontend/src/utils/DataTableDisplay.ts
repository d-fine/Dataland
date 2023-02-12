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

/**
 * Function to safe pass link to href attr
 *
 * @param  href link in string to check is it safe href
 * @returns href safe href string
 */
export function sanitizeHref(href: string): string {
  return href.replace(/[^a-zA-Z0-9.:/?#&=_-]+/g, "");
}
