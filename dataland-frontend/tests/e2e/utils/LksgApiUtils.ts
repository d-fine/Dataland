import { Configuration, LksgData, LksgDataControllerApi, DataMetaInformation } from "@clients/backend";

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

export async function getReportingYearOfLksgDataSet(dataId: string, token: string): Promise<string> {
  // check if lksg
  const response = await new LksgDataControllerApi(
    new Configuration({ accessToken: token })
  ).getCompanyAssociatedLksgData(dataId);
  const lksgData = response.data.data;
  const reportingDate = lksgData!.social!.general!.dataDate; // TODO
  if (lksgData) {
    return reportingDate!.split("-").shift()!; // TODO
  } else {
    throw Error(`blub`);
  }
}
