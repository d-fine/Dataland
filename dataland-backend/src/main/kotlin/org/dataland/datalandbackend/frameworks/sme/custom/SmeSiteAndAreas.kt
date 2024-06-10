package org.dataland.datalandbackend.frameworks.sme.custom

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
    val adjointAreas: List<SmeAreas>?,
// TODO siteAddress as Address or plain string?

)
