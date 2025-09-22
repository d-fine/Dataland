import { type Configuration } from '@clients/backend/configuration';
import { DocumentControllerApi } from '@clients/documentmanager';
import { QaControllerApi } from '@clients/qaservice';
import type Keycloak from 'keycloak-js';
import { ApiKeyControllerApi } from '@clients/apikeymanager';
import {
  CompanyRolesControllerApi,
  type CompanyRolesControllerApiInterface,
  RequestControllerApi,
  type RequestControllerApiInterface,
} from '@clients/communitymanager';
import axios, { type AxiosInstance } from 'axios';
import { updateTokenAndItsExpiryTimestampAndStoreBoth } from '@/utils/SessionTimeoutUtils';
import * as backendApis from '@clients/backend/api';
import { EmailControllerApi } from '@clients/emailservice';
import { PortfolioControllerApi } from '@clients/userservice';
import { EmailAddressControllerApi } from '@clients/communitymanager';

interface ApiBackendClients {
  actuator: backendApis.ActuatorApiInterface;
  companyDataController: backendApis.CompanyDataControllerApiInterface;
  metaDataController: backendApis.MetaDataControllerApiInterface;
  userUploadsController: backendApis.UserUploadsControllerApiInterface;
}

interface ApiClients {
  apiKeyController: ApiKeyControllerApi;
  documentController: DocumentControllerApi;
  requestController: RequestControllerApiInterface;
  companyRolesController: CompanyRolesControllerApiInterface;
  qaController: QaControllerApi;
  emailController: EmailControllerApi;
  portfolioController: PortfolioControllerApi;
  emailAddressController: EmailAddressControllerApi;
}

type ApiClientConstructor<T> = new (
  configuration: Configuration | undefined,
  basePath: string,
  axios: AxiosInstance
) => T;
type ApiClientFactory = <T>(constructor: ApiClientConstructor<T>) => T;

export class ApiClientProvider {
  private readonly keycloakPromise: Promise<Keycloak>;
  readonly axiosInstance: AxiosInstance;

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
          config.headers['Authorization'] = `Bearer ${bearerToken}`;
        }
        return config;
      },
      (error) => {
        return Promise.reject(new Error(error.message ?? 'Unknown error occurred'));
      }
    );
  }

  private constructBackendClients(): ApiBackendClients {
    const backendClientFactory = this.getClientFactory('/api');
    return {
      actuator: backendClientFactory(backendApis.ActuatorApi),
      companyDataController: backendClientFactory(backendApis.CompanyDataControllerApi),
      metaDataController: backendClientFactory(backendApis.MetaDataControllerApi),
      userUploadsController: backendClientFactory(backendApis.UserUploadsControllerApi),
    };
  }

  private constructApiClients(): ApiClients {
    return {
      apiKeyController: this.getClientFactory('/api-keys')(ApiKeyControllerApi),
      documentController: this.getClientFactory('/documents')(DocumentControllerApi),
      requestController: this.getClientFactory('/community')(RequestControllerApi),
      companyRolesController: this.getClientFactory('/community')(CompanyRolesControllerApi),
      qaController: this.getClientFactory('/qa')(QaControllerApi),
      emailController: this.getClientFactory('/email')(EmailControllerApi),
      portfolioController: this.getClientFactory('/users')(PortfolioControllerApi),
      emailAddressController: this.getClientFactory('/community')(EmailAddressControllerApi),
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
}
