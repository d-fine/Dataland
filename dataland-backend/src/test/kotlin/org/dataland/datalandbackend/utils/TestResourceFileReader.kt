package org.dataland.datalandbackend.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper

object TestResourceFileReader {
    fun getJsonString(resourceFile: String): String = this.getJsonNode(resourceFile).toString()

    fun getJsonNode(resourceFile: String): JsonNode =
        defaultObjectMapper
            .readTree(
                this.javaClass.classLoader.getResourceAsStream(resourceFile)
                    ?: throw IllegalArgumentException("Could not load the resource file"),
            )

    inline fun <reified T> getKotlinObject(resourceFile: String): T =
        this.javaClass.classLoader
            .getResourceAsStream(resourceFile)
            ?.let { defaultObjectMapper.readValue<T>(it) } ?: throw IllegalArgumentException("Could not load the object")
}
