import { Configuration, DataMetaInformation, P2pDataControllerApi, PathwaysToParisData } from "@clients/backend";

/**
 * Uploads a single P2p data entry for a company
 * @param token The API bearer token to use
 * @param companyId The Id of the company to upload the dataset for
 * @param reportingPeriod The reporting period to use for the upload
 * @param data The Dataset to upload
 * @returns a promise on the created data meta information
 */
export async function uploadOneP2pDatasetViaApi(
  token: string,
  companyId: string,
  reportingPeriod: string,
  data: PathwaysToParisData,
): Promise<DataMetaInformation> {
  const response = await new P2pDataControllerApi(
    new Configuration({ accessToken: token }),
  ).postCompanyAssociatedP2pData(
    {
      companyId,
      reportingPeriod,
      data,
    },
    true,
  );
  return response.data;
}
