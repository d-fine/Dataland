import {
  Configuration,
  LksgData,
  LksgDataControllerApi,
  DataMetaInformation,
  CompanyInformation,
} from "@clients/backend";
import { UploadIds } from "./GeneralApiUtils";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "./CompanyUpload";

export async function uploadOneLksgDatasetViaApi(
  token: string,
  companyId: string,
  data: LksgData
): Promise<DataMetaInformation> {
  const response = await new LksgDataControllerApi(
    new Configuration({ accessToken: token })
  ).postCompanyAssociatedLksgData({
    companyId,
    data,
  });
  return response.data;
}

export function uploadCompanyAndLksgDataViaApi(
  token: string,
  companyInformation: CompanyInformation,
  testData: LksgData
): Promise<UploadIds> {
  return uploadCompanyViaApi(token, generateDummyCompanyInformation(companyInformation.companyName)).then(
    (storedCompany) => {
      return uploadOneLksgDatasetViaApi(token, storedCompany.companyId, testData).then((dataMetaInformation) => {
        return { companyId: storedCompany.companyId, dataId: dataMetaInformation.dataId };
      });
    }
  );
}
