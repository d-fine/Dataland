package org.dataland.e2etests

const val BASE_PATH_TO_DATALAND_BACKEND = "https://dataland-local.duckdns.org:443/api"

const val PATH_TO_KEYCLOAK_TOKENENDPOINT =
    "https://dataland-local.duckdns.org:443/keycloak/realms/datalandsecurity/protocol/openid-connect/token"

const val TOKENREQUEST_GRANT_TYPE = "password"
const val TOKENREQUEST_CLIENT_ID = "dataland-public"

const val UPLOADER_USER_NAME = "data_uploader"
val UPLOADER_USER_PASSWORD: String = System.getenv("KEYCLOAK_UPLOADER_PASSWORD")

const val READER_USER_NAME = "data_reader"
val READER_USER_PASSWORD: String = System.getenv("KEYCLOAK_READER_PASSWORD")
