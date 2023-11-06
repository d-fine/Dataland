package org.dataland.datalandcommunitymanager.converters

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.springframework.core.convert.converter.Converter

class StringToDataTypeEnumConverter : Converter<String?, DataTypeEnum?> {
    override fun convert(inputString: String): DataTypeEnum {
        return DataTypeEnum.values().find { it.value == inputString }
            ?: throw IllegalArgumentException("Could not find any value in enum to match with the string: $inputString")
    }
}
