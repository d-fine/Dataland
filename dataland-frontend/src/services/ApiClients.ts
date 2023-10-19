import { Configuration } from "@clients/backend/configuration";
import { DocumentControllerApi } from "@clients/documentmanager";
import { QaControllerApi } from "@clients/qaservice";
import type Keycloak from "keycloak-js";
import { ApiKeyControllerApi, type ApiKeyControllerApiInterface } from "@clients/apikeymanager";
import axios, { type AxiosInstance } from "axios";
import { updateTokenAndItsExpiryTimestampAndStoreBoth } from "@/utils/SessionTimeoutUtils";
import { type FrameworkDataTypes } from "@/utils/api/FrameworkDataTypes";
import { type FrameworkDataApi } from "@/utils/api/UnifiedFrameworkDataApi";
import { getUnifiedFrameworkDataControllerFromConfiguration } from "@/utils/api/FrameworkApiClient";
import * as backendApis from "@clients/backend/api";

interface ApiBackendClients {
  actuator: backendApis.ActuatorApiInterface;
  inviteController: backendApis.InviteControllerApiInterface;
  companyDataController: backendApis.CompanyDataControllerApiInterface;
  metaDataController: backendApis.MetaDataControllerApiInterface;
  euTaxonomyDataForFinancialsController: backendApis.EuTaxonomyDataForFinancialsControllerApiInterface;
  euTaxonomyDataForNonFinancialsController: backendApis.EuTaxonomyDataForNonFinancialsControllerApiInterface;
  lksgDataController: backendApis.LksgDataControllerApiInterface;
  p2pDataController: backendApis.P2pDataControllerApiInterface;
  sfdrDataController: backendApis.SfdrDataControllerApiInterface;
  smeDataController: backendApis.SmeDataControllerApiInterface;
}

type ApiClientConstructor<T> = new (
  configuration: Configuration | undefined, // TODO why not writing "configuration?: Configuration"
  basePath: string,
  axios: AxiosInstance,
) => T;
type ApiClientFactory = <T>(constructor: ApiClientConstructor<T>) => T;

export class ApiClientProvider {
  private readonly keycloakPromise: Promise<Keycloak>;
  private readonly axiosInstance: AxiosInstance;

  readonly backendClients: ApiBackendClients;

  constructor(keycloakPromise: Promise<Keycloak>) {
    this.keycloakPromise = keycloakPromise;
    this.axiosInstance = axios.create({});
    this.registerAutoAuthenticatingAxiosInterceptor();

    this.backendClients = this.constructBackendClients();
  }

  private registerAutoAuthenticatingAxiosInterceptor(): void {
    this.axiosInstance.interceptors.request.use(
      async (config) => {
        const bearerToken = await this.getBearerToken();
        if (bearerToken) {
          config.headers["Authorization"] = `Bearer ${bearerToken}`;
        }
        return config;
      },
      (error) => Promise.reject(error),
    );
  }

  private constructBackendClients(): ApiBackendClients {
    const backendClientFactory = this.getClientFactory("/api");
    return {
      actuator: backendClientFactory(backendApis.ActuatorApi),
      companyDataController: backendClientFactory(backendApis.CompanyDataControllerApi),
      euTaxonomyDataForFinancialsController: backendClientFactory(backendApis.EuTaxonomyDataForFinancialsControllerApi),
      euTaxonomyDataForNonFinancialsController: backendClientFactory(backendApis.EuTaxonomyDataForNonFinancialsControllerApi),
      inviteController: backendClientFactory(backendApis.InviteControllerApi),
      lksgDataController: backendClientFactory(backendApis.LksgDataControllerApi),
      metaDataController: backendClientFactory(backendApis.MetaDataControllerApi),
      p2pDataController: backendClientFactory(backendApis.P2pDataControllerApi),
      sfdrDataController: backendClientFactory(backendApis.SfdrDataControllerApi),
      smeDataController: backendClientFactory(backendApis.SmeDataControllerApi),
    };
  }

  private async getBearerToken(): Promise<string | undefined> {
    console.log("Obtaining Bearer Token"); // TODO delete at the very end
    const keycloak = await this.keycloakPromise;
    if (keycloak.authenticated) {
      await updateTokenAndItsExpiryTimestampAndStoreBoth(keycloak);
      return keycloak.token;
    } else {
      return undefined;
    }
  }

  private getClientFactory(basePath: string): ApiClientFactory {
    return (constructor) => {
      return new constructor(undefined, basePath, this.axiosInstance);
    };
  }

  async getConfiguration(): Promise<Configuration | undefined> {
    const keycloak = await this.keycloakPromise;
    if (keycloak.authenticated) {
      await updateTokenAndItsExpiryTimestampAndStoreBoth(keycloak);
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

  async getCompanyDataControllerApi(): Promise<backendApis.CompanyDataControllerApiInterface> {
    return this.getConstructedApi(backendApis.CompanyDataControllerApi);
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

  async getMetaDataControllerApi(): Promise<backendApis.MetaDataControllerApiInterface> { //TODO this is a backend controller, why needed?
    return this.getConstructedApi(backendApis.MetaDataControllerApi);
  }

  async getApiKeyManagerController(): Promise<ApiKeyControllerApiInterface> {
    return this.getConstructedApi(ApiKeyControllerApi, "/api-keys");
  }

  async getDocumentControllerApi(): Promise<DocumentControllerApi> {
    return this.getConstructedDocumentManager(DocumentControllerApi); // TODO why only one without the basePath and ApiKeyController?
  }

  async getInviteControllerApi(): Promise<backendApis.InviteControllerApi> { //TODO this is a backend controller, why needed?
    return this.getConstructedApi(backendApis.InviteControllerApi);
  }

  async getQaControllerApi(): Promise<QaControllerApi> {
    return this.getConstructedApi(QaControllerApi, "/qa");
  }
}
