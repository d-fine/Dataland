import { CompanyDataControllerApi, Configuration, DataTypeEnum, StoredCompany } from "@clients/backend";

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
  dataset.forEach((it): void => {
    matchingDataIds += it.dataRegisteredByDataland.length;
  });

  return {
    matchingDataIds,
    matchingCompanies,
  };
}
