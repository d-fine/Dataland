import {
  type DataTypeEnum,
  type EuTaxonomyDataForFinancials,
  type EuTaxonomyDataForFinancialsControllerApi,
  type EuTaxonomyDataForNonFinancials,
  type EuTaxonomyDataForNonFinancialsControllerApi,
  type LksgData,
  type LksgDataControllerApi,
  type P2pDataControllerApi,
  type PathwaysToParisData,
  type SfdrData,
  type SfdrDataControllerApi,
  type SmeData,
  type SmeDataControllerApi,
} from "@clients/backend";

export type FrameworkDataTypes = {
  [DataTypeEnum.P2p]: {
    data: PathwaysToParisData;
    apiSuffix: "P2pData";
    api: P2pDataControllerApi;
  };
  [DataTypeEnum.Sme]: {
    data: SmeData;
    apiSuffix: "SmeData";
    api: SmeDataControllerApi;
  };
  [DataTypeEnum.Lksg]: {
    data: LksgData;
    apiSuffix: "LksgData";
    api: LksgDataControllerApi;
  };
  [DataTypeEnum.Sfdr]: {
    data: SfdrData;
    apiSuffix: "SfdrData";
    api: SfdrDataControllerApi;
  };
  [DataTypeEnum.EutaxonomyFinancials]: {
    data: EuTaxonomyDataForFinancials;
    apiSuffix: "EuTaxonomyDataForFinancials";
    api: EuTaxonomyDataForFinancialsControllerApi;
  };
  [DataTypeEnum.EutaxonomyNonFinancials]: {
    data: EuTaxonomyDataForNonFinancials;
    apiSuffix: "EuTaxonomyDataForNonFinancials";
    api: EuTaxonomyDataForNonFinancialsControllerApi;
  };
};
