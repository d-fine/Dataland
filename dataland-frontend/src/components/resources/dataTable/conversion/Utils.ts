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

type DataPointObject = {
  value?: number;
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
  value: number | null,
  chosenQuality: string | null,
  selectedDocument: string | null,
  insertedComment: string | null,
  insertedPage: string | null,
  selectedDocumentMeta: DocumentMetaInfoResponse | null,
  companyID: string,
  reportingPeriod: string,
  currency?: string | null
): {
  dataPoint: string;
  dataPointType: string;
  companyId: string;
  reportingPeriod: string;
} {
  const dataPointObj: DataPointObject = {};
  if (value !== null && value !== undefined) dataPointObj.value = value;
  if (currency) dataPointObj.currency = currency;
  if (chosenQuality) dataPointObj.quality = chosenQuality;
  if (insertedComment) dataPointObj.comment = insertedComment;
  if (selectedDocument) {
    dataPointObj.dataSource = {
      fileReference: selectedDocument,
      fileName: selectedDocumentMeta?.documentName,
      page: insertedPage ?? undefined,
    };
  }

  return {
    dataPoint: JSON.stringify(dataPointObj),
    dataPointType: 'extendedDecimalGhgIntensityInTonnesPerMillionEURRevenue',
    companyId: companyID,
    reportingPeriod: reportingPeriod,
  };
}
