package org.dataland.datalandcommunitymanager.converters

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum

/**
 *  This converter is used for database entities to convert the DataType enum to strings when saving to the database.
 *  and to convert the string to the DataType when loading data from the database.
 *  As autoApply=true, this should work for all stored DataTypeEnums.
 */
@Converter(autoApply = true)
class DataTypEnumAttributeConverter : AttributeConverter<DataTypeEnum, String> {
    override fun convertToDatabaseColumn(dataType: DataTypeEnum?): String? = dataType?.name

    override fun convertToEntityAttribute(string: String?): DataTypeEnum? =
        string?.let {
            DataTypeEnum.entries.find { it.name == string } ?: throw IllegalArgumentException("Unknown dataType $string")
        }
}
