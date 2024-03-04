import { DataTypeEnum } from "@clients/backend";

export const frameworkFixtureMap = {
  [DataTypeEnum.EutaxonomyFinancials]: "CompanyInformationWithEuTaxonomyDataForFinancials",
  [DataTypeEnum.Lksg]: "CompanyInformationWithLksgData",
  [DataTypeEnum.P2p]: "CompanyInformationWithP2pData",
};
