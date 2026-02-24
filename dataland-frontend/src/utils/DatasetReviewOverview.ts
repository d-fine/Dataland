export type AcceptedSource = 'Original' | 'Qa' | 'Custom';
import type { DataTypeEnum } from '@clients/backend';

export type QaReportSummary = {
  qaReportId: string;
  verdict: string;
  correctedData: string | null; // raw JSON string as returned by backend
};

export type DataPointReviewInfo = {
  dataPointTypeId: string;
  dataPointId: string;
  qaReport: QaReportSummary | null;
  acceptedSource: AcceptedSource;
  customValue: unknown;
};

export type DatasetReviewOverview = {
  datasetReviewId: string;
  datasetId: string;
  companyId: string;
  framework: DataTypeEnum;
  reportingPeriod: string;
  reviewState: 'Pending' | 'Finished' | 'Abandoned';
  reviewerUserId?: string;
  reviewerUserName?: string;
  dataPoints: Record<string, DataPointReviewInfo>;
};
