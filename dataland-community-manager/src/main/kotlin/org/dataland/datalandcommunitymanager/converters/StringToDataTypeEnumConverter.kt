package org.dataland.datalandcommunitymanager.converters

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.springframework.core.convert.converter.Converter

/**
 * This class is used to define a converter to tell Spring how to convert strings into DataTypeEnums in requests
 */
class StringToDataTypeEnumConverter : Converter<String?, DataTypeEnum?> {
    override fun convert(inputString: String): DataTypeEnum =
        DataTypeEnum.values().find { it.value == inputString }
            ?: throw IllegalArgumentException("Could not find any value in enum to match with the string: $inputString")
}
