package org.dataland.datalandbackend.services.datapoints

import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.EuTaxonomyActivity
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.EuTaxonomyAlignedActivity
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.RelativeAndAbsoluteFinancialShare
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials202673.custom.EuTaxonomyEligibleOrAlignedActivity
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.eutaxonomy.nonfinancials.Activity
import org.dataland.datalandbackend.model.generics.AmountWithCurrency
import java.math.BigDecimal
import kotlin.reflect.full.memberProperties

/**
 * Groups [activities] by the identifier derived from each entry via [identifierOf], mirroring the grouping used to
 * match non-aligned and aligned activities sharing the same activity name, NACE codes, and currency.
 */
internal fun <T> groupActivitiesByIdentifier(
    activities: Iterable<T>?,
    identifierOf: (T) -> Triple<Activity, Set<String>?, String?>,
): Map<Triple<Activity, Set<String>?, String?>, List<T>>? = activities?.groupBy(identifierOf)

internal fun activityIdentifier(activity: EuTaxonomyActivity): Triple<Activity, Set<String>?, String?> =
    Triple(activity.activityName, activity.naceCodes?.toSet(), activity.share?.absoluteShare?.currency)

internal fun activityIdentifier(activity: EuTaxonomyAlignedActivity): Triple<Activity, Set<String>?, String?> =
    Triple(activity.activityName, activity.naceCodes?.toSet(), activity.share?.absoluteShare?.currency)

/**
 * Builds the merged [EuTaxonomyEligibleOrAlignedActivity] for a single activity [identifier] from the aligned and
 * non-aligned activities sharing that identifier, resolving any substantial contribution conflict.
 *
 * @param identifier the activity name, NACE codes, and currency shared by the merged activities
 * @param alignedActivities the aligned activities sharing [identifier], or `null` if there are none
 * @param nonAlignedActivities the non-aligned activities sharing [identifier], or `null` if there are none
 * @return a pair of the merged activity and whether a substantial contribution conflict was resolved for it, or
 *   `null` if the merged activity is not relevant (see [isActivityRelevant])
 */
internal fun buildMergedActivity(
    identifier: Triple<Activity, Set<String>?, String?>,
    alignedActivities: List<EuTaxonomyAlignedActivity>?,
    nonAlignedActivities: List<EuTaxonomyActivity>?,
): Pair<EuTaxonomyEligibleOrAlignedActivity, Boolean>? {
    val alignedAbsoluteShare =
        determineAlignedAbsoluteShare(
            alignedActivities = alignedActivities,
            currency = identifier.third,
        )
    val alignedRelativeShare = determineAlignedRelativeShare(alignedActivities)
    val nonAlignedRelativeShare = determineNonAlignedRelativeShare(nonAlignedActivities)
    val relativeEligibleShareInPercent =
        if (alignedRelativeShare == null && nonAlignedRelativeShare == null) {
            null
        } else {
            (alignedRelativeShare ?: BigDecimal.ZERO) + (nonAlignedRelativeShare ?: BigDecimal.ZERO)
        }
    val activity =
        createEuTaxonomyEligibleOrAlignedActivity(
            identifier,
            alignedAbsoluteShare,
            alignedRelativeShare,
            relativeEligibleShareInPercent,
            alignedActivities,
        )
    val (adjustedActivity, hadConflict) = removeConflictingSubstantialContribution(activity)
    return if (isActivityRelevant(adjustedActivity)) adjustedActivity to hadConflict else null
}

/**
 * Checks whether [activity] carries no meaningful data beyond identification, i.e. it returns true if there is a property except
 * `activityName` and `naceCodes` that is not `null`.
 */
internal fun isActivityRelevant(activity: EuTaxonomyEligibleOrAlignedActivity): Boolean =
    EuTaxonomyEligibleOrAlignedActivity::class
        .memberProperties
        .filter { it.name != "activityName" && it.name != "naceCodes" }
        .any { property -> property.get(activity) != null }

/**
 * Checks the substantial contribution fields (the `BigDecimal?` fields named `substantialContributionTo...InPercent`) of
 * [activity]. If more than one of them is non-null and greater zero, returns a copy of [activity] with all of them set to `null` together
 * with `true` to signal that the substantial contributions were removed; otherwise returns [activity] unchanged together with `false`.
 */
internal fun removeConflictingSubstantialContribution(
    activity: EuTaxonomyEligibleOrAlignedActivity,
): Pair<EuTaxonomyEligibleOrAlignedActivity, Boolean> {
    val substantialContributionProperties =
        EuTaxonomyEligibleOrAlignedActivity::class
            .memberProperties
            .filter { it.name.startsWith("substantialContributionTo") }
    val nonNullCount =
        substantialContributionProperties.count { property ->
            val value = property.get(activity)
            value is BigDecimal && value > BigDecimal.ZERO
        }
    return if (nonNullCount > 1) {
        activity.copy(
            substantialContributionToClimateChangeMitigationInPercent = null,
            substantialContributionToClimateChangeAdaptationInPercent = null,
            substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent = null,
            substantialContributionToTransitionToACircularEconomyInPercent = null,
            substantialContributionToPollutionPreventionAndControlInPercent = null,
            substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent = null,
        ) to true
    } else {
        activity to false
    }
}

