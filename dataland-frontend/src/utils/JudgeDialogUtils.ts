import type { CustomFormData, DocumentOption, ParsedSingleDataPoint } from '@/types/JudgeDialogTypes.ts';
import { toSafeDisplayString } from '@/utils/StringFormatter.ts';

export const DEFAULT_CUSTOM_JSON = JSON.stringify(
  { value: null, quality: null, comment: null, dataSource: { fileName: null, page: null } },
  null,
  2
);

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
 * @param json - JSON string to wrap.
 * @returns The wrapped {@link ParsedSingleDataPoint}, or `null` on parse failure.
 */
export function wrapDataPointJson(json: string): ParsedSingleDataPoint | null {
  try {
    const parsed: unknown = JSON.parse(json);
    return parsed !== null && typeof parsed === 'object' ? (parsed as ParsedSingleDataPoint) : { value: parsed };
  } catch {
    return null;
  }
}

/**
 * Converts {@link CustomFormData} into the pretty-printed JSON string expected by the backend.
 * Returns {@link DEFAULT_CUSTOM_JSON} when the resulting data point would be empty.
 *
 * @param formData - The form data to convert.
 * @param selectedDocument - The currently selected document option, used to resolve the data source.
 * @returns A pretty-printed JSON string representing the custom data point.
 */
export function parseFormDataToDataPointJson(
  formData: CustomFormData,
  selectedDocument: DocumentOption | null
): string {
  const { value, quality, comment, pages } = formData;

  const documentDataSource = selectedDocument?.dataSource ?? null;
  let dataSource: ParsedSingleDataPoint['dataSource'] | null;
  if (documentDataSource) {
    dataSource = { ...documentDataSource, ...(pages ? { page: pages } : {}) };
  } else if (pages) {
    dataSource = { page: pages };
  } else {
    dataSource = null;
  }

  const data: ParsedSingleDataPoint = {
    ...(value && { value }),
    ...(quality && { quality }),
    ...(comment && { comment }),
    ...(dataSource && Object.keys(dataSource).length > 0 && { dataSource }),
  };

  return Object.keys(data).length > 0 ? JSON.stringify(data, null, 2) : DEFAULT_CUSTOM_JSON;
}

/**
 * Parses a JSON string representing a data point and maps it into a {@link CustomFormData} object.
 * Returns `null` if the JSON is invalid or cannot be parsed.
 * If the parsed value is a plain primitive (e.g. a plainDate `"2024-01-01"`), it is treated
 * as the `value` field.
 *
 * @param json - JSON string to parse.
 * @returns The mapped {@link CustomFormData}, or `null` on parse failure.
 */
export function parseDataPointJsonToFormData(json: string): CustomFormData | null {
  const detail = wrapDataPointJson(json);
  if (detail === null) return null;
  return transformDataPointDetailToFormData(detail);
}

/**
 * Maps a {@link ParsedSingleDataPoint} object directly into a {@link CustomFormData} object.
 *
 * @param detail - The data point detail to map.
 * @returns The mapped {@link CustomFormData}.
 */
export function transformDataPointDetailToFormData(detail: ParsedSingleDataPoint): CustomFormData {
  return {
    value: toSafeDisplayString(detail.value),
    quality: toSafeDisplayString(detail.quality),
    document: toSafeDisplayString(detail.dataSource?.fileName ?? detail.dataSource?.fileReference),
    pages: toSafeDisplayString(detail.dataSource?.page),
    comment: toSafeDisplayString(detail.comment),
  };
}
