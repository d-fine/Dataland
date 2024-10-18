package org.dataland.e2etests

const val BASE_PATH_TO_DATALAND_BACKEND = "https://local-dev.dataland.com:443/api"

const val BASE_PATH_TO_QA_SERVICE = "https://local-dev.dataland.com:443/qa"

const val BASE_PATH_TO_API_KEY_MANAGER = "https://local-dev.dataland.com:443/api-keys"

const val BASE_PATH_TO_DOCUMENT_MANAGER = "https://local-dev.dataland.com:443/documents"

const val BASE_PATH_TO_COMMUNITY_MANAGER = "https://local-dev.dataland.com:443/community"

const val PATH_TO_KEYCLOAK_TOKENENDPOINT =
    "https://local-dev.dataland.com:443/keycloak/realms/datalandsecurity/protocol/openid-connect/token"

const val TOKENREQUEST_GRANT_TYPE = "password"
const val TOKENREQUEST_CLIENT_ID = "dataland-public"

const val REVIEWER_USER_NAME = "data_reviewer"
const val REVIEWER_USER_ID = "f7a02ff1-0dab-4e10-a908-7d775c1014ae"
val REVIEWER_USER_PASSWORD: String = System.getenv("KEYCLOAK_REVIEWER_PASSWORD")
val REVIEWER_EXTENDED_ROLES = listOf("ROLE_REVIEWER")

const val UPLOADER_USER_NAME = "data_uploader"
const val UPLOADER_USER_ID = "c5ef10b1-de23-4a01-9005-e62ea226ee83"
val UPLOADER_USER_PASSWORD: String = System.getenv("KEYCLOAK_UPLOADER_PASSWORD")
val UPLOADER_EXTENDED_ROLES = listOf("ROLE_UPLOADER")

const val PREMIUM_USER_NAME = "data_premium_user"
const val PREMIUM_USER_ID = "68129cce-52e5-473e-bec9-90046eebc619"
val PREMIUM_USER_PASSWORD: String = System.getenv("KEYCLOAK_PREMIUM_USER_PASSWORD")
val PREMIUM_USER_EXTENDED_ROLES = listOf("ROLE_PREMIUM_USER")

const val ADMIN_USER_NAME = "data_admin"
const val ADMIN_USER_ID = "136a9394-4873-4a61-a25b-65b1e8e7cc2f"
val ADMIN_USER_PASSWORD: String = System.getenv("KEYCLOAK_DATALAND_ADMIN_PASSWORD")
val ADMIN_EXTENDED_ROLES = REVIEWER_EXTENDED_ROLES + UPLOADER_EXTENDED_ROLES + PREMIUM_USER_EXTENDED_ROLES

const val READER_USER_NAME = "data_reader"
const val READER_USER_ID = "18b67ecc-1176-4506-8414-1e81661017ca"
val READER_USER_PASSWORD: String = System.getenv("KEYCLOAK_READER_PASSWORD")

val MUTUAL_ROLES_DATALAND_USERS =
    listOf("default-roles-datalandsecurity", "ROLE_USER", "offline_access", "uma_authorization")

private val valueFromEnv = System.getenv("MAX_NUMBER_OF_DAYS_SELECTABLE_FOR_API_KEY_VALIDITY")
val MAX_NUMBER_OF_DAYS_SELECTABLE_FOR_API_KEY_VALIDITY =
    if (valueFromEnv.isNullOrEmpty()) {
        3650
    } else {
        valueFromEnv.toInt()
    }
