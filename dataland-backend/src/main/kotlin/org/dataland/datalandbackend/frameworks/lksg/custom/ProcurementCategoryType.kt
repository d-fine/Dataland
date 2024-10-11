package org.dataland.datalandbackend.frameworks.lksg.custom

import io.swagger.v3.oas.annotations.media.Schema

/**
 * A procurement category
 */
@Schema(
    enumAsRef = true,
)
enum class ProcurementCategoryType {
    Products,
    RawMaterials,
    Services,
}
