import {Configuration} from "@/../build/clients/backend/configuration"

console.log(process.env.VUE_APP_BASE_API_URL )
export const axiosDefaultConfiguration = new Configuration({
    accessToken: () => window.sessionStorage.getItem('keycloakToken') ?? "undefined",
    basePath: process.env.VUE_APP_BASE_API_URL + "/api"

})
console.log(axiosDefaultConfiguration.basePath)