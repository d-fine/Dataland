import {
  CompanyDataControllerApi,
  CompanyInformation,
  Configuration,
  DataTypeEnum,
  StoredCompany,
} from "../../../build/clients/backend";

export async function uploadCompany(token: string, companyInformation: CompanyInformation): Promise<StoredCompany> {
  const data = await new CompanyDataControllerApi(new Configuration({ accessToken: token })).postCompany(
    companyInformation
  );
  return data.data;
}
export async function getCompanyAndDataIds(token: string, dataType: DataTypeEnum): Promise<StoredCompany[]> {
  const dataset = await new CompanyDataControllerApi(new Configuration({ accessToken: token })).getCompanies(
    undefined,
    new Set([dataType])
  );
  return dataset.data;
}

export async function countCompanyAndDataIds(
  token: string,
  dataType: DataTypeEnum
): Promise<{ matchingCompanies: number; matchingDataIds: number }> {
  const dataset = await getCompanyAndDataIds(token, dataType);
  const matchingCompanies = dataset.length;
  let matchingDataIds = 0;
  dataset.forEach((it) => {
    matchingDataIds += it.dataRegisteredByDataland.length;
  });

  return {
    matchingDataIds,
    matchingCompanies,
  };
}
