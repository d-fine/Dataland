import {Configuration} from "@/../build/clients/backend/configuration"
import {
    CompanyDataControllerApi,
    EuTaxonomyDataControllerApi,
    MetaDataControllerApi,
    SkyminderControllerApi
} from "@/../build/clients/backend/api"
export class ApiClientProvider {

    keycloak_init_promise: any
    keycloak_init: any

    constructor(keycloak_init_promise: any, keycloak_init: any) {
        this.keycloak_init_promise = keycloak_init_promise
        this.keycloak_init = keycloak_init
    }

    async getConfiguration() {
        const keycloak = await this.keycloak_init_promise
        if (keycloak.authenticated) {
            const refreshed = await keycloak.updateToken(5)
            if (refreshed) {
                console.log('Token was successfully refreshed');
            } else {
                console.log('Token is still valid');
            }
            console.log("Using Token")
            return new Configuration({accessToken: keycloak.token})
        } else {
            console.log("Not Using Token")
            return undefined
        }
    }

    async getConstructedApi<T>(constructor: new (configuration: Configuration | undefined, basePath: string) => T) {
        const configuration = await this.getConfiguration()
        return new constructor(configuration, process.env.VUE_APP_BASE_API_URL + "/api")
    }

    async getCompanyDataControllerApi() {
        return this.getConstructedApi(CompanyDataControllerApi)
    }

    async getEuTaxonomyDataControllerApi() {
        return this.getConstructedApi(EuTaxonomyDataControllerApi)
    }

    async getMetaDataControllerApi() {
        return this.getConstructedApi(MetaDataControllerApi)
    }

    async getSkyminderControllerApi() {
        return this.getConstructedApi(SkyminderControllerApi)
    }
}

