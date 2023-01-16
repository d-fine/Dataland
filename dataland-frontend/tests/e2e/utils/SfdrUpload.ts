import { Configuration, SfdrData, SfdrDataControllerApi } from "@clients/backend";

/**
 * Uploads a single SFDR data entry for a company
 *
 * @param token The API bearer token to use
 * @param companyId The Id of the company to upload the dataset for
 * @param data The Dataset to upload
 */
export async function uploadOneSfdrDataset(token: string, companyId: string, data: SfdrData): Promise<void> {
  await new SfdrDataControllerApi(new Configuration({ accessToken: token })).postCompanyAssociatedSfdrData({
    companyId,
    data,
  });
}
