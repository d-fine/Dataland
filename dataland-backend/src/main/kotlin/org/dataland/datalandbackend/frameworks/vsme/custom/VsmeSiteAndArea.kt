package org.dataland.datalandbackend.frameworks.vsme.custom

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.sme.AreaAdjointness
import org.dataland.datalandbackend.model.generics.Address
import java.math.BigDecimal

/**
 * --- API model ---
 * Sites and area class for vsme framework
 */
data class VsmeSiteAndArea(
    @field:JsonProperty(required = true)
    val siteName: String,
    @field:JsonProperty(required = true)
    val siteAddress: Address,
    val siteGeocoordinateLongitudeval: BigDecimal?,
    val siteGeocoordinateLatitude: BigDecimal?,
    val areaInHectare: BigDecimal?,
    val biodiversitySensitiveArea: String?,
    @field:JsonProperty(required = true)
    val areaAddress: Address,
    val areaGeocoordinateLongitude: BigDecimal?,
    val areaGeocoordinateLatitude: BigDecimal?,
    @field:JsonProperty(required = true)
    val specificationOfAdjointness: AreaAdjointness,
)
