package org.dataland.datalandbackend.configurations

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema
import org.dataland.datalandbackend.annotations.DataTypesExtractor
import org.dataland.datalandbackend.model.DataType
import org.springdoc.core.utils.SpringDocUtils
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.stereotype.Component

/**
 * This class ensures that the DataType model gets displayed as an Enum
 * in the OpenApi Spec
 */
@Component
class DataTypeSchemaCustomizer : OpenApiCustomizer {
    init {
        val dataTypeEnumRefSchema = Schema<Any>()
        dataTypeEnumRefSchema.`$ref` = "#/components/schemas/DataTypeEnum"
        SpringDocUtils.getConfig().replaceWithSchema(DataType::class.java, dataTypeEnumRefSchema)
    }

    override fun customise(openApi: OpenAPI) {
        val allowedDataTypes = DataTypesExtractor().getAllDataTypes()
        val actualDataTypeEnumSchema = Schema<String>()
        actualDataTypeEnumSchema.enum = allowedDataTypes
        actualDataTypeEnumSchema.type = "string"

        openApi.components.addSchemas("DataTypeEnum", actualDataTypeEnumSchema)
    }
}
