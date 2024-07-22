package org.dataland.datalandcommunitymanager.model

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum

/**
 * --- API model ---
 * Data class containing only a framework and a year
 */
data class FrameworkAndYear(val framework: DataTypeEnum, val year: String)
