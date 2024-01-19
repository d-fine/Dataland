import {
  type DataTypeEnum,
  type DataMetaInformationForMyDatasets,
  QaStatus
} from "@clients/backend";
import type Keycloak from "keycloak-js";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";

export enum DatasetStatus {
  QaPending,
  QaApproved,
  QaRejected,
  Superseded,
}

export class DatasetTableInfo {
  constructor(
    readonly companyName: string,
    readonly dataType: DataTypeEnum,
    readonly uploadTimeInMs: number,
    readonly companyId: string,
    readonly dataId: string,
    readonly dataReportingPeriod: string,
    readonly status: DatasetStatus,
  ) {}
}

/**
 * Computes the reduced DatasetStatus of the provided dataset
 * @param dataMetaInfo the dataset containing different status indicators (i.e QaStatus, currentlyActive,...)
 * @returns a unified DatasetStatus
 */
export function getDatasetStatus(dataMetaInfo: DataMetaInformationForMyDatasets): DatasetStatus {
  if (dataMetaInfo.qualityStatus == QaStatus.Accepted) {
    return dataMetaInfo.currentlyActive ? DatasetStatus.QaApproved : DatasetStatus.Superseded;
  } else if (dataMetaInfo.qualityStatus == QaStatus.Rejected) {
    return DatasetStatus.QaRejected;
  } else {
    return DatasetStatus.QaPending;
  }
}

/**
 * Loads the datasets in form of DatasetTableInfos the requesting user is responsible for
 * @param getKeycloakPromise the authorization for backend interaction
 * @param searchString a filter for the company names / alternative names
 * @returns the filtered DatasetTableInfos
 */
export async function getMyDatasetTableInfos(
  getKeycloakPromise: () => Promise<Keycloak>,
  searchString?: string,
): Promise<DatasetTableInfo[]> {
  let userId: string | undefined;
  const parsedIdToken = (await getKeycloakPromise()).idTokenParsed;
  if (parsedIdToken) {
    userId = parsedIdToken.sub;
  }
  const userControllerApi = new ApiClientProvider(getKeycloakPromise()).backendClients.userController;
  const storedCompaniesUploadedByCurrentUser = (
    await userControllerApi.getUserDataMetaInformation(assertDefined(userId))
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
        getDatasetStatus(company),
      ),
  );
}
