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
} from "@clients/backend/api";
import Keycloak from "keycloak-js";
import { ApiKeyControllerApi, ApiKeyControllerApiInterface } from "@clients/apikeymanager";
export class ApiClientProvider {
  keycloakPromise: Promise<Keycloak>;

  constructor(keycloakPromise: Promise<Keycloak>) {
    this.keycloakPromise = keycloakPromise;
  }

  async getConfiguration(): Promise<Configuration | undefined> {
    const keycloak = await this.keycloakPromise;
    if (keycloak.authenticated) {
      const refreshed = await keycloak.updateToken(5);
      if (refreshed) {
        console.log("Token was successfully refreshed");
      } else {
        console.log("Token is still valid");
      }
      console.log("Using Token");
      return new Configuration({ accessToken: keycloak.token });
    } else {
      console.log("Not Using Token");
      return undefined;
    }
  }

  async getConstructedApi<T>(
    constructor: new (configuration: Configuration | undefined, basePath: string) => T
  ): Promise<T> {
    const configuration = await this.getConfiguration();
    return new constructor(configuration, "/api");
  }

  async getConstructedApiKeyManager<T>(
    constructor: new (configuration: Configuration | undefined, basePath: string) => T
  ): Promise<T> {
    const configuration = await this.getConfiguration();
    return new constructor(configuration, "/api-keys");
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

  async getApiKeyManagerController(): Promise<ApiKeyControllerApiInterface> {
    return this.getConstructedApiKeyManager(ApiKeyControllerApi);
  }
}
