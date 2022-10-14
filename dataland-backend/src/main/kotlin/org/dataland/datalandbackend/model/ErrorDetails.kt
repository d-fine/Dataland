package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.dataland.datalandbackend.configurations.HttpStatusIntegerSerializer
import org.springframework.http.HttpStatus

/**
 * --- API model ---
 * Describes a single error
 */
data class ErrorDetails(
    val errorCode: String,

    val summary: String,

    val message: String,

    @JsonProperty("httpStatus")
    @JsonSerialize(using = HttpStatusIntegerSerializer::class)
    val httpStatus: HttpStatus,

    val stackTrace: String? = null
)
