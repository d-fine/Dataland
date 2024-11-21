package org.dataland.datalandbackend.utils

import org.dataland.datalandbackend.services.LogMessageBuilder
import org.slf4j.LoggerFactory
import java.util.UUID

/**
 * A class to help with id generation
 */
object IdUtils {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val logMessageBuilder = LogMessageBuilder()

    /**
     * Generates a UUID-string
     * @returns the generated UUID as a string
     */
    fun generateUUID(): String = UUID.randomUUID().toString()

    /**
     * Generates a correlationId as UUID-string and writes a log to make it traceable and express its association
     * with a companyId and/or dataId
     * @returns the correlationId
     */
    fun generateCorrelationId(
        companyId: String?,
        dataId: String?,
    ): String {
        val correlationId = generateUUID()
        logger.info(
            logMessageBuilder.generateCorrelationIdMessage(
                correlationId = correlationId,
                companyId = companyId,
                dataId = dataId,
            ),
        )
        return correlationId
    }
}
