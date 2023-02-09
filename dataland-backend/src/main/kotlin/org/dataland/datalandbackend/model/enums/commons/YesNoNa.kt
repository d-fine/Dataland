package org.dataland.datalandbackend.model.enums.commons

import io.swagger.v3.oas.annotations.media.Schema

/**
 * An enum with the values Yes, No and N/A to be used for fields that are not backed by a datapoint. Hence, there
 * should be no datapoint instances using this enum.
 */
@Schema(
    enumAsRef = true,
)
enum class YesNoNa { Yes, No, NA }
