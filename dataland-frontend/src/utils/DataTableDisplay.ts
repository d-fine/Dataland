/**
 * Sorts dates to ensure that Sfdr and LkSG datasets are displayed chronologically in the table in terms of reporting
 * periods (strings starting with numbers should at least be listed before those that do not)
 * @param  listOfDataDateToDisplayAsColumns list of objects to sort
 * Shortens the test-function and avoids code duplications.
 * @returns list of sorted objects
 */
export function sortReportingPeriodsToDisplayAsColumns(
  listOfDataDateToDisplayAsColumns: ReportingPeriodOfDataSetWithId[],
): ReportingPeriodOfDataSetWithId[] {
  return listOfDataDateToDisplayAsColumns.sort((dataSetA, dataSetB) =>
    compareReportingPeriods(dataSetA.reportingPeriod, dataSetB.reportingPeriod),
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

/**
 * Enables group row expansion (and collapse) on DataTable when clicking on the whole header row
 * @param event a click event
 * @param referenceData array of items to be checked
 * @param targetKey the name of the key/property of the item to be checked
 * @param expandedRowGroups an array of currently expaned rows
 * @returns an updated array of expanded rows
 */
export function expandRowGroupOnHeaderClick<T>(
  event: Event,
  referenceData: Array<T>,
  targetKey: keyof T,
  expandedRowGroups: string[] = [],
): string[] {
  const id = (event.target as Element).id;

  const matchingChild = Array.from((event.target as Element).children).filter((child: Element) =>
    referenceData.some((dataObject) => dataObject[targetKey] === child.id),
  )[0];

  if (matchingChild || referenceData.some((dataObject) => dataObject[targetKey] === id)) {
    const index = expandedRowGroups.indexOf(matchingChild?.id ?? id);
    if (index === -1) expandedRowGroups.push(matchingChild?.id ?? id);
    else expandedRowGroups.splice(index, 1);
  }
  return expandedRowGroups;
}
