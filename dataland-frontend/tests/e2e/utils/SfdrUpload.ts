import { Configuration, DataMetaInformation, SfdrData, SfdrDataControllerApi } from "@clients/backend";

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
