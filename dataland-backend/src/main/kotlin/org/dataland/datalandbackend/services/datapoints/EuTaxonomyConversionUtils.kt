package org.dataland.datalandbackend.services.datapoints

import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.EuTaxonomyActivity
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.EuTaxonomyAlignedActivity
import org.dataland.datalandbackend.model.generics.AmountWithCurrency
import java.math.BigDecimal

/**
 * Computes the combined absolute share for aligned activities, or `null` if none report one.
 */
internal fun determineAlignedAbsoluteShare(
    alignedActivities: List<EuTaxonomyAlignedActivity>?,
    currency: String?,
): AmountWithCurrency? =
    if (alignedActivities == null ||
        alignedActivities.all { it.share?.absoluteShare == null } ||
        alignedActivities.all { it.share?.absoluteShare?.amount == null }
    ) {
        null
    } else {
        AmountWithCurrency(
            // When no aligned activity with identifier exist or all share.absoluteShare.amount are null, return null
            amount = alignedActivities.sumOf { it.share?.absoluteShare?.amount ?: BigDecimal.ZERO },
            currency = currency,
        )
    }

/**
 * Sums the relative share in percent across the aligned activities sharing an identifier.
 * See also [determineNonAlignedRelativeShare] for non-aligned activities.
 */
internal fun determineAlignedRelativeShare(alignedActivities: List<EuTaxonomyAlignedActivity>?): BigDecimal? =
    alignedActivities?.mapNotNull { it.share?.relativeShareInPercent }?.takeIf { it.isNotEmpty() }?.sumOf { it }

/**
 * Sums the relative share in percent across the non-aligned activities sharing an identifier.
 * See also [determineAlignedRelativeShare] for aligned activities.
 */
internal fun determineNonAlignedRelativeShare(nonAlignedActivities: List<EuTaxonomyActivity>?): BigDecimal? =
    nonAlignedActivities?.mapNotNull { it.share?.relativeShareInPercent }?.takeIf { it.isNotEmpty() }?.sumOf { it }
