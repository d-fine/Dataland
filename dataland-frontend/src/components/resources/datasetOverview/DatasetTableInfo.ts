import { DataMetaInformation, DataTypeEnum, StoredCompany } from "@clients/backend";
import Keycloak from "keycloak-js";
import { ApiClientProvider } from "@/services/ApiClients";
import { ARRAY_OF_FRONTEND_INCLUDED_FRAMEWORKS } from "@/utils/Constants";

export class DatasetTableInfo {
  constructor(
    readonly companyName: string,
    readonly dataType: DataTypeEnum,
    readonly uploadTimeInMs: number,
    readonly companyId: string,
    readonly dataId: string
  ) {}
}

/**
 * Loads the datasets in form of DatasetTableInfos the requesting user is responsible for
 *
 * @param getKeycloakPromise the authorization for backend interaction
 * @param searchString a filter for the company names / alternative names
 * @returns the filtered DatasetTableInfos
 */
export async function getMyDatasetTableInfos(
  getKeycloakPromise: () => Promise<Keycloak>,
  searchString?: string
): Promise<DatasetTableInfo[]> {
  let userId: string | undefined;
  const companyDataControllerApi = await new ApiClientProvider(getKeycloakPromise()).getCompanyDataControllerApi();
  const companiesMetaInfos = (
    await companyDataControllerApi.getCompanies(
      searchString,
      new Set(ARRAY_OF_FRONTEND_INCLUDED_FRAMEWORKS),
      undefined,
      undefined,
      undefined,
      true
    )
  ).data;
  const parsedIdToken = (await getKeycloakPromise()).idTokenParsed;
  if (parsedIdToken) {
    userId = parsedIdToken.sub;
  }
  return companiesMetaInfos.flatMap((company: StoredCompany) =>
    company.dataRegisteredByDataland
      .filter(
        (dataMetaInfo: DataMetaInformation) =>
          dataMetaInfo.uploaderUserId == userId && ARRAY_OF_FRONTEND_INCLUDED_FRAMEWORKS.includes(dataMetaInfo.dataType)
      )
      .map(
        (dataMetaInfo: DataMetaInformation) =>
          new DatasetTableInfo(
            company.companyInformation.companyName,
            dataMetaInfo.dataType,
            dataMetaInfo.uploadTime * 1000,
            company.companyId,
            dataMetaInfo.dataId
          )
      )
  );
}
