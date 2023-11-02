import { type BaseDocumentReference, type ExtendedDocumentReference, type QualityOptions } from "@clients/backend";

export interface GenericDataPoint<T> {
  value?: T | null;
  dataSource?: ExtendedDocumentReference | null;
  quality: QualityOptions;
  comment?: string | null;
  currency?: string | null;
}

export interface GenericBaseDataPoint<T> {
  value: T;
  dataSource?: BaseDocumentReference | null;
}

/**
 * Checks if a potential data point has a value that is neither undefined nor
 * @param dataPoint the potential data point
 * @param dataPoint.value the value of the potential data point
 * @returns the result of the evaluation
 */
export function hasDataPointProperValue(dataPoint: { value: unknown }): boolean {
  return dataPoint.value != undefined && dataPoint.value != "";
}
