import { type QaReportDataPointVerdict, type AcceptedDataPointSource } from '@clients/qaservice';

export const DATA_POINT_TYPES = {
  fiscalYearEnd: 'extendedDateFiscalYearEnd',
  greenAssetRatioTotal:
    'extendedCurrencyCreditInstitutionAssetsForCalculationOfGreenAssetRatioTotalGrossCarryingAmount',
  isNfrdMandatory: 'extendedEnumYesNoIsNfrdMandatory',
  numberOfEmployees: 'extendedDecimalNumberOfEmployees',
} as const;

export type DataPointTypeKey = keyof typeof DATA_POINT_TYPES;
export type DataPointType = (typeof DATA_POINT_TYPES)[DataPointTypeKey];

export type QaRole = 'reviewer' | 'admin';

export interface QaReport {
  role: QaRole;
  verdict: QaReportDataPointVerdict;
  correctedValue?: string;
}

export interface QaJudgement {
  acceptedSource?: AcceptedDataPointSource;
  reporterUserIdOfAcceptedQaReport?: string;
  reporterUserNameOfAcceptedQaReport?: string;
  customValue?: string;
}

export interface QaScenarioConfig {
  dataPointType: DataPointType;
  qaReports: QaReport[];
  judgement: QaJudgement;
}

export interface DataPointOverview {
  dataPointsWithQaReports: Record<string, string>;
  dataPointsWithoutQaReports: Record<string, string>;
  amountOfDataPointsToReview: number;
}
