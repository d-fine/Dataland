package org.dataland.e2etests

const val BASE_PATH_TO_DATALAND_PROXY = "http://proxy:80/api"

const val PATH_TO_KEYCLOAK_TOKENENDPOINT =
    "http://proxy:80/keycloak/realms/datalandsecurity/protocol/openid-connect/token"

const val TOKENREQUEST_GRANT_TYPE = "password"
const val TOKENREQUEST_CLIENT_ID = "dataland-public"

const val ADMIN_USER_NAME = "admin_user"
const val ADMIN_USER_PASSWORD = "test"

const val SOME_USER_NAME = "some_user"
const val SOME_USER_PASSWORD = "test"
