package org.dataland.datalandbackend.utils

import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.RelativeAndAbsoluteFinancialShare
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials202673.custom.EuTaxonomyEligibleOrAlignedActivity
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.dataland.datalandbackend.model.enums.eutaxonomy.nonfinancials.Activity
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.specificationservice.openApiClient.model.DataPointTypeSpecification
import org.dataland.specificationservice.openApiClient.model.IdWithRef
import java.math.BigDecimal

const val ELIGIBLE_OR_ALIGNED_TARGET_TYPE = "eligibleOrAlignedType"
const val NUCLEAR_ELIGIBLE_TARGET_TYPE = "relativeShareOfNuclearActivitiesInPercentEligible"
const val NUCLEAR_ALIGNED_TARGET_TYPE = "relativeShareOfNuclearActivitiesInPercentAligned"
const val FOSSIL_GAS_ELIGIBLE_TARGET_TYPE = "relativeShareOfFossilGasActivitiesInPercentEligible"
const val FOSSIL_GAS_ALIGNED_TARGET_TYPE = "relativeShareOfFossilGasActivitiesInPercentAligned"

private val eligibleOrAlignedRef = IdWithRef(id = "extendedEuTaxonomyEligibleOrAlignedActivitiesComponent", ref = "dummy")

val shareSpecs =
    dummySpecs + (
        ELIGIBLE_OR_ALIGNED_TARGET_TYPE to
            DataPointTypeSpecification(
                dataPointType = IdWithRef(id = ELIGIBLE_OR_ALIGNED_TARGET_TYPE, ref = "dummy"),
                name = "Eligible or Aligned Activities",
                businessDefinition = "dummy",
                dataPointBaseType = eligibleOrAlignedRef,
                usedBy = emptyList(),
                calculationRules = emptyList(),
            )
    )

fun eligibleOrAlignedActivity(
    activityName: Activity = Activity.AcquisitionAndOwnershipOfBuildings,
    relativeEligibleShareInPercent: BigDecimal? = null,
    relativeShareInPercent: BigDecimal? = null,
    naceCodes: List<String>? = listOf(NACE_CODE_FIXTURE),
) = EuTaxonomyEligibleOrAlignedActivity(
    activityName = activityName,
    naceCodes = naceCodes,
    relativeEligibleShareInPercent = relativeEligibleShareInPercent,
    share =
        RelativeAndAbsoluteFinancialShare(
            absoluteShare = null,
            relativeShareInPercent = relativeShareInPercent,
        ),
    substantialContributionToClimateChangeMitigationInPercent = null,
    substantialContributionToClimateChangeAdaptationInPercent = null,
    substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent = null,
    substantialContributionToTransitionToACircularEconomyInPercent = null,
    substantialContributionToPollutionPreventionAndControlInPercent = null,
    substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent = null,
    enablingActivity = null,
    transitionalActivity = null,
)

fun createEligibleOrAlignedInput(
    activities: List<EuTaxonomyEligibleOrAlignedActivity>?,
    quality: QualityOptions? = QualityOptions.Reported,
) = createUploadedDataPoint(
    defaultObjectMapper.writeValueAsString(
        ExtendedDataPoint(value = activities, quality = quality),
    ),
).copy(dataPointType = ELIGIBLE_OR_ALIGNED_TARGET_TYPE)
