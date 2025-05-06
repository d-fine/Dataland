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
