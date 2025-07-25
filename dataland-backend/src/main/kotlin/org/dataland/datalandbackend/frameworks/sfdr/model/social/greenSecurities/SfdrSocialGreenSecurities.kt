// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
package org.dataland.datalandbackend.frameworks.sfdr.model.social.greenSecurities

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * The data-model for the GreenSecurities section
 */
@Suppress("MaxLineLength")
data class SfdrSocialGreenSecurities(
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Does the company have securities in investments not certified as green under a future EU legal act setting up an EU Green Bond Standard?""",
        example = """{
      "value" :  "Yes" , 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val securitiesNotCertifiedAsGreen: ExtendedDataPoint<YesNo?>? = null,
)
