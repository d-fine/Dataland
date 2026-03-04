import type { DataTypeEnum } from '@clients/backend';

export type ReviewState = 'Pending' | 'Finished' | 'Abandoned';
export type AcceptedSource = 'Original' | 'Qa' | 'Custom';

export type QaReporterCompany = {
  reporterCompanyName: string;
  reporterCompanyId: string;
};

export type QaReportSummary = {
  qaReportId: string;
  verdict: string;
  correctedData: string | null; // raw JSON string as returned by backend
  reporterUserId: string;
  reporterCompanyId: string;
};

export type DataPointReviewInfo = {
  dataPointTypeId: string;
  dataPointId: string;
  qaReports: QaReportSummary[];
  acceptedSource: AcceptedSource;
  companyIdOfAcceptedQaReport: string | null;
  customValue: unknown;
};

export type DatasetReviewOverview = {
  dataSetReviewId: string;
  datasetId: string;
  companyId: string;
  dataType: DataTypeEnum;
  reportingPeriod: string;
  reviewState: ReviewState;
  qaJudgeUserId?: string;
  qaJudgeUserName?: string;

  qaReporterCompanies: QaReporterCompany[];
  dataPoints: Record<string, DataPointReviewInfo>;
};
