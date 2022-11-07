import { Configuration, SmeData, SmeDataControllerApi } from "@clients/backend";

export async function uploadOneSmeDataset(token: string, companyId: string, data: SmeData): Promise<void> {
  await new SmeDataControllerApi(new Configuration({ accessToken: token })).postCompanyAssociatedSmeData({
    companyId,
    data,
  });
}
