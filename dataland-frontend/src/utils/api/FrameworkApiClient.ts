import { type FrameworkDataTypes } from "@/utils/api/FrameworkDataTypes";
import { type FrameworkDataApi, translateFrameworkApi } from "@/utils/api/UnifiedFrameworkDataApi";
import {
  type Configuration,
  DataTypeEnum,
  EuTaxonomyDataForFinancialsControllerApi,
  P2pDataControllerApi,
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
    default:
      return assertNever(framework);
  }
}
