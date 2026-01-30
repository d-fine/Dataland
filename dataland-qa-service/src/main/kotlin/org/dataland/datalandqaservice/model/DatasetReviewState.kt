package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model

import io.swagger.v3.oas.annotations.media.Schema

/**
 * An enum with the different stati of dataset reviews
 */
@Schema(
    enumAsRef = true,
)
enum class DatasetReviewState {
    Pending,
    Finished,
    Aborted,
}
