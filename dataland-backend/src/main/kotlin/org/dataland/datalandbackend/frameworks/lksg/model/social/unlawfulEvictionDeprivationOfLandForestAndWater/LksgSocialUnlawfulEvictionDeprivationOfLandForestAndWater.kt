// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
package org.dataland.datalandbackend.frameworks.lksg.model.social.unlawfulEvictionDeprivationOfLandForestAndWater

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import org.dataland.datalandbackend.model.datapoints.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * The data-model for the UnlawfulEvictionDeprivationOfLandForestAndWater section
 */
@Suppress("MaxLineLength")
data class LksgSocialUnlawfulEvictionDeprivationOfLandForestAndWater(
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Is your company, as a result of the acquisition, development, or other use of land, forests, or bodies of water, which secures a person's livelihood, at risk of carrying out: Unlawful evictions; Unlawful claims of land, forests, or water?""",
        example = """ "Yes"  """,
    )
    val unlawfulEvictionAndTakingOfLand: YesNo? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """If so, what exactly is the risk?""",
        example = """ "This is a string"  """,
    )
    val unlawfulEvictionAndTakingOfLandRisk: String? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Has your company developed and implemented measures that avoid, reduce, mitigate, or remedy direct and indirect negative impacts on the land, and natural resources of indigenous peoples and local communities?""",
        example = """ "Yes"  """,
    )
    val unlawfulEvictionAndTakingOfLandMeasures: YesNo? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Does your company have model contracts for buying or leasing land?""",
        example = """ "Yes"  """,
    )
    @field:Valid()
    val modelContractsForLandPurchaseOrLeasing: BaseDataPoint<YesNo>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Are local communities and stakeholders involved in decision-making processes?""",
        example = """ "Yes"  """,
    )
    val involvementOfLocalsInDecisionMaking: YesNo? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Does your company have a policy for the governance of tenure?""",
        example = """ "Yes"  """,
    )
    @field:Valid()
    val governanceOfTenurePolicy: BaseDataPoint<YesNo>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Have other measures been taken to avoid, reduce, mitigate, or remedy direct and indirect adverse impacts on the lands and natural resources of indigenous peoples and local communities?""",
        example = """ "Yes"  """,
    )
    @field:Valid()
    val unlawfulEvictionAndTakingOfLandOtherMeasures: BaseDataPoint<YesNo>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Please list other measures (if available) you take to avoid, reduce, mitigate, or remedy direct and indirect adverse impacts on the lands and natural resources of indigenous peoples and local communities.""",
        example = """ "This is a string"  """,
    )
    val unlawfulEvictionAndTakingOfLandOtherMeasuresDescription: String? = null,
)
