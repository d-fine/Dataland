// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
package org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.model.capex

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.EuTaxonomyActivity
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.EuTaxonomyAlignedActivity
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.model.capex.alignedShare
    .EutaxonomyNonFinancialsCapexAlignedShare
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.model.capex.eligibleShare
    .EutaxonomyNonFinancialsCapexEligibleShare
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.model.capex.nonAlignedShare
    .EutaxonomyNonFinancialsCapexNonAlignedShare
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.model.capex.nonEligibleShare
    .EutaxonomyNonFinancialsCapexNonEligibleShare
import org.dataland.datalandbackend.model.datapoints.CurrencyDataPoint
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import java.math.BigDecimal
import kotlin.collections.MutableList

/**
 * The data-model for the Capex section
 */
@Suppress("LargeClass", "MaxLineLength")
data class EutaxonomyNonFinancialsCapex(
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Total CapEx for the reported year. Capital expenditures are non-consumable investments, e.g. for acquiring, upgrading, and maintaining physical assets such as property, plants, buildings, technology """,
        example = """{
      "value" : 100.5,
      "currency" : "USD",
      "quality" : "Reported",
      "comment" : "The value is reported by the company.",
      "dataSource" : {
      "page" : "5-7",
      "tagName" : "monetaryAmount",
      "fileName" : "AnnualReport2020.pdf",
      "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
    } """,
    )
    val totalAmount: CurrencyDataPoint? = null,
    @field:Valid()
    val nonEligibleShare: EutaxonomyNonFinancialsCapexNonEligibleShare? = null,
    @field:Valid()
    val eligibleShare: EutaxonomyNonFinancialsCapexEligibleShare? = null,
    @field:Valid()
    val nonAlignedShare: EutaxonomyNonFinancialsCapexNonAlignedShare? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Absolute value and share of the CapEx per activity that is not taxonomy-aligned but eligible""",
        example = """{
      "value" : [{
      "activityName" : "CollectionAndTransportOfNonHazardousWasteInSourceSegregatedFractions",
      "naceCodes" : [ "E.38.11" ],
      "share" : {
        "relativeShareInPercent" : 25,
        "absoluteShare" : {
          "amount" : 100.5,
          "currency" : "EUR"
        }
      }
    }, {
      "activityName" : "InfrastructureForRailTransport",
      "naceCodes" : [ "C27.9", "C30.20" ],
      "share" : {
        "relativeShareInPercent" : 25,
        "absoluteShare" : {
          "amount" : 100.5,
          "currency" : "EUR"
        }
      }
    }, {
      "activityName" : "LowCarbonAirportInfrastructure",
      "naceCodes" : [ "F41.20", "F42.99" ],
      "share" : {
        "relativeShareInPercent" : 25,
        "absoluteShare" : {
          "amount" : 100.5,
          "currency" : "EUR"
        }
      }
    }], 
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
    val nonAlignedActivities: ExtendedDataPoint<MutableList<EuTaxonomyActivity>?>? = null,
    @field:Valid()
    val alignedShare: EutaxonomyNonFinancialsCapexAlignedShare? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Taxonomy-eligible proportion of CapEx substantially contributing to climate change mitigation""",
        example = """{
      "value" : 13.52, 
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
    val substantialContributionToClimateChangeMitigationInPercentEligible: ExtendedDataPoint<BigDecimal?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Taxonomy-aligned proportion of CapEx substantially contributing to climate change mitigation""",
        example = """{
      "value" : 13.52, 
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
    val substantialContributionToClimateChangeMitigationInPercentAligned: ExtendedDataPoint<BigDecimal?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Taxonomy-aligned use of proceeds share substantially contributing to climate change mitigation""",
        example = """{
      "value" : 13.52, 
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
    val substantialContributionToClimateChangeMitigationInPercentOfWhichUseOfProceeds: ExtendedDataPoint<BigDecimal?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Taxonomy-aligned and enabling proportion of CapEx substantially contributing to climate change mitigation""",
        example = """{
      "value" : 13.52, 
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
    val substantialContributionToClimateChangeMitigationInPercentEnablingShare: ExtendedDataPoint<BigDecimal?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Taxonomy-aligned and transitional proportion of CapEx substantially contributing to climate change mitigation""",
        example = """{
      "value" : 13.52, 
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
    val substantialContributionToClimateChangeMitigationInPercentTransitionalShare: ExtendedDataPoint<BigDecimal?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Taxonomy-eligible proportion of CapEx substantially contributing to climate change adaptation""",
        example = """{
      "value" : 13.52, 
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
    val substantialContributionToClimateChangeAdaptationInPercentEligible: ExtendedDataPoint<BigDecimal?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Taxonomy-aligned proportion of CapEx substantially contributing to climate change adaptation""",
        example = """{
      "value" : 13.52, 
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
    val substantialContributionToClimateChangeAdaptationInPercentAligned: ExtendedDataPoint<BigDecimal?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Taxonomy-aligned use of proceeds share substantially contributing to climate change adaptation""",
        example = """{
      "value" : 13.52, 
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
    val substantialContributionToClimateChangeAdaptationInPercentOfWhichUseOfProceeds: ExtendedDataPoint<BigDecimal?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Taxonomy-aligned and enabling proportion of CapEx substantially contributing to climate change adaptation""",
        example = """{
      "value" : 13.52, 
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
    val substantialContributionToClimateChangeAdaptationInPercentEnablingShare: ExtendedDataPoint<BigDecimal?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Taxonomy-eligible proportion of CapEx substantially contributing to sustainable use and protection of water and marine resources""",
        example = """{
      "value" : 13.52, 
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
    val substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercentEligible:
        ExtendedDataPoint<BigDecimal?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Taxonomy-aligned proportion of CapEx substantially contributing to sustainable use and protection of water and marine resources""",
        example = """{
      "value" : 13.52, 
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
    val substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercentAligned:
        ExtendedDataPoint<BigDecimal?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Taxonomy-aligned use of proceeds share substantially contributing to sustainable use and protection of water and marine resources""",
        example = """{
      "value" : 13.52, 
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
    val substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercentOfWhichUseOfProceeds:
        ExtendedDataPoint<BigDecimal?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Taxonomy-aligned and enabling proportion of CapEx substantially contributing to sustainable use and protection of water and marine resources""",
        example = """{
      "value" : 13.52, 
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
    val substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercentEnablingShare:
        ExtendedDataPoint<BigDecimal?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Taxonomy-eligible proportion of CapEx substantially contributing to circular economy""",
        example = """{
      "value" : 13.52, 
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
    val substantialContributionToTransitionToACircularEconomyInPercentEligible: ExtendedDataPoint<BigDecimal?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Taxonomy-aligned proportion of CapEx substantially contributing to circular economy""",
        example = """{
      "value" : 13.52, 
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
    val substantialContributionToTransitionToACircularEconomyInPercentAligned: ExtendedDataPoint<BigDecimal?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Taxonomy-aligned use of proceeds share substantially contributing to circular economy""",
        example = """{
      "value" : 13.52, 
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
    val substantialContributionToTransitionToACircularEconomyInPercentOfWhichUseOfProceeds: ExtendedDataPoint<BigDecimal?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Taxonomy-aligned and enabling proportion of CapEx substantially contributing to circular economy""",
        example = """{
      "value" : 13.52, 
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
    val substantialContributionToTransitionToACircularEconomyInPercentEnablingShare: ExtendedDataPoint<BigDecimal?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Taxonomy-eligible proportion of CapEx substantially contributing to pollution prevention and control""",
        example = """{
      "value" : 13.52, 
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
    val substantialContributionToPollutionPreventionAndControlInPercentEligible: ExtendedDataPoint<BigDecimal?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Taxonomy-aligned proportion of CapEx substantially contributing to pollution prevention and control""",
        example = """{
      "value" : 13.52, 
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
    val substantialContributionToPollutionPreventionAndControlInPercentAligned: ExtendedDataPoint<BigDecimal?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Taxonomy-aligned use of proceeds share substantially contributing to pollution prevention and control""",
        example = """{
      "value" : 13.52, 
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
    val substantialContributionToPollutionPreventionAndControlInPercentOfWhichUseOfProceeds: ExtendedDataPoint<BigDecimal?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Taxonomy-aligned and enabling proportion of CapEx substantially contributing to pollution prevention and control""",
        example = """{
      "value" : 13.52, 
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
    val substantialContributionToPollutionPreventionAndControlInPercentEnablingShare: ExtendedDataPoint<BigDecimal?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Taxonomy-eligible proportion of CapEx substantially contributing to protection and restoration of biodiversity and ecosystems""",
        example = """{
      "value" : 13.52, 
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
    val substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercentEligible:
        ExtendedDataPoint<BigDecimal?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Taxonomy-aligned proportion of CapEx substantially contributing to protection and restoration of biodiversity and ecosystems""",
        example = """{
      "value" : 13.52, 
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
    val substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercentAligned:
        ExtendedDataPoint<BigDecimal?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Taxonomy-aligned use of proceeds share substantially contributing to protection and restoration of biodiversity and ecosystems""",
        example = """{
      "value" : 13.52, 
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
    val substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercentOfWhichUseOfProceeds:
        ExtendedDataPoint<BigDecimal?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Taxonomy-aligned and enabling proportion of CapEx substantially contributing to protection and restoration of biodiversity and ecosystems""",
        example = """{
      "value" : 13.52, 
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
    val substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercentEnablingShare:
        ExtendedDataPoint<BigDecimal?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Absolute value and share of the CapEx per activity that is taxonomy-aligned, i.e., generated by an eligible economic activity that is making a substantial contribution to at least one of the climate and environmental objectives, while also doing no significant harm to the remaining objectives and meeting minimum standards on human rights and labour standards""",
        example = """[{
      "activityName" : "CollectionAndTransportOfNonHazardousWasteInSourceSegregatedFractions",
      "naceCodes" : [ "E.38.11" ],
      "share" : {
        "relativeShareInPercent" : 25,
        "absoluteShare" : {
          "amount" : 100.5,
          "currency" : "EUR"
        }
      },
      "substantialContributionToClimateChangeMitigationInPercent" : 10,
      "substantialContributionToClimateChangeAdaptationInPercent" : 5,
      "substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent" : 35,
      "substantialContributionToTransitionToACircularEconomyInPercent" : 70,
      "substantialContributionToPollutionPreventionAndControlInPercent" : 2,
      "substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent" : 1,
      "dnshToClimateChangeMitigation" : "Yes",
      "dnshToClimateChangeAdaptation" : "Yes",
      "dnshToSustainableUseAndProtectionOfWaterAndMarineResources" : "Yes",
      "dnshToTransitionToACircularEconomy" : "Yes",
      "dnshToPollutionPreventionAndControl" : "Yes",
      "dnshToProtectionAndRestorationOfBiodiversityAndEcosystems" : "Yes",
      "minimumSafeguards" : "Yes",
      "enablingActivity" : "No",
      "transitionalActivity" : "Yes"
    }, {
      "activityName" : "InfrastructureForRailTransport",
      "naceCodes" : [ "C27.9", "C30.20" ],
      "share" : {
        "relativeShareInPercent" : 25,
        "absoluteShare" : {
          "amount" : 100.5,
          "currency" : "EUR"
        }
      },
      "substantialContributionToClimateChangeMitigationInPercent" : 7,
      "substantialContributionToClimateChangeAdaptationInPercent" : 5,
      "substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent" : 35,
      "substantialContributionToTransitionToACircularEconomyInPercent" : 70,
      "substantialContributionToPollutionPreventionAndControlInPercent" : 2,
      "substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent" : 1,
      "dnshToClimateChangeMitigation" : "Yes",
      "dnshToClimateChangeAdaptation" : "Yes",
      "dnshToSustainableUseAndProtectionOfWaterAndMarineResources" : "Yes",
      "dnshToTransitionToACircularEconomy" : "Yes",
      "dnshToPollutionPreventionAndControl" : "Yes",
      "dnshToProtectionAndRestorationOfBiodiversityAndEcosystems" : "No",
      "minimumSafeguards" : "Yes",
      "enablingActivity" : "No",
      "transitionalActivity" : "Yes"
    }] """,
    )
    @field:Valid()
    val alignedActivities: ExtendedDataPoint<MutableList<EuTaxonomyAlignedActivity>?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Share of the taxonomy-aligned CapEx from total aligned CapEx that is linked to activities that enable reduction of GHG in other sectors""",
        example = """{
      "value" : 13.52, 
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
    val enablingShareInPercent: ExtendedDataPoint<BigDecimal?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Share of the taxonomy-aligned CapEx from total aligned CapEx that is linked to activities with significantly lower GHG emissions than the sector or industry average""",
        example = """{
      "value" : 13.52, 
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
    val transitionalShareInPercent: ExtendedDataPoint<BigDecimal?>? = null,
)
