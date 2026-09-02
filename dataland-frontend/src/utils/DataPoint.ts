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
 * Creates a deep copy of plain JSON-like data structures (objects/arrays/primitives).
 *
 * @param value value to clone
 * @returns deep copy of the provided value
 */
function cloneDeep<T>(value: T): T {
  if (Array.isArray(value)) {
    return value.map((entry) => cloneDeep(entry)) as T;
  }
  if (value && typeof value === 'object') {
    return Object.fromEntries(
      Object.entries(value as Record<string, unknown>).map(([key, nestedValue]) => [key, cloneDeep(nestedValue)])
    ) as T;
  }
  return value;
}

/**
 * Creates an upload-safe copy by removing fields inferred from document-manager metadata.
 *
 * @param data the dataset model to prepare for upload
 * @returns a copy of the dataset without inferred fields on document references
 */
export function removeInferableDocumentFields<T>(data: T): T {
  const uploadData = cloneDeep(data) as unknown; // NOSONAR: needed for tests to work
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
