import {Configuration} from "@/../build/clients/backend/configuration"
import {
    CompanyDataControllerApi,
    EuTaxonomyDataControllerApi,
    MetaDataControllerApi,
    SkyminderControllerApi
} from "@/../build/clients/backend/api"

function getConfiguration() {
    const keyCloakToken = window.sessionStorage.getItem('keycloakToken')
    if (keyCloakToken) {
        console.log("Using Token")
        return new Configuration({accessToken: keyCloakToken})
    } else {
        console.log("Not Using Token")
        return undefined
    }
}

function getConstructedApi<T>(constructor: new (configuration: Configuration | undefined, basePath: string) => T): T {
    return new constructor(getConfiguration(), process.env.VUE_APP_BASE_API_URL + "/api")
}

export function getCompanyDataControllerApi() {
    return getConstructedApi(CompanyDataControllerApi)
}

export function getEuTaxonomyDataControllerApi() {
    return getConstructedApi(EuTaxonomyDataControllerApi)
}

export function getMetaDataControllerApi() {
    return getConstructedApi(MetaDataControllerApi)
}

export function getSkyminderControllerApi() {
    return getConstructedApi(SkyminderControllerApi)
}

