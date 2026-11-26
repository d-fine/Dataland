import {
  type DataTypeEnum,
  type DataMetaInformationForMyDatasets,
  QaStatus,
  type DataMetaInformation,
} from '@clients/backend';

export const ExtendedQaStatus = {
  Accepted: QaStatus.Accepted,
  Rejected: QaStatus.Rejected,
  Pending: QaStatus.Pending,
  Superseded: 'Superseded',
} as const;

export type ExtendedQaStatus = (typeof ExtendedQaStatus)[keyof typeof ExtendedQaStatus];

export class DatasetTableInfo {
  constructor(
    readonly companyName: string,
    readonly dataType: DataTypeEnum,
    readonly uploadTimeInMs: number,
    readonly companyId: string,
    readonly dataId: string,
    readonly dataReportingPeriod: string,
    readonly status: ExtendedQaStatus
  ) {}
}

/**
 * Computes the reduced DatasetStatus of the provided dataset
 * @param dataMetaInfo the dataset containing different status indicators (i.e QaStatus, currentlyActive,...)
 * @returns a unified DatasetStatus
 */
export function getDatasetStatus(
  dataMetaInfo: DataMetaInformationForMyDatasets | DataMetaInformation
): ExtendedQaStatus {
  let qaStatus: QaStatus;
  if ('qualityStatus' in dataMetaInfo) {
    qaStatus = dataMetaInfo.qualityStatus;
  } else {
    qaStatus = dataMetaInfo.qaStatus;
  }

  if (qaStatus == QaStatus.Accepted) {
    return dataMetaInfo.currentlyActive ? ExtendedQaStatus.Accepted : ExtendedQaStatus.Superseded;
  } else if (qaStatus == QaStatus.Rejected) {
    return ExtendedQaStatus.Rejected;
  } else {
    return ExtendedQaStatus.Pending;
  }
}

/**
 * Converts the meta information of an uploaded dataset into an entry of the dataset overview table
 * @param dataMetaInfo the meta information of a dataset uploaded by the current user
 * @returns the corresponding table entry
 */
export function toDatasetTableInfo(dataMetaInfo: DataMetaInformationForMyDatasets): DatasetTableInfo {
  return new DatasetTableInfo(
    dataMetaInfo.companyName,
    dataMetaInfo.dataType,
    dataMetaInfo.uploadTime,
    dataMetaInfo.companyId,
    dataMetaInfo.dataId,
    dataMetaInfo.reportingPeriod,
    getDatasetStatus(dataMetaInfo)
  );
}
