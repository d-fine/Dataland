package org.dataland.datalandbackend.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.dataland.datalandbackend.model.DataType

/**
 * Converts DataType entries in Entities to database values and vice versa
 */
@Converter
class DataTypeConverter : AttributeConverter<DataType, String> {
    override fun convertToDatabaseColumn(dataType: DataType): String = dataType.toString()

    override fun convertToEntityAttribute(dataTypeAsString: String): DataType = DataType.valueOf(dataTypeAsString)
}
