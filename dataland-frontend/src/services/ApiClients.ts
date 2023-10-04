import { Configuration } from "@clients/backend/configuration";
import {
  CompanyDataControllerApi,
  type CompanyDataControllerApiInterface,
  EuTaxonomyDataForNonFinancialsControllerApi,
  type EuTaxonomyDataForNonFinancialsControllerApiInterface,
  EuTaxonomyDataForFinancialsControllerApi,
  type EuTaxonomyDataForFinancialsControllerApiInterface,
  MetaDataControllerApi,
  type MetaDataControllerApiInterface,
  LksgDataControllerApi,
  type LksgDataControllerApiInterface,
  SfdrDataControllerApi,
  type P2pDataControllerApiInterface,
  P2pDataControllerApi,
  type SmeDataControllerApiInterface,
  SmeDataControllerApi,
  InviteControllerApi,
} from "@clients/backend/api";
import { DocumentControllerApi } from "@clients/documentmanager";
import { QaControllerApi } from "@clients/qaservice";
import type Keycloak from "keycloak-js";
import { ApiKeyControllerApi, type ApiKeyControllerApiInterface } from "@clients/apikeymanager";
import { updateTokenAndItsExpiryTimestampAndStoreBoth } from "@/utils/SessionTimeoutUtils";
import { type FrameworkDataTypes } from "@/utils/api/FrameworkDataTypes";
import { type FrameworkDataApi, translateFrameworkApi } from "@/utils/api/UnifiedFrameworkDataApi";
import { assertNever } from "@/utils/TypeScriptUtils";
import { DataTypeEnum } from "@clients/backend";
export class ApiClientProvider {
  keycloakPromise: Promise<Keycloak>;

  constructor(keycloakPromise: Promise<Keycloak>) {
    this.keycloakPromise = keycloakPromise;
  }

  async getConfiguration(): Promise<Configuration | undefined> {
    const keycloak = await this.keycloakPromise;
    if (keycloak.authenticated) {
      updateTokenAndItsExpiryTimestampAndStoreBoth(keycloak);
      return new Configuration({ accessToken: keycloak.token });
    } else {
      return undefined;
    }
  }

  async getConstructedApi<T>(
    constructor: new (configuration: Configuration | undefined, basePath: string) => T,
    basePath = "/api",
  ): Promise<T> {
    const configuration = await this.getConfiguration();
    return new constructor(configuration, basePath);
  }

  async getConstructedDocumentManager<T>(
    constructor: new (configuration: Configuration | undefined, basePath: string) => T,
  ): Promise<T> {
    const configuration = await this.getConfiguration();
    return new constructor(configuration, "/documents");
  }

  async getCompanyDataControllerApi(): Promise<CompanyDataControllerApiInterface> {
    return this.getConstructedApi(CompanyDataControllerApi);
  }

  /**
   * This function returns a promise to an api controller adaption that is unified across frameworks to allow
   * for creation of generic components that work framework-independent.
   * @param framework The identified of the framework
   * @returns the unified API client
   */
  async getUnifiedFrameworkDataController<K extends keyof FrameworkDataTypes>(
    framework: K,
  ): Promise<FrameworkDataApi<FrameworkDataTypes[K]["data"]>> {
    switch (framework) {
      case DataTypeEnum.Lksg:
        return translateFrameworkApi<typeof DataTypeEnum.Lksg>(
          "LksgData",
          await this.getConstructedApi(LksgDataControllerApi),
        );
      case DataTypeEnum.Sfdr:
        return translateFrameworkApi<typeof DataTypeEnum.Sfdr>(
          "SfdrData",
          await this.getConstructedApi(SfdrDataControllerApi),
        );
      case DataTypeEnum.P2p:
        return translateFrameworkApi<typeof DataTypeEnum.P2p>(
          "P2pData",
          await this.getConstructedApi(P2pDataControllerApi),
        );
      case DataTypeEnum.Sme:
        return translateFrameworkApi<typeof DataTypeEnum.Sme>(
          "SmeData",
          await this.getConstructedApi(SmeDataControllerApi),
        );
      case DataTypeEnum.EutaxonomyFinancials:
        return translateFrameworkApi<typeof DataTypeEnum.EutaxonomyFinancials>(
          "EuTaxonomyDataForFinancials",
          await this.getConstructedApi(EuTaxonomyDataForFinancialsControllerApi),
        );
      case DataTypeEnum.EutaxonomyNonFinancials:
        return translateFrameworkApi<typeof DataTypeEnum.EutaxonomyNonFinancials>(
          "EuTaxonomyDataForNonFinancials",
          await this.getConstructedApi(EuTaxonomyDataForNonFinancialsControllerApi),
        );
      default:
        return assertNever(framework);
    }
  }

  /**
   * @deprecated Please use getUnifiedFrameworkDataController to get framework-specific API controllers.
   * @returns a framework-specific API Controller
   */
  async getEuTaxonomyDataForNonFinancialsControllerApi(): Promise<EuTaxonomyDataForNonFinancialsControllerApiInterface> {
    return this.getConstructedApi(EuTaxonomyDataForNonFinancialsControllerApi);
  }

  /**
   * @deprecated Please use getUnifiedFrameworkDataController to get framework-specific API controllers.
   * @returns a framework-specific API Controller
   */
  async getEuTaxonomyDataForFinancialsControllerApi(): Promise<EuTaxonomyDataForFinancialsControllerApiInterface> {
    return this.getConstructedApi(EuTaxonomyDataForFinancialsControllerApi);
  }

  async getMetaDataControllerApi(): Promise<MetaDataControllerApiInterface> {
    return this.getConstructedApi(MetaDataControllerApi);
  }

  /**
   * @deprecated Please use getUnifiedFrameworkDataController to get framework-specific API controllers.
   * @returns a framework-specific API Controller
   */
  async getLksgDataControllerApi(): Promise<LksgDataControllerApiInterface> {
    return this.getConstructedApi(LksgDataControllerApi);
  }

  /**
   * @deprecated Please use getUnifiedFrameworkDataController to get framework-specific API controllers.
   * @returns a framework-specific API Controller
   */
  async getP2pDataControllerApi(): Promise<P2pDataControllerApiInterface> {
    return this.getConstructedApi(P2pDataControllerApi);
  }

  /**
   * @deprecated Please use getUnifiedFrameworkDataController to get framework-specific API controllers.
   * @returns a framework-specific API Controller
   */
  async getSmeDataControllerApi(): Promise<SmeDataControllerApiInterface> {
    return this.getConstructedApi(SmeDataControllerApi);
  }

  async getApiKeyManagerController(): Promise<ApiKeyControllerApiInterface> {
    return this.getConstructedApi(ApiKeyControllerApi, "/api-keys");
  }

  async getDocumentControllerApi(): Promise<DocumentControllerApi> {
    return this.getConstructedDocumentManager(DocumentControllerApi);
  }

  async getInviteControllerApi(): Promise<InviteControllerApi> {
    return this.getConstructedApi(InviteControllerApi);
  }

  async getQaControllerApi(): Promise<QaControllerApi> {
    return this.getConstructedApi(QaControllerApi, "/qa");
  }
}
