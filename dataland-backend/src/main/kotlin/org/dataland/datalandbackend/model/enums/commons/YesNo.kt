package org.dataland.datalandbackend.model.enums.commons

import io.swagger.v3.oas.annotations.media.Schema

/**
 * An enum with the values Yes and No to be used for fields backed by datapoints.
 */
@Schema(
    enumAsRef = true,
)
enum class YesNo { Yes, No }
