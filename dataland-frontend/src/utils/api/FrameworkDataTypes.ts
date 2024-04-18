import {
  type DataTypeEnum,
  type EuTaxonomyDataForFinancials,
  type EuTaxonomyPublicDataForFinancialsControllerApi,
  type EutaxonomyNonFinancialsData,
  type EutaxonomyNonFinancialsPublicDataControllerApi,
  type LksgData,
  type LksgPublicDataControllerApi,
  type P2pPublicDataControllerApi,
  type PathwaysToParisData,
  type SfdrData,
  type SfdrPublicDataControllerApi,
  //type SmeData,
  //type SmeDataControllerApi,
} from "@clients/backend";

export type FrameworkDataTypes = {
  [DataTypeEnum.P2p]: {
    data: PathwaysToParisData;
    apiSuffix: "P2pData";
    api: P2pPublicDataControllerApi;
  };
  /*[DataTypeEnum.Sme]: {
    data: SmeData;
    apiSuffix: "SmeData";
    api: SmeDataControllerApi;
  };

   */
  [DataTypeEnum.Lksg]: {
    data: LksgData;
    apiSuffix: "LksgData";
    api: LksgPublicDataControllerApi;
  };
  [DataTypeEnum.Sfdr]: {
    data: SfdrData;
    apiSuffix: "SfdrData";
    api: SfdrPublicDataControllerApi;
  };
  [DataTypeEnum.EutaxonomyFinancials]: {
    data: EuTaxonomyDataForFinancials;
    apiSuffix: "EuTaxonomyDataForFinancials";
    api: EuTaxonomyPublicDataForFinancialsControllerApi;
  };
  [DataTypeEnum.EutaxonomyNonFinancials]: {
    data: EutaxonomyNonFinancialsData;
    apiSuffix: "EutaxonomyNonFinancialsData";
    api: EutaxonomyNonFinancialsPublicDataControllerApi;
  };
};
