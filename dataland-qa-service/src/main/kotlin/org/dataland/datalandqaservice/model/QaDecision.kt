package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model

import io.swagger.v3.oas.annotations.media.Schema

/**
 * An enum with the different decisions a judge can make regarding a dataset,
 * which is used in the dataset judgement process
 */
@Schema(
    enumAsRef = true,
)
enum class QaDecision {
    Accepted,
    Rejected,
}
