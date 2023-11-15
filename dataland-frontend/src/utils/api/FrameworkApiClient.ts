import { type FrameworkDataTypes } from "@/utils/api/FrameworkDataTypes";
import { type FrameworkDataApi, translateFrameworkApi } from "@/utils/api/UnifiedFrameworkDataApi";
import {
  type Configuration,
  DataTypeEnum,
  EuTaxonomyDataForFinancialsControllerApi,
  EuTaxonomyDataForNonFinancialsControllerApi,
  LksgDataControllerApi,
  P2pDataControllerApi,
  SfdrDataControllerApi,
  SmeDataControllerApi,
} from "@clients/backend";
import { assertNever } from "@/utils/TypeScriptUtils";

/**
 * Create a Unified Framework Data API client using the provided API client configuration
 * @param framework The identified of the framework
 * @param configuration The API Client configuration
 * @returns the unified API client
 */
export function getUnifiedFrameworkDataControllerFromConfiguration<K extends keyof FrameworkDataTypes>(
  framework: K,
  configuration: Configuration | undefined,
): FrameworkDataApi<FrameworkDataTypes[K]["data"]> {
  switch (framework) {
    case DataTypeEnum.Lksg:
      return translateFrameworkApi<typeof DataTypeEnum.Lksg>("LksgData", new LksgDataControllerApi(configuration));
    case DataTypeEnum.Sfdr:
      return translateFrameworkApi<typeof DataTypeEnum.Sfdr>("SfdrData", new SfdrDataControllerApi(configuration));
    case DataTypeEnum.P2p:
      return translateFrameworkApi<typeof DataTypeEnum.P2p>("P2pData", new P2pDataControllerApi(configuration));
    case DataTypeEnum.Sme:
      return translateFrameworkApi<typeof DataTypeEnum.Sme>("SmeData", new SmeDataControllerApi(configuration));
    case DataTypeEnum.EutaxonomyFinancials:
      return translateFrameworkApi<typeof DataTypeEnum.EutaxonomyFinancials>(
        "EuTaxonomyDataForFinancials",
        new EuTaxonomyDataForFinancialsControllerApi(configuration),
      );
    case DataTypeEnum.EutaxonomyNonFinancials:
      return translateFrameworkApi<typeof DataTypeEnum.EutaxonomyNonFinancials>(
        "EuTaxonomyDataForNonFinancials",
        new EuTaxonomyDataForNonFinancialsControllerApi(configuration),
      );
    default:
      return assertNever(framework);
  }
}
