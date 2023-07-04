package org.dataland.datalandbackend.model.enums.lksg

import io.swagger.v3.oas.annotations.media.Schema

/**
 * A procurement category
 */
@Schema(
    enumAsRef = true,
)
enum class ProcurementCategoryType {
    Products, RawMaterials, Services,
}
