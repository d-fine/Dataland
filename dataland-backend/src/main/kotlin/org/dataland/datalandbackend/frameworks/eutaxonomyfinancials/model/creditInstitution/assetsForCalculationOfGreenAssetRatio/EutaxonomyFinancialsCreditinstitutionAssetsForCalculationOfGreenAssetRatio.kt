// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
package org.dataland.datalandbackend.frameworks.eutaxonomyfinancials.model.creditInstitution.assetsForCalculationOfGreenAssetRatio

import jakarta.validation.Valid
import org.dataland.datalandbackend.model.datapoints.CurrencyDataPoint
import org.dataland.datalandbackend.validator.MinimumValue

/**
 * The data-model for the AssetsForCalculationOfGreenAssetRatio section
 */
@Suppress("MaxLineLength")
data class EutaxonomyFinancialsCreditinstitutionAssetsForCalculationOfGreenAssetRatio(
    @field:MinimumValue(minimumValue = 0)
    @field:Valid()
    val totalGrossCarryingAmount: CurrencyDataPoint? = null,
    @field:MinimumValue(minimumValue = 0)
    @field:Valid()
    val totalAmountOfAssetsTowardsTaxonomyRelevantSectorsTaxonomyEligible: CurrencyDataPoint? = null,
    @field:MinimumValue(minimumValue = 0)
    @field:Valid()
    val totalAmountOfAssetsWhichAreEnvironmentallySustainableTaxonomyAligned: CurrencyDataPoint? = null,
    @field:MinimumValue(minimumValue = 0)
    @field:Valid()
    val totalAmountOfEnvironmentallySustainableAssetsWhichAreUseOfProceeds: CurrencyDataPoint? = null,
    @field:MinimumValue(minimumValue = 0)
    @field:Valid()
    val totalAmountOfEnvironmentallySustainableAssetsWhichAreTransitional: CurrencyDataPoint? = null,
    @field:MinimumValue(minimumValue = 0)
    @field:Valid()
    val totalAmountOfEnvironmentallySustainableAssetsWhichAreEnabling: CurrencyDataPoint? = null,
)
