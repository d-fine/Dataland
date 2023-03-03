package org.dataland.datalandbackend.model.enums.data

import io.swagger.v3.oas.annotations.media.Schema

/**
 * An enum with the different stati of review a dataset has
 */
@Schema(
    enumAsRef = true,
)
enum class QAStatus { Pending, Accepted }
