import { Configuration } from "@clients/backend/configuration";
import {
  CompanyDataControllerApi,
  type CompanyDataControllerApiInterface,
  EuTaxonomyDataForNonFinancialsControllerApi,
  type EuTaxonomyDataForNonFinancialsControllerApiInterface,
  EuTaxonomyDataForFinancialsControllerApi,
  type EuTaxonomyDataForFinancialsControllerApiInterface,
  MetaDataControllerApi,
  type MetaDataControllerApiInterface,
  LksgDataControllerApi,
  type LksgDataControllerApiInterface,
  SfdrDataControllerApi,
  type SfdrDataControllerApiInterface,
  type P2pDataControllerApiInterface,
  P2pDataControllerApi,
  type SmeDataControllerApiInterface,
  SmeDataControllerApi,
  InviteControllerApi,
} from "@clients/backend/api";
import { DocumentControllerApi } from "@clients/documentmanager";
import { QaControllerApi } from "@clients/qaservice";
import type Keycloak from "keycloak-js";
import { ApiKeyControllerApi, type ApiKeyControllerApiInterface } from "@clients/apikeymanager";
import { updateTokenAndItsExpiryTimestampAndStoreBoth } from "@/utils/SessionTimeoutUtils";
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
