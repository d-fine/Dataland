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
