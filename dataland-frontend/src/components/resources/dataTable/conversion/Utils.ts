import type { DocumentMetaInfoResponse } from '@clients/documentmanager';

/**
 * Retrieves a deeply nested value from an object by an identifier.
 * @param identifier the path to the value to retrieve (dot-seperated)
 * @param frameworkDataset the data object of some framework to retrieve the value from
 * @returns the value at the path if it exists, undefined otherwise
 */
// This function is inherently not type-safe, but still required for the data-model conversion.
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function getFieldValueFromFrameworkDataset(identifier: string, frameworkDataset: any): any {
  const splits = identifier.split('.');
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  let currentObject: any = frameworkDataset;
  for (const split of splits) {
    if (currentObject === undefined || currentObject === null) return currentObject;

    currentObject = currentObject[split];
  }
  return currentObject;
}

/**
 * Retrieves a deeply nested value from an object by an identifier.
 * @param identifier the path to the value to retrieve (dot-seperated)
 * @param dataModel the data object to retrieve the value from
 * @returns the value at the path if it exists, undefined otherwise
 */
// This function is inherently not type-safe, but still required for the data-model conversion.
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function getFieldValueFromDataModel(identifier: string, dataModel: any): any {
  const splits = identifier.split('.');
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  let currentObject: any = dataModel;
  for (const split of splits) {
    if (currentObject === undefined || currentObject === null) return currentObject;

    currentObject = currentObject[split];
  }
  return currentObject;
}

/**
 * Maps the technical name of a select option to the respective original name
 * @param technicalName of a select option
 * @param mappingObject that contains the mappings
 * @returns original name that matches the technical name
 */
export function getOriginalNameFromTechnicalName<T extends string>(
  technicalName: T,
  mappingObject: { [key in T]: string }
): string {
  return mappingObject[technicalName];
}

export type DataPointObject = {
  value?: number | string | null;
  currency?: string;
  quality?: string;
  comment?: string;
  dataSource?: {
    fileName?: string;
    page?: string;
    tagName?: string;
    fileReference?: string;
    publicationDate?: string;
  };
};

/**
 * Builds the API body for the extended decimal estimated market capitalization data point.
 * Only includes fields that are filled.
 * @returns uploadedDataPoint
 */
export function buildApiBody(
  extendedDataPoint: DataPointObject,
  selectedDocumentMeta: DocumentMetaInfoResponse | undefined,
  companyID: string,
  reportingPeriod: string,
  dataPointTypeId: string,
  currency?: string | undefined,
): {
  dataPoint: string;
  dataPointType: string;
  companyId: string;
  reportingPeriod: string;
} {

  const dataPointObj: DataPointObject = {};
  if (extendedDataPoint.value !== null && extendedDataPoint.value !== undefined) dataPointObj.value = parseValue(extendedDataPoint.value);
  if (currency) dataPointObj.currency = currency;
  if (extendedDataPoint.quality) dataPointObj.quality = extendedDataPoint.quality;
  if (extendedDataPoint.comment) dataPointObj.comment = extendedDataPoint.comment;
  if (extendedDataPoint.dataSource?.fileReference) {
    dataPointObj.dataSource = {
      fileReference: extendedDataPoint.dataSource?.fileReference,
      fileName: selectedDocumentMeta?.documentName,
      page: extendedDataPoint.dataSource?.page ?? undefined,
      publicationDate: selectedDocumentMeta?.publicationDate,
    };
  }

  return {
    dataPoint: JSON.stringify(dataPointObj),
    dataPointType: dataPointTypeId,
    companyId: companyID,
    reportingPeriod: reportingPeriod,
  };
}

/**
 * Parses the input value to a number or null.
 * @param val The input value as string, number, null, or undefined.
 * @returns The parsed number or null if parsing fails.
 */
export function parseValue(val: string | number | null | undefined): number | null {
  if (typeof val === 'number') return val;
  if (typeof val === 'string') {
    const regex = /-?\d+(\.\d+)?/;
    const match = regex.exec(val.replaceAll(',', ''));
    return match ? Number.parseFloat(match[0]) : null;
  }
  return null;
}
