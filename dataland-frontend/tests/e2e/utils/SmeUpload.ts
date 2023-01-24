import { Configuration, DataMetaInformation, SmeData, SmeDataControllerApi } from "@clients/backend";

export async function uploadOneSmeDataset(
  token: string,
  companyId: string,
  data: SmeData
): Promise<DataMetaInformation> {
  const response = await new SmeDataControllerApi(
    new Configuration({ accessToken: token })
  ).postCompanyAssociatedSmeData({
    companyId,
    data,
  });
  return response.data;
}
