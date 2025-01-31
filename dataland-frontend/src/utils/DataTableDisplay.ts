import { type DataAndMetaInformation } from '@/api-models/DataAndMetaInformation';

/**
 * Sorts dates to ensure that Sfdr and LkSG datasets are displayed chronologically in the table in terms of reporting
 * periods (strings starting with numbers should at least be listed before those that do not)
 * @param  listOfDataDateToDisplayAsColumns list of objects to sort
 * Shortens the test-function and avoids code duplications.
 * @returns list of sorted objects
 */
export function sortReportingPeriodsToDisplayAsColumns(
  listOfDataDateToDisplayAsColumns: ReportingPeriodOfDatasetWithId[]
): ReportingPeriodOfDatasetWithId[] {
  return listOfDataDateToDisplayAsColumns.sort((datasetA, datasetB) =>
    compareReportingPeriods(datasetA.reportingPeriod, datasetB.reportingPeriod)
  );
}

/**
 * Sorts a list of datasets - associated by their respective meta info - by comparing their reporting periods.
 * @param listOfDatasets list of datasets associated by their respective meta info
 * @returns the sorted list
 */
export function sortDatasetsByReportingPeriod<T>(
  listOfDatasets: DataAndMetaInformation<T>[]
): DataAndMetaInformation<T>[] {
  return listOfDatasets.sort((datasetA, datasetB) =>
    compareReportingPeriods(datasetA.metaInfo.reportingPeriod, datasetB.metaInfo.reportingPeriod)
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
  } else if (firstReportingPeriod > secondReportingPeriod) {
    return 1;
  } else {
    return -1;
  }
}

export type ReportingPeriodOfDatasetWithId = {
  dataId: string;
  reportingPeriod: string;
};

const buttonRowHeaderId = 'row-header-id';

/**
 * Adds click event listeners on DataTable row headers to expand and collapse row
 * @param dataTableIdentifier id of parent DataTable element from where we are mounting
 * @param expandedRowsOnClick function passes the latest list of expanded row id's
 * @param newExpandedRowsCallback function that returns the updated (after click) list of expanded row id's
 * @returns the map of rows and their click handlers needed for unmounting
 */
export function mountRowHeaderClickEventListeners(
  dataTableIdentifier: string,
  expandedRowsOnClick: () => string[],
  newExpandedRowsCallback: (newExpandedRows: string[]) => void
): Map<Element, EventListener> {
  const handlerMap: Map<Element, EventListener> = new Map();
  let expandedRowGroups: string[] = [];
  const rowHeaders = Array.from(
    document.querySelectorAll(`[data-table-id="${dataTableIdentifier}"][data-row-header-click]`)
  );
  const rowButtons = rowHeaders
    .map((rowHeader: Element) => {
      const button = rowHeader.parentNode?.querySelector('button[data-pc-section="rowgrouptoggler"]');
      if (button) {
        button.setAttribute(buttonRowHeaderId, rowHeader.id);
        return button;
      }
      return void 0;
    })
    .filter((button): button is HTMLButtonElement => !!button);

  [...rowHeaders, ...rowButtons].forEach((el: Element) => {
    const clickHandler: EventListener | null = (evt): void => {
      if (el.getAttribute(buttonRowHeaderId)) {
        evt.stopImmediatePropagation();
        return;
      }
      expandedRowGroups = expandedRowsOnClick();
      if (!expandedRowGroups.includes(el.id)) {
        expandedRowGroups.push(el.id);
      } else {
        expandedRowGroups = expandedRowGroups.filter((id: string) => id !== el.id);
      }
      newExpandedRowsCallback(expandedRowGroups);
    };

    let target;
    if (el.getAttribute(buttonRowHeaderId)) {
      target = el;
    } else {
      target = el.parentNode;
    }
    if (target) {
      handlerMap.set(target as Element, clickHandler);
      target.addEventListener('click', clickHandler);
    }
  });
  return handlerMap;
}

/**
 *
 * @param handlerMap the map of rows and their click handlers that need to be looped and have their event listeners removed
 */
export function unmountRowHeaderClickEventListeners(handlerMap: Map<Element, EventListener>): void {
  const buttonAttributeName = 'data-parent-id';
  handlerMap.forEach((clickHandler: EventListener | null, el: Element) => {
    if (el?.getAttribute(buttonAttributeName)) {
      el.removeEventListener('click', clickHandler as EventListener);
    } else {
      el.parentNode?.removeEventListener('click', clickHandler);
    }
    handlerMap.delete(el);
  });
}
