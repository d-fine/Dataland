package org.dataland.e2etests.auth

import org.dataland.e2etests.ADMIN_EXTENDED_ROLES
import org.dataland.e2etests.ADMIN_USER_ID
import org.dataland.e2etests.MUTUAL_ROLES_DATALAND_USERS
import org.dataland.e2etests.READER_USER_ID
import org.dataland.e2etests.UPLOADER_EXTENDED_ROLES
import org.dataland.e2etests.UPLOADER_USER_ID

enum class TechnicalUser(val technicalUserId: String, val roles: List<String>) {
    Admin(ADMIN_USER_ID, MUTUAL_ROLES_DATALAND_USERS + ADMIN_EXTENDED_ROLES),
    Uploader(UPLOADER_USER_ID, MUTUAL_ROLES_DATALAND_USERS + UPLOADER_EXTENDED_ROLES),
    Reader(READER_USER_ID, MUTUAL_ROLES_DATALAND_USERS)
}
