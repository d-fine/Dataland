// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
package org.dataland.datalandbackend.frameworks.vsme.model.basic.biodiversity

import org.dataland.datalandbackend.frameworks.vsme.custom.VsmeSiteAndArea
import java.math.BigDecimal

/**
 * The data-model for the Biodiversity section
 */
data class VsmeBasicBiodiversity(
    val sitesAndAreas: List<VsmeSiteAndArea?>? = null,

    val totalSealedAreaPreviousYear: BigDecimal? = null,

    val totalSealedAreaReportingYear: BigDecimal? = null,

    val percentualChangeSealedArea: BigDecimal? = null,

    val totalNatureOrientedAreaOnSitePreviousYear: BigDecimal? = null,

    val totalNatureOrientedAreaOnSiteReportingYear: BigDecimal? = null,

    val percentualChangeNatureOrientedOnSite: BigDecimal? = null,

    val totalNatureOrientedAreaOffSitePreviousYear: BigDecimal? = null,

    val totalNatureOrientedAreaOffSiteReportingYear: BigDecimal? = null,

    val percentualChangeNatureOrientedOffSite: BigDecimal? = null,

    val totalUseOfLandPreviousYear: BigDecimal? = null,

    val totalUseOfLandReportingYear: BigDecimal? = null,

    val percentualChangeLandUse: BigDecimal? = null,

)