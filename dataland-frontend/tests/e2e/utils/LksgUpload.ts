import { Configuration, LksgData, LksgDataControllerApi } from "@clients/backend";

export async function uploadOneLksgDatasetViaApi(token: string, companyId: string, data: LksgData): Promise<void> {
  await new LksgDataControllerApi(new Configuration({ accessToken: token })).postCompanyAssociatedLksgData({
    companyId,
    data,
  });
}
