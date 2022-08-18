package org.dataland.datalandbackend.configurations

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema
import org.dataland.datalandbackend.annotations.DataTypesExtractor
import org.dataland.datalandbackend.model.DataType
import org.springdoc.core.SpringDocUtils
import org.springdoc.core.customizers.OpenApiCustomiser
import org.springframework.stereotype.Component

/**
 * This class ensures that the DataType model gets displayed as an Enum
 * in the OpenApi Spec
 */
@Component
class DataTypeSchemaCustomizer : OpenApiCustomiser {
    init {
        val schema = Schema<Any>()
        schema.`$ref` = "#/components/schemas/DataTypeEnum"
        SpringDocUtils.getConfig().replaceWithSchema(DataType::class.java, schema)
    }

    override fun customise(openApi: OpenAPI) {
        val allowedDataTypes = DataTypesExtractor().getAllDataTypes()
        val actualTargetSchema = Schema<String>()
        actualTargetSchema.enum = allowedDataTypes
        actualTargetSchema.type = "string"

        openApi.components.addSchemas("DataTypeEnum", actualTargetSchema)
    }
}
