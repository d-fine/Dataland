package org.dataland.datalandbackend.model.enums.p2p

import io.swagger.v3.oas.annotations.media.Schema

/**
 * An enum for company sectors in the P2P Framework
 */
@Schema(
    enumAsRef = true,
)
enum class P2pSector {
    Ammonia,
    Automotive,
    Cement,
    CommercialRealEstate,
    ElectricityGeneration,
    FreightTransportByRoad,
    HVCPlastics,
    LivestockFarming,
    ResidentialRealEstate,
    Steel,
    Other,
}
