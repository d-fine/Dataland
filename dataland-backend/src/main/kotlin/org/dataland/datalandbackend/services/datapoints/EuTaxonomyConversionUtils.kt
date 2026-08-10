package org.dataland.datalandbackend.services.datapoints

import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.EuTaxonomyActivity
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.EuTaxonomyAlignedActivity
import org.dataland.datalandbackend.model.generics.AmountWithCurrency
import org.dataland.datalandbackendutils.model.DataPointType
import org.dataland.specificationservice.openApiClient.model.DataPointTypeSpecification
import java.math.BigDecimal

/**
 * Resolves the dataPointBaseType id of [dataPointType] using [specs].
 *
 * @param dataPointType the data point type to inspect
 * @param specs the data point type specifications keyed by type
 * @return the id of the data point base type, or null if unknown
 */
internal fun getDataPointBaseTypeId(
    dataPointType: DataPointType,
    specs: Map<DataPointType, DataPointTypeSpecification>,
): String? = specs[dataPointType]?.dataPointBaseType?.id

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
