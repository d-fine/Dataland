import { ReportingPeriodOfDataSetWithId, sortReportingPeriodsToDisplayAsColumns } from "@/utils/DataTableDisplay";

/**
 * Calls the test function for sorting and swaps the list entries if necessary.
 * @param  listOfDataDateToDisplayAsColumns list of objects to sort
 * @param shouldSwap toggles the swap of both list elements in listOfDataDateToDisplayAsColumns (in case there are two.
 * Shortens the test-function and avoids code duplications.
 * @returns sorted list
 */
export function swapAndSortReportingPeriodsToDisplayAsColumns(
  listOfDataDateToDisplayAsColumns: ReportingPeriodOfDataSetWithId[],
  shouldSwap = false
): ReportingPeriodOfDataSetWithId[] {
  let swappedList: ReportingPeriodOfDataSetWithId[];
  if (shouldSwap && listOfDataDateToDisplayAsColumns.length == 2) {
    swappedList = listOfDataDateToDisplayAsColumns.slice();
    swappedList[0] = listOfDataDateToDisplayAsColumns[1];
    swappedList[1] = listOfDataDateToDisplayAsColumns[0];
    listOfDataDateToDisplayAsColumns = swappedList.slice();
  }
  return sortReportingPeriodsToDisplayAsColumns(listOfDataDateToDisplayAsColumns);
}

/**
 * @param firstYearObject first object used in sorting test
 * @param firstYearObject.dataId data id of the object
 * @param firstYearObject.reportingPeriod reporting period of the object
 * @param secondYearObject second object used in sorting test
 * @param secondYearObject.dataId data id of the object
 * @param secondYearObject.reportingPeriod reporting period of the object
 * @param firstOtherObject third object used in sorting test
 * @param firstOtherObject.dataId data id of the object
 * @param firstOtherObject.reportingPeriod reporting period of the object
 * @param secondOtherObject fourth object used in sorting test
 * @param secondOtherObject.dataId data id of the object
 * @param secondOtherObject.reportingPeriod reporting period of the object
 * @param shouldSwapList list of booleans: gives the instructions in the for loop to swap the inputs (saves lines of code).
 */
export function sortReportingPeriodsToDisplayAsColumnsTest(
  firstYearObject: { dataId: string; reportingPeriod: string },
  secondYearObject: { dataId: string; reportingPeriod: string },
  firstOtherObject: { dataId: string; reportingPeriod: string },
  secondOtherObject: { dataId: string; reportingPeriod: string },
  shouldSwapList: boolean[]
): void {
  for (let i = 0; i < 2; i++) {
    expect(
      swapAndSortReportingPeriodsToDisplayAsColumns([secondYearObject, firstYearObject], shouldSwapList[i])
    ).to.deep.equal([firstYearObject, secondYearObject]);

    expect(
      swapAndSortReportingPeriodsToDisplayAsColumns([secondOtherObject, firstOtherObject], shouldSwapList[i])
    ).to.deep.equal([firstOtherObject, secondOtherObject]);
  }
  expect(sortReportingPeriodsToDisplayAsColumns([firstYearObject, secondOtherObject, firstOtherObject])).to.deep.equal([
    firstYearObject,
    firstOtherObject,
    secondOtherObject,
  ]);
}
