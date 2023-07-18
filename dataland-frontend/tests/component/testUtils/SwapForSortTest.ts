import {
    ReportingPeriodOfDataSetWithId,
    sortReportingPeriodsToDisplayAsColumns
} from "../../../src/utils/DataTableDisplay";

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