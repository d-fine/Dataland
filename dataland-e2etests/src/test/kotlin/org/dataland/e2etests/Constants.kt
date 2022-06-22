package org.dataland.e2etests

const val BASE_PATH_TO_DATALAND_PROXY = "http://proxy:80/api"

const val PATH_TO_KEYCLOAK_TOKENENDPOINT =
    "http://proxy:80/keycloak/realms/datalandsecurity/protocol/openid-connect/token"

const val TOKENREQUEST_GRANT_TYPE = "password"
const val TOKENREQUEST_CLIENT_ID = "dataland-public"

const val ADMIN_USER_NAME = "data_uploader"
val ADMIN_USER_PASSWORD: String = System.getenv("KEYCLOAK_UPLOADER_PASSWORD")

const val SOME_USER_NAME = "data_reader"
val SOME_USER_PASSWORD: String = System.getenv("KEYCLOAK_READER_PASSWORD")
