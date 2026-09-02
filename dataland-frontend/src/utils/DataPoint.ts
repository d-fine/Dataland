import { type BaseDocumentReference, type ExtendedDocumentReference, type QualityOptions } from '@clients/backend';
import { type ObjectType } from '@/utils/UpdateObjectUtils';

export interface ExtendedDataPoint<T> {
  value?: T | null;
  dataSource?: ExtendedDocumentReference | null;
  quality?: QualityOptions | null;
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

export interface DataPointSourceInfo {
  fileName?: string | null;
  fileReference?: string | null;
  page?: string | number | null;
  tagName?: string | null;
  publicationDate?: string | null;
  [key: string]: unknown;
}

/**
 * Creates an upload-safe copy by removing fields inferred from document-manager metadata.
 *
 * @param data the dataset model to prepare for upload
 * @returns a copy of the dataset without inferred fields on document references
 */
export function removeInferableDocumentFields<T>(data: T): T {
  const uploadData = JSON.parse(JSON.stringify(data)) as unknown; // NOSONAR: needed for tests to work
  removeInferableDocumentFieldsFromValue(uploadData);
  return uploadData as T;
}

/**
 * Removes inferable fields such as `fileName` and `publicationDate` from the provided value
 * if the value contains a `fileReference` property of type string. This operation
 * is performed recursively for nested objects and arrays.
 *
 * @param {unknown} value - The value from which inferable fields need to be removed.
 *                          Can be an object, array, or any other type.
 *
 * @return {void} Doesn't return a value; modifies the provided object in place.
 */
function removeInferableDocumentFieldsFromValue(value: unknown): void {
  if (Array.isArray(value)) {
    value.forEach(removeInferableDocumentFieldsFromValue);
    return;
  }
  if (!value || typeof value !== 'object') return;

  const objectValue = value as Record<string, unknown>;
  if (typeof objectValue.fileReference === 'string') {
    delete objectValue.fileName;
    delete objectValue.publicationDate;
  }
  Object.values(objectValue).forEach(removeInferableDocumentFieldsFromValue);
}

/**
 * Backfills the inferable document fields (fileName, publicationDate) on every document reference contained in
 * the given dataset, based on the provided fileReference -> report lookup. The backend no longer persists these
 * fields on individual data points, as they are considered "inferable" from the referenced document itself.
 * Without backfilling them for display purposes, upload-form fields would repeatedly try (and fail) to resolve
 * the currently referenced report on every mount, which can cause excessive/recursive re-renders when a dataset
 * has many populated data points.
 *
 * @param data the dataset model as loaded from the backend
 * @param fileReferenceToReport a map from fileReference to the corresponding report name and publication date
 * @returns a copy of the dataset with fileName/publicationDate restored on document references where possible
 */
export function restoreInferableDocumentFields<T>(
  data: T,
  fileReferenceToReport: Map<string, { fileName: string; publicationDate?: string | null }>
): T {
  const restoredData = JSON.parse(JSON.stringify(data)) as unknown; // NOSONAR: needed for tests to work
  restoreInferableDocumentFieldsFromValue(restoredData, fileReferenceToReport);
  return restoredData as T;
}

/**
 * Restores inferable document fields, such as fileName and publicationDate, from a given value object
 * based on the provided fileReferenceToReport map. If the value is an array, it applies the restoration
 * recursively for each element. If the value is an object and contains a fileReference field that has a
 * corresponding entry in the map, it updates the object with any missing inferable fields.
 *
 * @param {unknown} value The object or array to process and restore inferable fields for. Nested structures
 *                        are handled recursively.
 * @param {Map<string, { fileName: string, publicationDate?: string | null }>} fileReferenceToReport A map
 *                        that associates fileReference strings with report objects containing fileName and
 *                        optional publicationDate to be applied to matching objects.
 * @return {void} The function does not return a value. It modifies the input object or array in place.
 */
function restoreInferableDocumentFieldsFromValue(
  value: unknown,
  fileReferenceToReport: Map<string, { fileName: string; publicationDate?: string | null }>
): void {
  if (Array.isArray(value)) {
    value.forEach((entry) => restoreInferableDocumentFieldsFromValue(entry, fileReferenceToReport));
    return;
  }
  if (!value || typeof value !== 'object') return;

  const objectValue = value as Record<string, unknown>;
  if (typeof objectValue.fileReference === 'string') {
    const report = fileReferenceToReport.get(objectValue.fileReference);
    if (report) {
      if (!objectValue.fileName) {
        objectValue.fileName = report.fileName;
      }
      if (!objectValue.publicationDate && report.publicationDate) {
        objectValue.publicationDate = report.publicationDate;
      }
    }
  }
  Object.values(objectValue).forEach((nestedValue) =>
    restoreInferableDocumentFieldsFromValue(nestedValue, fileReferenceToReport)
  );
}

export interface ParsedSingleDataPoint {
  value?: unknown;
  quality?: unknown;
  comment?: unknown;
  dataSource?: DataPointSourceInfo | null;
  [key: string]: unknown;
}

/**
 * Unwraps a data point JSON string for the backend.
 * If the original stored data point (`rawDataPoint`) was a plain primitive
 * (e.g. plainDate stored as `"2024-01-01"`), the custom value is unwrapped
 * to that same primitive format. Otherwise, the value is returned unchanged.
 *
 * @param dataPointJsonString - The custom JSON string to unwrap.
 * @param rawDataPoint - The original stored data point JSON, used to detect plain-primitive types.
 * @returns The unwrapped JSON string, or the original if no unwrapping is needed.
 */
export function unwrapDataPointJson(dataPointJsonString: string, rawDataPoint: string): string {
  try {
    const original = JSON.parse(rawDataPoint);
    if (typeof original !== 'object') {
      const parsed: unknown = JSON.parse(dataPointJsonString);
      const value =
        parsed !== null && typeof parsed === 'object' ? ((parsed as ParsedSingleDataPoint).value ?? null) : parsed;
      return JSON.stringify(value);
    }
  } catch {}
  return dataPointJsonString;
}

/**
 * Wraps a data point JSON string into a {@link ParsedSingleDataPoint} object.
 * This is the inverse of {@link unwrapDataPointJson}: if the stored JSON is a plain
 * primitive (e.g. `"2024-01-01"` for a plainDate), it is wrapped into `{ value: primitive }`
 * so it can be handled uniformly as a {@link ParsedSingleDataPoint}.
 *
 * @param dataPointJsonString - JSON string to wrap.
 * @returns The wrapped {@link ParsedSingleDataPoint}, or `null` on parse failure.
 */
export function wrapDataPointJson(dataPointJsonString: string): ParsedSingleDataPoint | null {
  try {
    const parsed: unknown = JSON.parse(dataPointJsonString);
    return parsed !== null && typeof parsed === 'object' ? (parsed as ParsedSingleDataPoint) : { value: parsed };
  } catch {
    return null;
  }
}

/**
 * Creates a list of the names of all Kpis that have some value
 * @param dataResponseData Data to prefill upload form
 * @returns array of Kpis names that have some value
 */
export function getFilledKpis(dataResponseData: object): string[] {
  const listOfFilledKpis: string[] = [];

  for (const category of Object.values(dataResponseData)) {
    if (!category || typeof category !== 'object') continue;

    for (const subCategory of Object.values(category as ObjectType)) {
      if (!subCategory || typeof subCategory !== 'object') continue;

      for (const [kpi, value] of Object.entries(subCategory)) {
        if (value) {
          listOfFilledKpis.push(kpi);
        }
      }
    }
  }

  return listOfFilledKpis;
}
