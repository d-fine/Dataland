import type { CustomFormData, DocumentOption } from '@/types/JudgeDialogTypes.ts';
import { toSafeDisplayString } from '@/utils/StringFormatter.ts';
import { type ParsedSingleDataPoint, wrapDataPointJson } from '@/utils/DataPoint.ts';

export const DEFAULT_CUSTOM_JSON = JSON.stringify(
  { value: null, quality: null, comment: null, dataSource: { fileName: null, page: null } },
  null,
  2
);

export const DEFAULT_CUSTOM_FORM_DATA: CustomFormData = {
  value: '',
  quality: '',
  document: '',
  pages: '',
  comment: '',
};

type JudgementErrorItem = {
  errorType?: string;
  summary?: string;
  message?: string;
  httpStatus?: number;
};

export type JudgementErrorResponse = {
  errors?: JudgementErrorItem[];
};

/**
 * Returns whether the value should be included in the custom data point payload.
 * Empty or whitespace-only strings are excluded; all other non-null values are included.
 *
 * @param value - The value to check.
 * @returns True if the value should be included in the resulting payload.
 */
export function hasValueContent(value: unknown): boolean {
  if (value === null || value === undefined) {
    return false;
  }
  if (typeof value === 'string') {
    return value.trim().length > 0;
  }
  return true;
}

/**
 * Converts {@link CustomFormData} into the pretty-printed JSON string expected by the backend.
 * Returns {@link DEFAULT_CUSTOM_JSON} when the resulting data point would be empty.
 *
 * Non-string `value`s (e.g. objects or arrays copied over from Activity-type data points) are
 * embedded as-is so that the returned JSON is only ever stringified once, avoiding double
 * JSON-encoding of nested structures.
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
    ...(hasValueContent(value) ? { value } : {}),
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
 * Object and array values are preserved to avoid double serialization.
 *
 * @param detail - The data point detail to map.
 * @returns The mapped {@link CustomFormData}.
 */
export function transformDataPointDetailToFormData(detail: ParsedSingleDataPoint): CustomFormData {
  return {
    value: detail.value !== null && typeof detail.value === 'object' ? detail.value : toSafeDisplayString(detail.value),
    quality: toSafeDisplayString(detail.quality),
    document: toSafeDisplayString(detail.dataSource?.fileName ?? detail.dataSource?.fileReference),
    pages: toSafeDisplayString(detail.dataSource?.page),
    comment: toSafeDisplayString(detail.comment),
  };
}
