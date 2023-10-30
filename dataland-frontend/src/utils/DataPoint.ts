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
