import { type FrameworkDataTypes } from "@/utils/api/FrameworkDataTypes";
import { type FrameworkDataApi, translateFrameworkApi } from "@/utils/api/UnifiedFrameworkDataApi";
import {
  type Configuration,
  DataTypeEnum,
  EuTaxonomyPublicDataForFinancialsControllerApi,
  EutaxonomyNonFinancialsPublicDataControllerApi,
  LksgPublicDataControllerApi,
  P2PPublicDataControllerApi,
  SfdrPublicDataControllerApi,
  //SmeDataControllerApi,
} from "@clients/backend";
import { assertNever } from "@/utils/TypeScriptUtils";
import { type AxiosInstance } from "axios";

/**
 * Create a Unified Framework Data API client using the provided API client configuration
 * @param framework The identified of the framework
 * @param configuration The API Client configuration
 * @param axiosInstance an Axios instance
 * @returns the unified API client
 */
export function getUnifiedFrameworkDataControllerFromConfiguration<K extends keyof FrameworkDataTypes>(
  framework: K,
  configuration: Configuration | undefined,
  axiosInstance?: AxiosInstance,
): FrameworkDataApi<FrameworkDataTypes[K]["data"]> {
  switch (framework) {
    case DataTypeEnum.Lksg:
      return translateFrameworkApi<typeof DataTypeEnum.Lksg>(
        "LksgData",
        new LksgPublicDataControllerApi(configuration, undefined, axiosInstance),
      );
    case DataTypeEnum.Sfdr:
      return translateFrameworkApi<typeof DataTypeEnum.Sfdr>(
        "SfdrData",
        new SfdrPublicDataControllerApi(configuration, undefined, axiosInstance),
      );
    case DataTypeEnum.P2p:
      return translateFrameworkApi<typeof DataTypeEnum.P2p>(
        "P2pData",
        new P2PPublicDataControllerApi(configuration, undefined, axiosInstance),
      );
    /*case DataTypeEnum.Sme:
       return translateFrameworkApi<typeof DataTypeEnum.Sme>(
          "SmeData",
          new SmeDataControllerApi(configuration, undefined, axiosInstance),
        );
        */
    case DataTypeEnum.EutaxonomyFinancials:
      return translateFrameworkApi<typeof DataTypeEnum.EutaxonomyFinancials>(
        "EuTaxonomyDataForFinancials",
        new EuTaxonomyPublicDataForFinancialsControllerApi(configuration, undefined, axiosInstance),
      );
    case DataTypeEnum.EutaxonomyNonFinancials:
      return translateFrameworkApi<typeof DataTypeEnum.EutaxonomyNonFinancials>(
        "EutaxonomyNonFinancialsData",
        new EutaxonomyNonFinancialsPublicDataControllerApi(configuration, undefined, axiosInstance),
      );
    default:
      return assertNever(framework);
  }
}
