package org.dataland.datalandbackend.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.text.SimpleDateFormat

/**
 * Utility class for comfortable access to an object mapper in unit tests.
 */
object JsonTestUtils {
    /**
     * The object mapper used for testing.
     */
    val testObjectMapper: ObjectMapper = jacksonObjectMapper().findAndRegisterModules().setDateFormat(SimpleDateFormat("yyyy-MM-dd"))
}
