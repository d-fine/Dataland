package org.dataland.datalandbackend.model

import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class DataTypeJpaConverter: AttributeConverter<DataType, String> {
    override fun convertToDatabaseColumn(attribute: DataType): String {
        return attribute.name
    }

    override fun convertToEntityAttribute(dbData: String): DataType {
        return DataType.valueOf(dbData)
    }
}