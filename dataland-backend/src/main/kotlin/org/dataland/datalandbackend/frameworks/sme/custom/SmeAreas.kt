package org.dataland.datalandbackend.frameworks.sme.custom

import org.dataland.datalandbackend.model.enums.sme.AreaAdjointness
import org.dataland.datalandbackend.model.generics.Address
import java.math.BigDecimal

/**
 * --- API model ---
 * Area class for vsme framework
 */
data class SmeAreas(
    val country: String,
    val areaInHectare: BigDecimal,
    val biodiversitySensitiveArea: String,
    val areaAddress: Address,
    val areaGeocoordinateLongitude: BigDecimal?,
    val areaGeocoordinateLatitude: BigDecimal?,
    val specificationOfAdjointness: AreaAdjointness,

// TODO areaAddress as Address or plain string?
    // TODO check which field should be nullable

)
