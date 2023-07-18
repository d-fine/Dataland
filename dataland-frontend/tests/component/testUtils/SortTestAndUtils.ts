import { ReportingPeriodOfDataSetWithId, sortReportingPeriodsToDisplayAsColumns } from "@/utils/DataTableDisplay";

/**
 * Calls the testfunction for sorting and swaps the list entries if necessary.
 * @param  listOfDataDateToDisplayAsColumns list of objects to sort
 * @param shouldSwap toogles the swap of both list elements in listOfDataDateToDisplayAsColumns (in case there are two.
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
 * @param secondYearObject second object used in sorting test
 * @param firstOtherObject third object used in sorting test
 * @param firstOtherObject fourth object used in sorting test
 * @param firstYearObject.dataId
 * @param firstYearObject.reportingPeriod
 * @param secondYearObject.dataId
 * @param shouldSwapList list of booleans: gives the instructions in the for loop to swap the inputs (saves lines of code).
 * @param secondYearObject.reportingPeriod
 * @param firstOtherObject.dataId
 * @param firstOtherObject.reportingPeriod
 * @param secondOtherObject
 * @param secondOtherObject.dataId
 * @param secondOtherObject.reportingPeriod
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
