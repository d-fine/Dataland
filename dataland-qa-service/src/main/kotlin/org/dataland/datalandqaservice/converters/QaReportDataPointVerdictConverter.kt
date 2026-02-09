package org.dataland.datalandqaservice.org.dataland.datalandqaservice.converters

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewState

/**
 * Converts QaReportDataPointVerdict entries in Entities to database values and vice versa
 */
@Converter
class QaReportDataPointVerdictConverter : AttributeConverter<QaReportDataPointVerdict, String> {
    override fun convertToDatabaseColumn(qaStatus: QaReportDataPointVerdict?): String? = qaStatus?.toString()

    override fun convertToEntityAttribute(qaStatusAsString: String): QaReportDataPointVerdict =
        QaReportDataPointVerdict
            .valueOf(qaStatusAsString)
}

/**
 * Converts DataType entries in Entities to database values and vice versa
 */
@Converter
class DatasetReviewStateConverter : AttributeConverter<DatasetReviewState, String> {
    override fun convertToDatabaseColumn(datasetReviewState: DatasetReviewState?): String = datasetReviewState.toString()

    override fun convertToEntityAttribute(datasetReviewStateAsString: String): DatasetReviewState =
        DatasetReviewState.valueOf(datasetReviewStateAsString)
}
