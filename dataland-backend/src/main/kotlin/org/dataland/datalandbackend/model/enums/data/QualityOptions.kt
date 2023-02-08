package org.dataland.datalandbackend.model.enums.data

import io.swagger.v3.oas.annotations.media.Schema

/**
 * A class that holds the level of confidence associated to the reported value/amount of the DataPoint
 */
@Schema(
    enumAsRef = true,
)
enum class QualityOptions { Audited, Reported, Estimated, Incomplete, NA }
