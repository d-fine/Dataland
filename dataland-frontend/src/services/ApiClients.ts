import { Configuration } from "@/../build/clients/backend/configuration";
import {
  CompanyDataControllerApi,
  EuTaxonomyDataControllerApi,
  MetaDataControllerApi,
  SkyminderControllerApi,
} from "@/../build/clients/backend/api";
import Keycloak from "keycloak-js";
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

  async getCompanyDataControllerApi(): Promise<CompanyDataControllerApi> {
    return this.getConstructedApi(CompanyDataControllerApi);
  }

  async getEuTaxonomyDataControllerApi(): Promise<EuTaxonomyDataControllerApi> {
    return this.getConstructedApi(EuTaxonomyDataControllerApi);
  }

  async getMetaDataControllerApi(): Promise<MetaDataControllerApi> {
    return this.getConstructedApi(MetaDataControllerApi);
  }

  async getSkyminderControllerApi(): Promise<SkyminderControllerApi> {
    return this.getConstructedApi(SkyminderControllerApi);
  }
}
