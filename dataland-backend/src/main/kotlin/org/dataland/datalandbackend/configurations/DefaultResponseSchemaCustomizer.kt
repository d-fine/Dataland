package org.dataland.datalandbackend.configurations

import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.responses.ApiResponse
import org.dataland.datalandbackend.annotations.DataTypesExtractor
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.ErrorDetails
import org.dataland.datalandbackend.model.ErrorResponse
import org.springdoc.core.SpringDocUtils
import org.springdoc.core.customizers.OpenApiCustomiser
import org.springframework.stereotype.Component

/**
 * This class ensures that the errorResponse is mapped as the default response
 * for non-explicitly set response codes as suggested in the swagger-docs
 * (ref https://swagger.io/docs/specification/describing-responses/)
 */
@Component
class DefaultResponseSchemaCustomizer : OpenApiCustomiser {

    override fun customise(openApi: OpenAPI) {
        openApi.components.schemas.putAll(ModelConverters.getInstance().read(ErrorDetails::class.java))
        openApi.components.schemas.putAll(ModelConverters.getInstance().read(ErrorResponse::class.java))

        val errorResponseSchema = Schema<Any>()
        errorResponseSchema.`$ref` = "#/components/schemas/ErrorResponse"

        val errorApiResponse = ApiResponse()
            .content(Content().addMediaType(
                org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                MediaType().schema(errorResponseSchema))
            )
            .description("An error occurred")

        openApi.paths.values.forEach {path ->
            path.readOperations().forEach { operation ->
                operation.responses.default = errorApiResponse
            }
        }
    }
}
