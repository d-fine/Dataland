import { Configuration } from "@clients/backend/configuration";
import { DocumentControllerApi } from "@clients/documentmanager";
import { QaControllerApi } from "@clients/qaservice";
import type Keycloak from "keycloak-js";
import { ApiKeyControllerApi } from "@clients/apikeymanager";
import { RequestControllerApi, type RequestControllerApiInterface } from "@clients/communitymanager";
import axios, { type AxiosInstance } from "axios";
import { updateTokenAndItsExpiryTimestampAndStoreBoth } from "@/utils/SessionTimeoutUtils";
import { type FrameworkDataTypes } from "@/utils/api/FrameworkDataTypes";
import { type FrameworkDataApi } from "@/utils/api/UnifiedFrameworkDataApi";
import { getUnifiedFrameworkDataControllerFromConfiguration } from "@/utils/api/FrameworkApiClient";
import * as backendApis from "@clients/backend/api";

interface ApiBackendClients {
  actuator: backendApis.ActuatorApiInterface;
  companyDataController: backendApis.CompanyDataControllerApiInterface;
  metaDataController: backendApis.MetaDataControllerApiInterface;
}

interface ApiClients {
  apiKeyController: ApiKeyControllerApi;
  documentController: DocumentControllerApi;
  requestController: RequestControllerApiInterface;
  qaController: QaControllerApi;
}

type ApiClientConstructor<T> = new (
  configuration: Configuration | undefined,
  basePath: string,
  axios: AxiosInstance,
) => T;
type ApiClientFactory = <T>(constructor: ApiClientConstructor<T>) => T;

export class ApiClientProvider {
  private readonly keycloakPromise: Promise<Keycloak>;
  private readonly axiosInstance: AxiosInstance;

  readonly backendClients: ApiBackendClients;
  readonly apiClients: ApiClients;

  constructor(keycloakPromise: Promise<Keycloak>) {
    this.keycloakPromise = keycloakPromise;
    this.axiosInstance = axios.create({});
    this.registerAutoAuthenticatingAxiosInterceptor();

    this.backendClients = this.constructBackendClients();
    this.apiClients = this.constructApiClients();
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
      metaDataController: backendClientFactory(
        backendApis.MetaDataControllerApi,
      ) as backendApis.MetaDataControllerApiInterface,
    };
  }

  private constructApiClients(): ApiClients {
    return {
      apiKeyController: this.getClientFactory("/api-keys")(ApiKeyControllerApi),
      documentController: this.getClientFactory("/documents")(DocumentControllerApi),
      requestController: this.getClientFactory("/community")(RequestControllerApi),
      qaController: this.getClientFactory("/qa")(QaControllerApi),
    };
  }

  private async getBearerToken(): Promise<string | undefined> {
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
    return getUnifiedFrameworkDataControllerFromConfiguration(framework, configuration, this.axiosInstance);
  }
}
