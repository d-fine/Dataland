package org.dataland.datalandbackend.frameworks.sme.custom

import org.dataland.datalandbackend.model.enums.sme.AreaAdjointness
import org.dataland.datalandbackend.model.generics.Address
import java.math.BigDecimal

/**
 * --- API model ---
 * Sites and area class for vsme framework
 */
data class SmeSiteAndAreas(
    val siteName: String,
    val siteAddress: Address,
    val siteGeocoordinateLongitudeval: BigDecimal?,
    val siteGeocoordinateLatitude: BigDecimal?,
    val country: String,
    val areaInHectare: BigDecimal,
    val biodiversitySensitiveArea: String,
    val areaAddress: Address,
    val areaGeocoordinateLongitude: BigDecimal?,
    val areaGeocoordinateLatitude: BigDecimal?,
    val specificationOfAdjointness: AreaAdjointness,
// TODO siteAddress as Address or plain string?
    //TODO should we remove country as it is redundant

)