/**
 * Builds a single merged [EuTaxonomyEligibleOrAlignedActivity] for one activity [identifier], combining the
 * pre-computed shares with the per-criterion substantial contributions and flags of the aligned activities
 * sharing that identifier.
 *
 * @param identifier the activity name, NACE codes, and currency shared by the merged activities
 * @param alignedAbsoluteShare the combined absolute share reported by the aligned activities, or `null` if none
 * @param alignedRelativeShare the combined relative share in percent reported by the aligned activities, or `null` if none
 * @param relativeEligibleShareInPercent the combined eligible share in percent across aligned and non-aligned activities
 * @param alignedActivities the aligned activities sharing [identifier], used to derive the substantial
 *   contributions and yes/no flags
 * @return the merged eligible-or-aligned activity
 */
internal fun createEuTaxonomyEligibleOrAlignedActivity(
    identifier: Triple<Activity, Set<String>?, String?>,
    alignedAbsoluteShare: AmountWithCurrency?,
    alignedRelativeShare: BigDecimal?,
    relativeEligibleShareInPercent: BigDecimal?,
    alignedActivities: List<EuTaxonomyAlignedActivity>?,
): EuTaxonomyEligibleOrAlignedActivity =
    EuTaxonomyEligibleOrAlignedActivity(
        activityName = identifier.first,
        naceCodes = identifier.second?.toList(),
        relativeEligibleShareInPercent = relativeEligibleShareInPercent,
        share =
            if (alignedActivities == null || alignedActivities.all { it.share == null }) {
                null
            } else {
                RelativeAndAbsoluteFinancialShare(
                    absoluteShare = alignedAbsoluteShare,
                    relativeShareInPercent = alignedRelativeShare,
                )
            },
        substantialContributionToClimateChangeMitigationInPercent =
            determineSubstantialContributions(
                alignedActivities?.map { it.substantialContributionToClimateChangeMitigationInPercent },
                alignedRelativeShare,
            ),
        substantialContributionToClimateChangeAdaptationInPercent =
            determineSubstantialContributions(
                alignedActivities?.map { it.substantialContributionToClimateChangeAdaptationInPercent },
                alignedRelativeShare,
            ),
        substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent =
            determineSubstantialContributions(
                alignedActivities?.map {
                    it.substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent
                },
                alignedRelativeShare,
            ),
        substantialContributionToTransitionToACircularEconomyInPercent =
            determineSubstantialContributions(
                alignedActivities?.map { it.substantialContributionToTransitionToACircularEconomyInPercent },
                alignedRelativeShare,
            ),
        substantialContributionToPollutionPreventionAndControlInPercent =
            determineSubstantialContributions(
                alignedActivities?.map { it.substantialContributionToPollutionPreventionAndControlInPercent },
                alignedRelativeShare,
            ),
        substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent =
            determineSubstantialContributions(
                alignedActivities?.map {
                    it.substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent
                },
                alignedRelativeShare,
            ),
        enablingActivity = determineYesNoActivity(alignedActivities?.map { it.enablingActivity }),
        transitionalActivity = determineYesNoActivity(alignedActivities?.map { it.transitionalActivity }),
    )

/**
 * Determines the merged substantial contribution for a single criterion from the aligned activities sharing
 * an identifier, using the highest reported value (`null` is treated as lower than any value).
 * This implements the rules for the mapping of substantial contribution criteria as defined
 * [here](https://github.com/d-fine/Dataland/wiki/EU-Taxonomy-mapping-for-non%E2%80%90financial-undertakings:-old-template-%E2%86%92-new-template#mapping-to-eligible-or-aligned-activities-not-yet-implemented-for-eu-taxonomy-non-financials-202673---expected-implementation-date-mid-august)
 *
 * @param substantialContributions the per-aligned-activity contribution values for this criterion
 * @param alignedRelativeShare the value to return when the highest contribution is strictly positive
 * @return `null` if there is no aligned activity or all contributions are `null`; `0` if the highest
 *   contribution is `0`; otherwise [alignedRelativeShare]
 * @throws IllegalArgumentException if the highest contribution is negative
 */
@Suppress("ktlint:standard:max-line-length", "MaxLineLength", "kotlin:S103")
private fun determineSubstantialContributions(
    substantialContributions: List<BigDecimal?>?,
    alignedRelativeShare: BigDecimal?,
): BigDecimal? {
    val maxSubstantialContribution = substantialContributions?.maxWithOrNull(nullsFirst())
    return when {
        maxSubstantialContribution == null -> null

        maxSubstantialContribution > BigDecimal.ZERO -> alignedRelativeShare

        maxSubstantialContribution.compareTo(BigDecimal.ZERO) == 0 -> BigDecimal.ZERO

        // comparesTo only checks for equality in value.
        else -> throw IllegalArgumentException("A substantial contribution must not be negative.")
    }
}

/**
 * Determines the merged yes/no flag for a set of aligned activities sharing an identifier, using the
 * strictest reported value under the order `null < No < Yes`.
 *
 * @param activities the per-aligned-activity flag values
 * @return the highest-ranked value in [activities] according to `null < No < Yes`, or `null` if [activities]
 *   is `null` or empty
 */
private fun determineYesNoActivity(activities: List<YesNo?>?): YesNo? {
    val yesNoOrder =
        listOf(null, YesNo.No, YesNo.Yes)
    return activities?.maxByOrNull { yesNoOrder.indexOf(it) }
}
