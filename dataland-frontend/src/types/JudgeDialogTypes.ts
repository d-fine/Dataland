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

export interface QaReport {
  qaReportId: string;
  verdict: string;
  correctedData: string;
  reporterUserId: string;
}

export interface QaReporter {
  reporterUserId: string;
  reporterUserName?: string | null;
  reporterEmailAddress?: string | null;
}

export interface NextDataPointOption {
  label: string;
  dataPointTypeId: string;
  reviewed: boolean;
}

export interface DocumentOption {
  label: string;
  value: string;
  dataSource: DataPointSourceInfo;
}

export interface CustomFormData {
  value: string;
  quality: string;
  document: string;
  pages: string;
  comment: string;
}
