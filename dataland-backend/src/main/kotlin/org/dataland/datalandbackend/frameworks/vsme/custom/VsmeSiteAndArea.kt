package org.dataland.datalandbackend.frameworks.vsme.custom

import org.dataland.datalandbackend.model.enums.sme.AreaAdjointness
import org.dataland.datalandbackend.model.generics.Address
import java.math.BigDecimal

/**
 * --- API model ---
 * Sites and area class for vsme framework
 */
data class VsmeSiteAndArea(
    val siteName: String,
    val siteAddress: Address,
    val siteGeocoordinateLongitudeval: BigDecimal?,
    val siteGeocoordinateLatitude: BigDecimal?,
    val areaInHectare: BigDecimal,
    val biodiversitySensitiveArea: String,
    val areaAddress: Address,
    val areaGeocoordinateLongitude: BigDecimal?,
    val areaGeocoordinateLatitude: BigDecimal?,
    val specificationOfAdjointness: AreaAdjointness,
// TODO siteAddress as Address or plain string?

)
