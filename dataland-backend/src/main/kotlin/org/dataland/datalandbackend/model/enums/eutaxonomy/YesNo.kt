package org.dataland.datalandbackend.model.enums.eutaxonomy

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Possible options to specify if obligation to report exists
 */
@Schema(
    enumAsRef = true
)
enum class YesNo { Yes, No }
