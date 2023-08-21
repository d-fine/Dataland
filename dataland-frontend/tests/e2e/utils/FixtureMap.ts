import { DataTypeEnum } from "@clients/backend";

export const frameworkFixtureMap = {
  [DataTypeEnum.EutaxonomyFinancials]: "CompanyInformationWithEuTaxonomyDataForFinancials",
  [DataTypeEnum.EutaxonomyNonFinancials]: "CompanyInformationWithEuTaxonomyDataForNonFinancials",
  [DataTypeEnum.Lksg]: "CompanyInformationWithLksgData",
  [DataTypeEnum.P2p]: "CompanyInformationWithP2pData",
  [DataTypeEnum.Sfdr]: "CompanyInformationWithSfdrData",
  [DataTypeEnum.Sme]: "CompanyInformationWithSmeData",
  [DataTypeEnum.NewEutaxonomyNonFinancials]: "CompanyInformationWithNewEuTaxonomyDataForNonFinancials",
};
