package org.dataland.datalandbackendutils.configurations

import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.headers.Header
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.responses.ApiResponse
import org.dataland.datalandbackendutils.model.ErrorDetails
import org.dataland.datalandbackendutils.model.ErrorResponse
import org.springdoc.core.customizers.OpenApiCustomiser
import org.springframework.stereotype.Component

/**
 * This class ensures that the errorResponse is mapped as the default response
 * for non-explicitly set response codes as suggested in the swagger-docs
 * (ref https://swagger.io/docs/specification/describing-responses/)
 */
@Component
class DefaultResponseSchemaCustomizer : OpenApiCustomiser {

    private val errorResponseSchema = Schema<Any>().`$ref`("#/components/schemas/ErrorResponse")

    private val errorApiResponse = ApiResponse()
        .content(
            Content().addMediaType(
                org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                MediaType().schema(errorResponseSchema)
            )
        )
        .description("An error occurred")

    // All errors follow the default errorApiResponse except for the 401 error which only returns the error in the
    // WWW-Authenticate header as described in rfc9110
    private val unauthorizedApiResponse = ApiResponse()
        .description("Unauthorized")
        .addHeaderObject("WWW-Authenticate", Header().schema(Schema<Any>().type("string")))

    override fun customise(openApi: OpenAPI) {
        openApi.components.schemas.putAll(ModelConverters.getInstance().read(ErrorDetails::class.java))
        openApi.components.schemas.putAll(ModelConverters.getInstance().read(ErrorResponse::class.java))

        openApi.paths.values.forEach { path ->
            path.readOperations().forEach { operation ->
                operation.responses.default = errorApiResponse
                operation.responses.addApiResponse("401", unauthorizedApiResponse)
            }
        }
    }
}
