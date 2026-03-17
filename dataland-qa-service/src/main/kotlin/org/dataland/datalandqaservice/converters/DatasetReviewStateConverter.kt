package org.dataland.datalandqaservice.org.dataland.datalandqaservice.converters

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetJudgementState

/**
 * Converts DataType entries in Entities to database values and vice versa
 */
@Converter
class DatasetReviewStateConverter : AttributeConverter<DatasetJudgementState, String> {
    override fun convertToDatabaseColumn(datasetJudgementState: DatasetJudgementState?): String = datasetJudgementState.toString()

    override fun convertToEntityAttribute(datasetReviewStateAsString: String): DatasetJudgementState =
        DatasetJudgementState.valueOf(datasetReviewStateAsString)
}
