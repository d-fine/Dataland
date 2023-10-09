import { Configuration } from "@clients/backend/configuration";
import {
  CompanyDataControllerApi,
  type CompanyDataControllerApiInterface,
  MetaDataControllerApi,
  type MetaDataControllerApiInterface,
  InviteControllerApi,
} from "@clients/backend/api";
import { DocumentControllerApi } from "@clients/documentmanager";
import { QaControllerApi } from "@clients/qaservice";
import type Keycloak from "keycloak-js";
import { ApiKeyControllerApi, type ApiKeyControllerApiInterface } from "@clients/apikeymanager";
import { updateTokenAndItsExpiryTimestampAndStoreBoth } from "@/utils/SessionTimeoutUtils";
import { type FrameworkDataTypes } from "@/utils/api/FrameworkDataTypes";
import { type FrameworkDataApi } from "@/utils/api/UnifiedFrameworkDataApi";
import { getUnifiedFrameworkDataControllerFromConfiguration } from "@/utils/api/FrameworkApiClient";
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
    const configuration = await this.getConfiguration();
    return getUnifiedFrameworkDataControllerFromConfiguration(framework, configuration);
  }

  async getMetaDataControllerApi(): Promise<MetaDataControllerApiInterface> {
    return this.getConstructedApi(MetaDataControllerApi);
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
