package org.dataland.e2etests.utils

import org.dataland.e2etests.auth.TechnicalUser

data class UploadConfiguration(
    val uploadingTechnicalUser: TechnicalUser = TechnicalUser.Admin,
    val bypassQa: Boolean = true,
)
