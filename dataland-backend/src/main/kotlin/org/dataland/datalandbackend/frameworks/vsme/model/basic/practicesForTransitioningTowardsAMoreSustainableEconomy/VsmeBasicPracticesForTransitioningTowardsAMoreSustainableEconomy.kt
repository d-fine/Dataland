// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
package org.dataland.datalandbackend.frameworks.vsme.model.basic.practicesForTransitioningTowardsAMoreSustainableEconomy

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import org.dataland.datalandbackend.model.datapoints.BaseDataPoint

/**
 * The data-model for the PracticesForTransitioningTowardsAMoreSustainableEconomy section
 */
@Suppress("MaxLineLength")
data class VsmeBasicPracticesForTransitioningTowardsAMoreSustainableEconomy(
    @field:Valid()
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Please, describe specific practices for transitioning towards a more sustainable economy in case you have them in place.""",
        example = """[
      {
        "value": "lifetime value",
        "dataSource": {
          "fileName": "Certification",
          "fileReference": "1902e40099c913ecf3715388cb2d9f7f84e6f02a19563db6930adb7b6cf22868"
        }
      },
      {
        "value": "technologies",
        "dataSource": {
          "fileName": "Policy",
          "fileReference": "04c4e6cd07eeae270635dd909f58b09b2104ea5e92ec22a80b6e7ba1d0b75dd0"
        }
      }
    ] """,
    )
    val undertakenMeasures: List<BaseDataPoint<String>>? = null,
)
