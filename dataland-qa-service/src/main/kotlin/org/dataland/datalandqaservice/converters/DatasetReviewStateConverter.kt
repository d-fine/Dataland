package org.dataland.datalandqaservice.org.dataland.datalandqaservice.converters

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewState

/**
 * Converts DataType entries in Entities to database values and vice versa
 */
@Converter
class DatasetReviewStateConverter : AttributeConverter<DatasetReviewState, String> {
    override fun convertToDatabaseColumn(datasetReviewState: DatasetReviewState?): String = datasetReviewState.toString()

    override fun convertToEntityAttribute(datasetReviewStateAsString: String): DatasetReviewState =
        DatasetReviewState.valueOf(datasetReviewStateAsString)
}
