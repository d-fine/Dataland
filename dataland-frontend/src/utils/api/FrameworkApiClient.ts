import { type FrameworkDataTypes } from "@/utils/api/FrameworkDataTypes";
import { type PublicFrameworkDataApi, translateFrameworkApi } from "@/utils/api/UnifiedFrameworkDataApi";
import {
  type Configuration,
  DataTypeEnum,
  EuTaxonomyDataForFinancialsControllerApi,
  EutaxonomyNonFinancialsDataControllerApi,
  LksgDataControllerApi,
  P2pDataControllerApi,
  SfdrDataControllerApi,
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
): PublicFrameworkDataApi<FrameworkDataTypes[K]["data"]> {
  /* TODO kann sein dass man jetzt lksg, sfdr und nonfinancials (also alle nicht-toolbox-frameworks) rausnehmen kann

   Antwort by Stephan: Ich glaube nicht, da diese Funktionalität in uploadFrameworkData verwendet wird,
   das müsste vorher geändert werden

   Re-Antwort by Emanuel: Aber alle toolbox-supported Frameworks sollten in der Lage sein, ohne diese Funktion hier
   Sachen hochzuladen. Ähnlich wie auf den upload pages dieser Frameworks, wo sie auch auf diese Funktion hier
   verzichten können.
  */
  switch (framework) {
    case DataTypeEnum.Lksg:
      return translateFrameworkApi<typeof DataTypeEnum.Lksg>(
        "LksgData",
        new LksgDataControllerApi(configuration, undefined, axiosInstance),
      );
    case DataTypeEnum.Sfdr:
      return translateFrameworkApi<typeof DataTypeEnum.Sfdr>(
        "SfdrData",
        new SfdrDataControllerApi(configuration, undefined, axiosInstance),
      );
    case DataTypeEnum.P2p:
      return translateFrameworkApi<typeof DataTypeEnum.P2p>(
        "P2pData",
        new P2pDataControllerApi(configuration, undefined, axiosInstance),
      );
    case DataTypeEnum.EutaxonomyFinancials:
      return translateFrameworkApi<typeof DataTypeEnum.EutaxonomyFinancials>(
        "EuTaxonomyDataForFinancials",
        new EuTaxonomyDataForFinancialsControllerApi(configuration, undefined, axiosInstance),
      );
    case DataTypeEnum.EutaxonomyNonFinancials:
      return translateFrameworkApi<typeof DataTypeEnum.EutaxonomyNonFinancials>(
        "EutaxonomyNonFinancialsData",
        new EutaxonomyNonFinancialsDataControllerApi(configuration, undefined, axiosInstance),
      );
    default:
      return assertNever(framework);
  }
}
