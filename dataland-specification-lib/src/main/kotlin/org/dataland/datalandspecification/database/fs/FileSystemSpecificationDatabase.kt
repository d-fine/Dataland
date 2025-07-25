package org.dataland.datalandspecification.database.fs

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandspecification.database.SpecificationDatabase
import org.dataland.datalandspecification.specifications.DataPointBaseType
import org.dataland.datalandspecification.specifications.DataPointType
import org.dataland.datalandspecification.specifications.Framework
import org.dataland.datalandspecification.specifications.FrameworkTranslation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException

private inline fun <reified T> loadSpecifications(
    folder: File,
    objectMapper: ObjectMapper,
    logger: Logger,
): MutableMap<String, T> {
    val specifications = mutableMapOf<String, T>()
    folder.listFiles()?.forEach { file ->
        try {
            val specification = objectMapper.readValue<T>(file)
            specifications[file.nameWithoutExtension] = specification
        } catch (e: IOException) {
            logger.error("Failed to load ${T::class.simpleName} specification from file: ${file.name}", e)
            throw e
        }
    }
    return specifications
}

private inline fun <reified T> saveSpecifications(
    folder: File,
    objectMapper: ObjectMapper,
    specifications: Map<String, T>,
) {
    val allExistingPaths = folder.listFiles()?.map { it.name }?.toMutableSet() ?: mutableSetOf()
    specifications.forEach { (id, specification) ->
        val file = File(folder, "$id.json")
        allExistingPaths.remove(file.name)
        val printer = DefaultPrettyPrinter().withObjectIndenter(DefaultIndenter().withLinefeed("\n"))
        objectMapper.writer(printer).writeValue(file, specification)
    }
    allExistingPaths.forEach { require(File(folder, it).delete()) }
}

/**
 * A specification database that loads specifications from the file system.
 */
class FileSystemSpecificationDatabase(
    private val baseFolder: File,
    private val objectMapper: ObjectMapper,
) : SpecificationDatabase() {
    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        loadFromDisk()
    }

    private fun loadFromDisk() {
        loadSpecifications<DataPointBaseType>(File(baseFolder, "dataPointBaseTypes"), objectMapper, logger)
            .forEach { (id, specification) ->
                dataPointBaseTypes[id] = specification
            }
        logger.info("Loaded ${dataPointBaseTypes.size} data point type specifications")
        loadSpecifications<DataPointType>(File(baseFolder, "dataPointTypes"), objectMapper, logger)
            .forEach { (id, specification) ->
                dataPointTypes[id] = specification
            }
        logger.info("Loaded ${dataPointTypes.size} data point specifications")
        loadSpecifications<Framework>(File(baseFolder, "frameworks"), objectMapper, logger)
            .forEach { (id, specification) ->
                frameworks[id] = specification
            }
        logger.info("Loaded ${frameworks.size} framework specifications")
        loadSpecifications<FrameworkTranslation>(File(baseFolder, "translations"), objectMapper, logger)
            .forEach { (id, translation) ->
                translations[id] = translation
            }
        logger.info("Loaded ${translations.size} framework translations")

        validateIntegrity()
    }

    private fun validateIntegrity() {
        frameworks.forEach {
            it.value.validateIntegrity(this)
            assert(it.value.id == it.key) { "Framework ID does not match key: ${it.key}" }
        }
        dataPointBaseTypes.forEach {
            it.value.validateIntegrity()
            assert(it.value.id == it.key) { "Data point type ID does not match key: ${it.key}" }
        }
        dataPointTypes.forEach {
            it.value.validateIntegrity(this)
            assert(it.value.id == it.key) { "Data point ID does not match key: ${it.key}" }
        }
        translations.forEach {
            it.value.validateIntegrity()
            assert(it.value.id == it.key) { "Translation ID does not match key: ${it.key}" }
        }
    }

    /**
     * Save the specifications to the file system.
     */
    fun saveToDisk() {
        validateIntegrity()
        saveSpecifications(File(baseFolder, "dataPointBaseTypes"), objectMapper, dataPointBaseTypes)
        saveSpecifications(File(baseFolder, "dataPointTypes"), objectMapper, dataPointTypes)
        saveSpecifications(File(baseFolder, "frameworks"), objectMapper, frameworks)
        saveSpecifications(File(baseFolder, "translations"), objectMapper, translations)
    }
}
