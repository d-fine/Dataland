package org.dataland.datalandbackend.model.enums.lksg

import io.swagger.v3.oas.annotations.media.Schema

/**
 * A product's procurement category
 */
@Schema(
    enumAsRef = true,
)
enum class ProcurementCategory {
    Products, RawMaterials, Services,
}
