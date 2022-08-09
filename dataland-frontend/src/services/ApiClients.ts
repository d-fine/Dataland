import { Configuration } from "@/../build/clients/backend/configuration";
import {
  CompanyDataControllerApi,
  EuTaxonomyDataForNonFinancialsControllerApi,
  EuTaxonomyDataForFinancialsControllerApi,
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
    return new constructor(configuration, `${process.env.VUE_APP_BASE_API_URL}` + `${process.env.VUE_APP_API}`);
  }

  async getCompanyDataControllerApi(): Promise<CompanyDataControllerApi> {
    return this.getConstructedApi(CompanyDataControllerApi);
  }

  async getEuTaxonomyDataForNonFinancialsControllerApi(): Promise<EuTaxonomyDataForNonFinancialsControllerApi> {
    return this.getConstructedApi(EuTaxonomyDataForNonFinancialsControllerApi);
  }

  async getEuTaxonomyDataForFinancialsControllerApi(): Promise<EuTaxonomyDataForFinancialsControllerApi> {
    return this.getConstructedApi(EuTaxonomyDataForFinancialsControllerApi);
  }

  async getMetaDataControllerApi(): Promise<MetaDataControllerApi> {
    return this.getConstructedApi(MetaDataControllerApi);
  }

  async getSkyminderControllerApi(): Promise<SkyminderControllerApi> {
    return this.getConstructedApi(SkyminderControllerApi);
  }
}
