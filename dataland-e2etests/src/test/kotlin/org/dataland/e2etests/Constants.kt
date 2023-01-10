package org.dataland.e2etests
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum

const val BASE_PATH_TO_DATALAND_BACKEND = "https://local-dev.dataland.com:443/api"

const val BASE_PATH_TO_API_KEY_MANAGER = "https://local-dev.dataland.com:443/api-keys"

const val PATH_TO_KEYCLOAK_TOKENENDPOINT =
    "https://local-dev.dataland.com:443/keycloak/realms/datalandsecurity/protocol/openid-connect/token"

const val TOKENREQUEST_GRANT_TYPE = "password"
const val TOKENREQUEST_CLIENT_ID = "dataland-public"

const val UPLOADER_USER_NAME = "data_uploader"
const val UPLOADER_USER_ID = "c5ef10b1-de23-4a01-9005-e62ea226ee83"
val UPLOADER_USER_PASSWORD: String = System.getenv("KEYCLOAK_UPLOADER_PASSWORD")
val UPLOADER_EXTENDED_ROLES = listOf("ROLE_UPLOADER")

const val READER_USER_NAME = "data_reader"
const val READER_USER_ID = "18b67ecc-1176-4506-8414-1e81661017ca"
val READER_USER_PASSWORD: String = System.getenv("KEYCLOAK_READER_PASSWORD")

const val ADMIN_USER_NAME = "data_admin"
const val ADMIN_USER_ID = "136a9394-4873-4a61-a25b-65b1e8e7cc2f"
val ADMIN_USER_PASSWORD: String = System.getenv("KEYCLOAK_DATALAND_ADMIN_PASSWORD")
val ADMIN_EXTENDED_ROLES = listOf("ROLE_ADMIN")

val MUTUAL_ROLES_DATALAND_USERS =
    listOf("default-roles-datalandsecurity", "ROLE_USER", "offline_access", "uma_authorization")

val FRONTEND_DISPLAYED_FRAMEWORKS = listOf(
    DataTypeEnum.eutaxonomyMinusFinancials,
    DataTypeEnum.eutaxonomyMinusNonMinusFinancials
)
