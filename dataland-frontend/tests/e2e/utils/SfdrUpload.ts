import {
  CompanyInformation,
  Configuration,
  DataMetaInformation,
  SfdrData,
  SfdrDataControllerApi,
} from "@clients/backend";
import { UploadIds } from "./GeneralApiUtils";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "./CompanyUpload";

/**
 * Uploads a single SFDR data entry for a company
 *
 * @param token The API bearer token to use
 * @param companyId The Id of the company to upload the dataset for
 * @param data The Dataset to upload
 */
export async function uploadOneSfdrDataset(
  token: string,
  companyId: string,
  data: SfdrData
): Promise<DataMetaInformation> {
  const response = await new SfdrDataControllerApi(
    new Configuration({ accessToken: token })
  ).postCompanyAssociatedSfdrData({
    companyId,
    data,
  });
  return response.data;
}

/**
 * Uploads a company and single SFDR data entry for a company
 *
 * @param token The API bearer token to use
 * @param companyInformation The company information to use for the company upload
 * @param testData The Dataset to upload
 * @returns an object which contains the companyId from the company upload and the dataId from the data upload
 */
export function uploadCompanyAndSfdrDataViaApi(
  token: string,
  companyInformation: CompanyInformation,
  testData: SfdrData
): Promise<UploadIds> {
  return uploadCompanyViaApi(token, generateDummyCompanyInformation(companyInformation.companyName)).then(
    (storedCompany) => {
      return uploadOneSfdrDataset(token, storedCompany.companyId, testData).then((dataMetaInformation) => {
        return { companyId: storedCompany.companyId, dataId: dataMetaInformation.dataId };
      });
    }
  );
}
