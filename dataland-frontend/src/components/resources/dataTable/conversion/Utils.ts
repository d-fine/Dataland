import { type DocumentReference, type SfdrData } from "@clients/backend";

/**
 * Retrieves a deeply nested value from an object by an identifier.
 * @param identifier the path to the value to retrieve (dot-seperated)
 * @param dataModel the data object to retrieve the value from
 * @returns the value at the path if it exists, undefined otherwise
 */
// This function is inherently not type-safe, but still required for the data-model conversion.
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function getFieldValueFromDataModel(identifier: string, dataModel: any): any {
  const splits = identifier.split(".");
  // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment,@typescript-eslint/no-explicit-any
  let currentObject: any = dataModel;
  for (const split of splits) {
    if (currentObject === undefined || currentObject === null) return currentObject;
    // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment,@typescript-eslint/no-unsafe-member-access
    currentObject = currentObject[split];
  }
  return currentObject;
}

/**
 * Checks if a given data point has a valid reference set
 * @param dataPoint the datapoint whose reference to check
 * @param dataPoint.dataSource the data source of the data point
 * @returns true if the reference is properly set
 */
export function hasDataPointValidReference(dataPoint: {
  dataSource?: { reference?: string | null; report?: string | null } | null;
}): boolean {
  return (
    (dataPoint.dataSource?.reference != null && dataPoint.dataSource.reference.trim().length > 0) ||
    (dataPoint.dataSource?.report != null && dataPoint.dataSource.report.trim().length > 0)
  );
}

/**
 * Returns the document references globally available in a dataset
 * @param dataset the dataset containing the document references
 * @returns the document references
 */
export function getGloballyReferencableDocuments(dataset: any): DocumentReference[] {
  return Object.entries((dataset as SfdrData)?.general?.general?.referencedReports ?? {}).map(
    (document): DocumentReference => ({ name: document[0], reference: document[1].reference }),
  );
}
