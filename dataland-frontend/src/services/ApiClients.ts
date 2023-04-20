import { Configuration } from "@clients/backend/configuration";
import {
  CompanyDataControllerApi,
  CompanyDataControllerApiInterface,
  EuTaxonomyDataForFinancialsControllerApi,
  EuTaxonomyDataForFinancialsControllerApiInterface,
  EuTaxonomyDataForNonFinancialsControllerApi,
  EuTaxonomyDataForNonFinancialsControllerApiInterface,
  InviteControllerApi,
  LksgDataControllerApi,
  LksgDataControllerApiInterface,
  MetaDataControllerApi,
  MetaDataControllerApiInterface,
  SfdrDataControllerApi,
  SfdrDataControllerApiInterface,
} from "@clients/backend/api";
import Keycloak from "keycloak-js";
import { ApiKeyControllerApi, ApiKeyControllerApiInterface } from "@clients/apikeymanager";
import { DocumentControllerApi } from "@clients/documentmanager";

export class ApiClientProvider {
  keycloakPromise: Promise<Keycloak>;

  constructor(keycloakPromise: Promise<Keycloak>) {
    this.keycloakPromise = keycloakPromise;
  }

  async getConfiguration(): Promise<Configuration | undefined> {
    const keycloak = await this.keycloakPromise;
    if (keycloak.authenticated) {
      await keycloak.updateToken(5);
      return new Configuration({ accessToken: keycloak.token });
    } else {
      return undefined;
    }
  }

  async getConstructedApi<T>(
    constructor: new (configuration: Configuration | undefined, basePath: string) => T,
    basePath = "/api"
  ): Promise<T> {
    const configuration = await this.getConfiguration();
    return new constructor(configuration, basePath);
  }

  async getConstructedDocumentManager<T>(
    constructor: new (configuration: Configuration | undefined, basePath: string) => T
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

  async getApiKeyManagerController(): Promise<ApiKeyControllerApiInterface> {
    return this.getConstructedApi(ApiKeyControllerApi, "/api-keys");
  }

  async getDocumentControllerApi(): Promise<DocumentControllerApi> {
    return this.getConstructedDocumentManager(DocumentControllerApi);
  }

  async getInviteControllerApi(): Promise<InviteControllerApi> {
    return this.getConstructedApi(InviteControllerApi);
  }
}
