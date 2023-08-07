import { Configuration } from "@clients/backend/configuration";
import {
  CompanyDataControllerApi,
  CompanyDataControllerApiInterface,
  EuTaxonomyDataForNonFinancialsControllerApi,
  EuTaxonomyDataForNonFinancialsControllerApiInterface,
  EuTaxonomyDataForFinancialsControllerApi,
  EuTaxonomyDataForFinancialsControllerApiInterface,
  MetaDataControllerApi,
  MetaDataControllerApiInterface,
  LksgDataControllerApi,
  LksgDataControllerApiInterface,
  SfdrDataControllerApi,
  SfdrDataControllerApiInterface,
  P2pDataControllerApiInterface,
  P2pDataControllerApi,
  SmeDataControllerApiInterface,
  SmeDataControllerApi,
  InviteControllerApi,
} from "@clients/backend/api";
import { DocumentControllerApi } from "@clients/documentmanager";
import { QaControllerApi } from "@clients/qaservice";
import Keycloak from "keycloak-js";
import axios, { AxiosInstance } from "axios";
import * as backendApis from "@clients/backend/api";
import * as documentApis from "@clients/documentmanager";
import * as qaApis from "@clients/qaservice";
import { ApiKeyControllerApi, ApiKeyControllerApiInterface } from "@clients/apikeymanager";
import { updateTokenAndItsExpiryTimestampAndStoreBoth } from "@/utils/SessionTimeoutUtils";

interface BackendClients {
  actuator: backendApis.ActuatorApiInterface;
  companyDataController: backendApis.CompanyDataControllerApiInterface;
  euTaxonomyDataForFinancialsController: backendApis.EuTaxonomyDataForFinancialsControllerApiInterface;
  euTaxonomyDataForNonFinancialsController: backendApis.EuTaxonomyDataForNonFinancialsControllerApiInterface;
  inviteController: backendApis.InviteControllerApiInterface;
  lksgDataController: backendApis.LksgDataControllerApiInterface;
  metaDataController: backendApis.MetaDataControllerApiInterface;
  p2pDataController: backendApis.P2pDataControllerApiInterface;
  sfdrDataController: backendApis.SfdrDataControllerApiInterface;
  smeDataController: backendApis.SmeDataControllerApiInterface;
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

  readonly backend: BackendClients;

  constructor(keycloakPromise: Promise<Keycloak>) {
    this.keycloakPromise = keycloakPromise;
    this.axiosInstance = axios.create({});
    this.registerAutoAuthenticatingAxiosInterceptor();

    this.backend = this.constructBackendClients();
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

  private constructBackendClients(): BackendClients {
    const factory = this.getClientFactory("/api");
    return {
      actuator: factory(backendApis.ActuatorApi),
      companyDataController: factory(backendApis.CompanyDataControllerApi),
      euTaxonomyDataForFinancialsController: factory(backendApis.EuTaxonomyDataForFinancialsControllerApi),
      euTaxonomyDataForNonFinancialsController: factory(backendApis.EuTaxonomyDataForNonFinancialsControllerApi),
      inviteController: factory(backendApis.InviteControllerApi),
      lksgDataController: factory(backendApis.LksgDataControllerApi),
      metaDataController: factory(backendApis.MetaDataControllerApi),
      p2pDataController: factory(backendApis.P2pDataControllerApi),
      sfdrDataController: factory(backendApis.SfdrDataControllerApi),
      smeDataController: factory(backendApis.SmeDataControllerApi),
    };
  }

  private async getBearerToken(): Promise<string | undefined> {
    console.log("Obtaining Bearer Token");
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

  async getCompanyDataControllerApi(): Promise<CompanyDataControllerApiInterface> {
    return this.getConstructedApi(CompanyDataControllerApi);
  }

  async getEuTaxonomyDataForNonFinancialsControllerApi(): Promise<EuTaxonomyDataForNonFinancialsControllerApiInterface> {
    return this.getConstructedApi(EuTaxonomyDataForNonFinancialsControllerApi);
  }

  async getEuTaxonomyDataForFinancialsControllerApi(): Promise<EuTaxonomyDataForFinancialsControllerApiInterface> {
    return this.getConstructedApi(EuTaxonomyDataForFinancialsControllerApi);
  }

  async getMetaDataControllerApi(): Promise<MetaDataControllerApiInterface> {
    return this.getConstructedApi(MetaDataControllerApi);
  }

  async getLksgDataControllerApi(): Promise<LksgDataControllerApiInterface> {
    return this.getConstructedApi(LksgDataControllerApi);
  }

  async getSfdrDataControllerApi(): Promise<SfdrDataControllerApiInterface> {
    return this.getConstructedApi(SfdrDataControllerApi);
  }

  async getP2pDataControllerApi(): Promise<P2pDataControllerApiInterface> {
    return this.getConstructedApi(P2pDataControllerApi);
  }

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
