import type {
  CustomFormData,
  DataPointDetail,
  DocumentOption,
} from '@/components/resources/datasetReview/JudgeDialogTypes.ts';

export const DEFAULT_CUSTOM_JSON = JSON.stringify(
  { value: null, quality: null, comment: null, dataSource: { fileName: null, page: null } },
  null,
  2
);

/**
 * Converts {@link CustomFormData} into the pretty-printed JSON string expected by the backend.
 * Returns {@link DEFAULT_CUSTOM_JSON} when the resulting datapoint would be empty.
 *
 * @param formData - The form data to convert.
 * @param selectedDocument - The currently selected document option, used to resolve the data source.
 * @returns A pretty-printed JSON string representing the custom datapoint.
 */
export function parseFormDataToDataPointJson(
  formData: CustomFormData,
  selectedDocument: DocumentOption | null
): string {
  const { value, quality, comment, pages } = formData;

  const documentDataSource = selectedDocument?.dataSource ?? null;
  let dataSource: DataPointDetail['dataSource'] | null;
  if (documentDataSource) {
    dataSource = { ...documentDataSource, ...(pages ? { page: pages } : {}) };
  } else if (pages) {
    dataSource = { page: pages };
  } else {
    dataSource = null;
  }

  const data: DataPointDetail = {
    ...(value && { value }),
    ...(quality && { quality }),
    ...(comment && { comment }),
    ...(dataSource && Object.keys(dataSource).length > 0 && { dataSource }),
  };

  return Object.keys(data).length > 0 ? JSON.stringify(data, null, 2) : DEFAULT_CUSTOM_JSON;
}

/**
 * Parses a JSON string representing a datapoint and maps it into a {@link CustomFormData} object.
 * Returns `null` if the JSON is invalid or cannot be parsed.
 *
 * @param json - JSON string to parse.
 * @returns The mapped {@link CustomFormData}, or `null` on parse failure.
 */
export function parseDataPointJsonToFormData(json: string): CustomFormData | null {
  try {
    return transformDataPointDetailToFormData(JSON.parse(json) as DataPointDetail);
  } catch {
    return null;
  }
}

/**
 * Maps a {@link DataPointDetail} object directly into a {@link CustomFormData} object.
 *
 * @param detail - The datapoint detail to map.
 * @returns The mapped {@link CustomFormData}.
 */
export function transformDataPointDetailToFormData(detail: DataPointDetail): CustomFormData {
  return {
    value: toSafeDisplayString(detail.value),
    quality: toSafeDisplayString(detail.quality),
    document: toSafeDisplayString(detail.dataSource?.fileName ?? detail.dataSource?.fileReference),
    pages: toSafeDisplayString(detail.dataSource?.page),
    comment: toSafeDisplayString(detail.comment),
  };
}

/**
 * Safely converts an arbitrary value into a display-friendly string.
 * Objects are JSON-serialized instead of using their default `[object Object]` representation.
 *
 * @param value - Value to convert.
 * @returns A string representation suitable for form fields.
 */
export function toSafeDisplayString(value: unknown): string {
  if (value === null || value === undefined) {
    return '';
  }

  const valueType = typeof value;

  if (valueType === 'string') {
    return value as string;
  }

  if (valueType === 'number') {
    return (value as number).toString();
  }

  if (valueType === 'boolean') {
    return (value as boolean) ? 'true' : 'false';
  }

  if (valueType === 'bigint') {
    return (value as bigint).toString();
  }

  if (valueType === 'symbol') {
    return (value as symbol).toString();
  }

  if (valueType === 'object' || valueType === 'function') {
    try {
      return JSON.stringify(value);
    } catch {
      return '[unserializable value]';
    }
  }

  return '';
}
