package org.dataland.datalanduserservice.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum

/**
 * Converts DataTypeEnum entries for the User Service repositories to database values and vice versa
 */
@Converter
class DataTypeEnumConverter : AttributeConverter<DataTypeEnum, String> {
    override fun convertToDatabaseColumn(dataType: DataTypeEnum): String = dataType.value

    override fun convertToEntityAttribute(dataTypeAsString: String): DataTypeEnum? = DataTypeEnum.decode(dataTypeAsString)
}
