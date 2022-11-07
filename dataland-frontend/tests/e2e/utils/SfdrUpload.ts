import { Configuration, SfdrData, SfdrDataControllerApi } from "@clients/backend";

export async function uploadOneSfdrDataset(token: string, companyId: string, data: SfdrData): Promise<void> {
  await new SfdrDataControllerApi(new Configuration({ accessToken: token })).postCompanyAssociatedSfdrData({
    companyId,
    data,
  });
}
