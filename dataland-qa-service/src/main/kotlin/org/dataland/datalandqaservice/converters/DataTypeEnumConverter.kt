package org.dataland.datalandqaservice.converters

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum

/**
 * Converts DataTypeEnum entries in DatasetQaReviewLogEntity to database values and vice versa
 */
@Converter(autoApply = true)
class DataTypeEnumConverter : AttributeConverter<DataTypeEnum, String> {
    override fun convertToDatabaseColumn(dataType: DataTypeEnum?): String? = dataType?.value

    override fun convertToEntityAttribute(dataTypeAsString: String?): DataTypeEnum? = DataTypeEnum.decode(dataTypeAsString)
}
