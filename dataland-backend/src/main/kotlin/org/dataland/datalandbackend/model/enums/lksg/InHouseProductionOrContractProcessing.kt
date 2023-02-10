package org.dataland.datalandbackend.model.enums.lksg

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Methods of how a company production site produces their product
 */
@Schema(
    enumAsRef = true,
)
enum class InHouseProductionOrContractProcessing {
    InHouseProduction, ContractProcessing
}
