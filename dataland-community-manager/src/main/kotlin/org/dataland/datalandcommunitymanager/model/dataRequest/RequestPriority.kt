package org.dataland.datalandcommunitymanager.model.dataRequest

import io.swagger.v3.oas.annotations.media.Schema

/**
 * An enum with the different priorities of a data request
 */
@Schema(
    enumAsRef = true,
)
enum class RequestPriority { Low, Baseline, High, Urgent }
