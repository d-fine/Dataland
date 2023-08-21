import { type DataMetaInformation, type DataTypeEnum, QaStatus, type StoredCompany } from "@clients/backend";
import type Keycloak from "keycloak-js";
import { ApiClientProvider } from "@/services/ApiClients";
import { ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE } from "@/utils/Constants";

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
export function getDatasetStatus(dataMetaInfo: DataMetaInformation): DatasetStatus {
  if (dataMetaInfo.qaStatus == QaStatus.Accepted) {
    return dataMetaInfo.currentlyActive ? DatasetStatus.QaApproved : DatasetStatus.Superseded;
  } else if (dataMetaInfo.qaStatus == QaStatus.Rejected) {
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
  const companyDataControllerApi = await new ApiClientProvider(getKeycloakPromise()).getCompanyDataControllerApi();
  const storedCompaniesUploadedByCurrentUser = (
    await companyDataControllerApi.getCompanies(
      searchString,
      new Set(ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE),
      undefined,
      undefined,
      undefined,
      true,
    )
  ).data;
  const parsedIdToken = (await getKeycloakPromise()).idTokenParsed;
  if (parsedIdToken) {
    userId = parsedIdToken.sub;
  }

  return storedCompaniesUploadedByCurrentUser.flatMap((company: StoredCompany) =>
    company.dataRegisteredByDataland
      .filter(
        (dataMetaInfo: DataMetaInformation) =>
          dataMetaInfo.uploaderUserId == userId && ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE.includes(dataMetaInfo.dataType),
      )
      .map(
        (dataMetaInfo: DataMetaInformation) =>
          new DatasetTableInfo(
            company.companyInformation.companyName,
            dataMetaInfo.dataType,
            dataMetaInfo.uploadTime,
            company.companyId,
            dataMetaInfo.dataId,
            dataMetaInfo.reportingPeriod,
            getDatasetStatus(dataMetaInfo),
          ),
      ),
  );
}
