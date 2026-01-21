import { ApiKeyControllerApi } from '@clients/apikeymanager';
import * as backendApis from '@clients/backend/api';
import { type Configuration } from '@clients/backend/configuration';
import {
  CompanyRolesControllerApi,
  type CompanyRolesControllerApiInterface,
  EmailAddressControllerApi,
  InheritedRolesControllerApi,
  CompanyRightsControllerApi,
  RequestControllerApi as CommunityManagerRequestControllerApi,
} from '@clients/communitymanager';
import { RequestControllerApi } from '@clients/datasourcingservice';
import { DocumentControllerApi } from '@clients/documentmanager';
import { EmailControllerApi } from '@clients/emailservice';
import { QaControllerApi } from '@clients/qaservice';
import { PortfolioControllerApi } from '@clients/userservice';
import type Keycloak from 'keycloak-js';
import axios, { type AxiosInstance } from 'axios';
import { updateTokenAndItsExpiryTimestampAndStoreBoth } from '@/utils/SessionTimeoutUtils';
import { DataPointControllerApi } from '@clients/backend/api';
import { CreditsControllerApi } from '@clients/accountingservice';
import { InjectionKey } from 'vue';

interface ApiBackendClients {
  actuator: backendApis.ActuatorApiInterface;
  companyDataController: backendApis.CompanyDataControllerApiInterface;
  metaDataController: backendApis.MetaDataControllerApiInterface;
  userUploadsController: backendApis.UserUploadsControllerApiInterface;
}

interface ApiClients {
  apiKeyController: ApiKeyControllerApi;
  documentController: DocumentControllerApi;
  requestController: RequestControllerApi;
  communityManagerRequestController: CommunityManagerRequestControllerApi;
  companyRolesController: CompanyRolesControllerApiInterface;
  inheritedRolesController: InheritedRolesControllerApi;
  qaController: QaControllerApi;
  emailController: EmailControllerApi;
  portfolioController: PortfolioControllerApi;
  emailAddressController: EmailAddressControllerApi;
  dataPointController: DataPointControllerApi;
  companyRightsController: CompanyRightsControllerApi;
  creditsController: CreditsControllerApi;
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
      requestController: this.getClientFactory('/data-sourcing')(RequestControllerApi),
      communityManagerRequestController: this.getClientFactory('/community')(CommunityManagerRequestControllerApi),
      companyRolesController: this.getClientFactory('/community')(CompanyRolesControllerApi),
      inheritedRolesController: this.getClientFactory('/community')(InheritedRolesControllerApi),
      qaController: this.getClientFactory('/qa')(QaControllerApi),
      emailController: this.getClientFactory('/email')(EmailControllerApi),
      portfolioController: this.getClientFactory('/users')(PortfolioControllerApi),
      emailAddressController: this.getClientFactory('/community')(EmailAddressControllerApi),
      dataPointController: this.getClientFactory('/api')(DataPointControllerApi),
      companyRightsController: this.getClientFactory('/community')(CompanyRightsControllerApi),
      creditsController: this.getClientFactory('/accounting')(CreditsControllerApi),
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
