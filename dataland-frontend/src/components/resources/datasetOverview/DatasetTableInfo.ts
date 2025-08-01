import {
  type DataTypeEnum,
  type DataMetaInformationForMyDatasets,
  QaStatus,
  type DataMetaInformation,
} from '@clients/backend';
import type Keycloak from 'keycloak-js';
import { ApiClientProvider } from '@/services/ApiClients';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { getUserId } from '@/utils/KeycloakUtils';

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
 * Loads the datasets in form of DatasetTableInfos the requesting user is responsible for
 * @param getKeycloakPromise the authorization for backend interaction
 * @returns the filtered DatasetTableInfos
 */
export async function getMyDatasetTableInfos(getKeycloakPromise: () => Promise<Keycloak>): Promise<DatasetTableInfo[]> {
  const userId = await getUserId(getKeycloakPromise);
  const userUploadsControllerApi = new ApiClientProvider(getKeycloakPromise()).backendClients.userUploadsController;
  const storedCompaniesUploadedByCurrentUser = (
    await userUploadsControllerApi.getUserUploadsDataMetaInformation(assertDefined(userId))
  ).data;

  return storedCompaniesUploadedByCurrentUser.map(
    (company: DataMetaInformationForMyDatasets) =>
      new DatasetTableInfo(
        company.companyName,
        company.dataType,
        company.uploadTime,
        company.companyId,
        company.dataId,
        company.reportingPeriod,
        getDatasetStatus(company)
      )
  );
}
