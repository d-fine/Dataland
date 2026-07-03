package org.dataland.datalandbackend.services.datapoints

import org.dataland.datalandbackend.interfaces.datapoints.BaseDataPoint
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.datapoints.extended.ExtendedCurrencyDataPoint
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.dataland.datalandbackendutils.model.DataPointType
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.specificationservice.openApiClient.model.DataPointTypeSpecification
import org.dataland.datalandbackend.interfaces.datapoints.ExtendedDataPoint as ExtendedDataPointInterface

private const val EXTENDED_CURRENCY_BASE_TYPE = "extendedCurrency"

/**
 * Checks whether [dataPointType] has the extended currency base type in [specs].
 *
 * @param dataPointType the data point type to inspect
 * @param specs the data point type specifications keyed by type
 * @return true if the type is specified as an extended currency data point
 */
internal fun isCurrencyDataPoint(
    dataPointType: DataPointType,
    specs: Map<DataPointType, DataPointTypeSpecification>,
): Boolean = specs[dataPointType]?.dataPointBaseType?.id == EXTENDED_CURRENCY_BASE_TYPE

/**
 * Returns the currency set on [dataPoint].
 *
 * @param dataPoint the currency data point to inspect
 * @return the non-null currency of the data point
 * @throws IllegalArgumentException if [dataPoint] has no currency
 */
internal fun getCurrency(dataPoint: ExtendedCurrencyDataPoint): String {
    require(dataPoint.currency != null) { "Currency data points used in calculations must have a currency." }
    return dataPoint.currency
}

/**
 * Returns the shared currency used by all [dataPoints].
 *
 * @param dataPoints the currency data points to inspect
 * @return the single currency used by all given data points
 * @throws IllegalArgumentException if not all data points have the same non-null currency
 */
internal fun getCommonCurrency(dataPoints: Collection<ExtendedCurrencyDataPoint>): String {
    val currencies = dataPoints.map { getCurrency(it) }.toSet()
    require(currencies.size == 1) { "Currency data points used in summation must all have the same currency." }
    return currencies.single()
}

/**
 * Wraps [calculatedDataPoint] in an [UploadedDataPoint] using metadata from the first source input.
 *
 * @param inputs the source inputs providing reporting period and company ID
 * @param targetType the data point type assigned to the calculated data point
 * @param calculatedDataPoint the calculated data point object to serialize
 * @return the uploaded data point representing the calculation result
 */
internal fun <T : Any> createUploadedDataPoint(
    inputs: Collection<UploadedDataPoint>,
    targetType: DataPointType,
    calculatedDataPoint: BaseDataPoint<T>,
): UploadedDataPoint =
    UploadedDataPoint(
        dataPoint = defaultObjectMapper.writeValueAsString(calculatedDataPoint),
        reportingPeriod = inputs.first().reportingPeriod,
        companyId = inputs.first().companyId,
        dataPointType = targetType,
    )

/**
 * Merges the given [QualityOptions] into a single entry, returning the lowest quality among them.
 *
 * @param inputs the quality values to merge
 * @return the lowest quality value among the inputs
 */
internal fun mergeQuality(inputs: Collection<QualityOptions?>): QualityOptions? {
    val qualityOrder =
        listOf(
            QualityOptions.Audited, QualityOptions.Reported, QualityOptions.Estimated, QualityOptions.Incomplete,
            QualityOptions.NoDataFound, null,
        )
    return inputs.maxByOrNull { qualityOrder.indexOf(it) }
}

/**
 * Merges the given list of [ExtendedDocumentReference] into a single reference.
 *
 * @param inputs the document references to merge
 * @return the reference with the smallest file reference, or null if no references are provided
 */
internal fun mergeDataSources(inputs: Collection<ExtendedDocumentReference>): ExtendedDocumentReference? =
    inputs.minByOrNull { it.fileReference }

/**
 * Extracts the data source of a given [dataPoint]
 *
 * @param dataPoint The data point provided for data source extraction
 * @return The associated data source object
 */
internal fun getDataSource(dataPoint: ExtendedDataPointInterface<*>): ExtendedDocumentReference? =
    when (dataPoint) {
        is ExtendedCurrencyDataPoint -> dataPoint.dataSource
        is ExtendedDataPoint<*> -> dataPoint.dataSource
        else -> throw IllegalArgumentException("Data point of type ${dataPoint::class.java} is not supported.")
    }
