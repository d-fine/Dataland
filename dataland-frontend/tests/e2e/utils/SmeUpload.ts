import { Configuration, DataMetaInformation, SmeData, SmeDataControllerApi } from "@clients/backend";

/**
 * Uploads a single SME data entry for a company
 *
 * @param token The API bearer token to use
 * @param companyId The Id of the company to upload the dataset for
 * @param reportingPeriod The reporting period to use for the upload
 * @param data The Dataset to upload
 * @returns a promise on the created data meta information
 */
export async function uploadOneSmeDataset(
  token: string,
  companyId: string,
  reportingPeriod: string,
  data: SmeData
): Promise<DataMetaInformation> {
  const response = await new SmeDataControllerApi(
    new Configuration({ accessToken: token })
  ).postCompanyAssociatedSmeData({
    companyId,
    reportingPeriod,
    data,
  });
  return response.data;
}
