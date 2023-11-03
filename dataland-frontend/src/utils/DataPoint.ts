import { type BaseDocumentReference, type ExtendedDocumentReference, type QualityOptions } from "@clients/backend";

export interface ExtendedDataPoint<T> {
  value?: T | null;
  dataSource?: ExtendedDocumentReference | null;
  quality: QualityOptions;
  comment?: string | null;
}

export interface BaseDataPoint<T> {
  value: T;
  dataSource?: BaseDocumentReference | null;
}

export type DataPointDisplay = {
  value: string;
  quality?: string;
  dataSource?: ExtendedDocumentReference | BaseDocumentReference;
  comment?: string;
};

/**
 * Checks if a potential data point has a value that is neither undefined nor
 * @param dataPoint the potential data point
 * @param dataPoint.value the value of the potential data point
 * @returns the result of the evaluation
 */
export function hasDataPointProperValue(dataPoint: { value: unknown }): boolean {
  return dataPoint.value != undefined && dataPoint.value != "";
}
