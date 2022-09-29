package org.dataland.datalandbackend.model.enums.eutaxonomy

import io.swagger.v3.oas.annotations.media.Schema

/**
 * An enum with the values Yes/No as used e.g. in the EuTaxonomy frameworks
 */
@Schema(
    enumAsRef = true
)
enum class YesNo { Yes, No }
