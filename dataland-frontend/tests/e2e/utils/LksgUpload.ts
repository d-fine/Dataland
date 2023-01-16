import { Configuration, LksgData, LksgDataControllerApi } from "@clients/backend";

/**
 * Uploads a single LKSG data entry for a company
 *
 * @param token The API bearer token to use
 * @param companyId The Id of the company to upload the dataset for
 * @param data The Dataset to upload
 */
export async function uploadOneLksgDatasetViaApi(token: string, companyId: string, data: LksgData): Promise<void> {
  await new LksgDataControllerApi(new Configuration({ accessToken: token })).postCompanyAssociatedLksgData({
    companyId,
    data,
  });
}
