package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.configurations.HttpStatusIntegerSerializer
import org.springframework.http.HttpStatus

/**
 * --- API model ---
 * Describes a single error
 */
data class ErrorDetails(
    val errorType: String,

    val summary: String,

    val message: String,

    @JsonProperty("httpStatus")
    @JsonSerialize(using = HttpStatusIntegerSerializer::class)
    @Schema(type = "number")
    val httpStatus: HttpStatus,

    @Hidden
    @JsonProperty("stackTrace")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val stackTrace: String? = null
)
